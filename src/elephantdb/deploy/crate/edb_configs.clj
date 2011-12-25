(ns elephantdb.deploy.crate.edb-configs
  (:use pallet.compute
        [org.jclouds.blobstore :only (upload-blob)]
        [pallet.resource.remote-file :only (remote-file)])
  (:require [elephantdb.deploy.util :as u]
            [pallet.configure :as conf]
            [pallet.session :as session]))

(defn edb-config []
  (conf/compute-service-properties (conf/pallet-config)
                                   ["elephantdb"]))

(defn edb-ring-config [ring]
  (let [ring (u/keywordize ring)]
    (-> (edb-config) :configs ring)))

(def read-global-conf!
  (comp :global edb-ring-config))

(def read-local-conf!
  (comp :local edb-ring-config))

(defn- global-conf-with-hosts
  [session local-config]
  (let [hosts (map private-ip (session/nodes-in-group session))]
    (prn-str (assoc local-config :hosts hosts))))

;; TODO: Move path out to config staging path in pallet config.
(defn upload-global-conf!
  [session]
  (let [ring (-> session :environment :ring)
        local-conf (read-global-conf! ring)
        s3-conf (global-conf-with-hosts session local-conf)
        s3-key (format "configs/elephantdb/%s/global-conf.clj" ring)]
    (upload-blob "hdfs2" s3-key s3-conf (:blobstore session))
    session))

;; TODO: Pull this stuff out of local-conf.
(defn local-conf-with-keys
  [session local-conf]
  (let [{:keys [edb-s3-keys]} session
        {:keys [identity credential]} edb-s3-keys]
    (if-not (and identity credential)
      local-conf
      (u/deep-merge-with #(identity %2)
                         {:hdfs-conf {"fs.s3n.awsAccessKeyId" (:identity edb-s3-keys)
                                      "fs.s3n.awsSecretAccessKey" (:credential edb-s3-keys)}}
                         local-conf))))

(defn remote-file-local-conf!
  [session dst-path]
  (let [conf-with-keys (->> (-> session :environment :ring)
                            (read-local-conf!)
                            (local-conf-with-keys session)
                            (prn-str))]
    (remote-file session
                 dst-path
                 :content conf-with-keys)))

