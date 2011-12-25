(ns elephantdb.deploy.provision
  (:use clojure.contrib.command-line
        pallet.compute
        pallet.core
        pallet.resource
        [pallet.configure :only (pallet-config compute-service-properties)]
        [pallet.blobstore :only (blobstore-from-map)]
        [pallet.utils :only (make-user)]
        [elephantdb.deploy.security :only (authorize-group)]
        ;; TODO: Replace this with equivalent pallet command
        [org.jclouds.compute :only (nodes-with-tag)])
  (:require [elephantdb.deploy.util :as util]
            [elephantdb.deploy.node :as node]
            [pallet.request-map :as rm]
            [elephantdb.deploy.crate.edb-configs :as edb-configs])
  (:import org.jclouds.rest.ResourceNotFoundException)
  (:gen-class))

(defn my-region []
  (-> (pallet-config) :services :default :jclouds.regions))

(defn jclouds-group
  [& group-pieces]
  (str "jclouds#"
       (apply str group-pieces)
       "#"
       (my-region)))

(defn- print-ips-for-tag! [aws tag-str]
  (let [running-node (filter running? (nodes-with-tag tag-str aws))]
    (println "TAG:     " tag-str)
    (println "PUBLIC:  " (map primary-ip running-node))
    (println "PRIVATE: " (map private-ip running-node))))

;; TODO: Figure out how to get user into `ips!` and uncomment this in
;; `start!`.
(defn ips! [ring]
  (let [{:keys [group-name]} (node/edb-group-spec ring "elephantdb")
        aws (compute-service-from-map (:deploy-creds (edb-configs/edb-config)))]
    (print-ips-for-tag! aws (name group-name))))

(defn aws-user-id []
  (-> (edb-configs/edb-config)
      :deploy-creds
      :aws-user-id))

(defn edb-compute-service []
  (let [{:keys [deploy-creds]} (edb-configs/edb-config)
        deploy-creds (update-in deploy-creds [:user] util/resolve-keypaths)]
    (compute-service-from-map deploy-creds)))

(defn- converge-edb!
  [ring count local?]
  (let [{:keys [data-creds deploy-creds]} (edb-configs/edb-config)
        {:keys [user environment auth-groups] :as deploy-creds}
        (update-in deploy-creds [:user] util/resolve-keypaths)
        compute (if local?
                  (service "virtualbox")
                  (compute-service-from-map (dissoc deploy-creds :user :auth-groups)))
        {username :user :as options} (:user deploy-creds)
        user (->> options
                  (apply concat)
                  (apply make-user username))
        node-set (node/edb-group-spec ring user :local? local?)
        edb-compute-args [:compute compute
                          :user user
                          :environment
                          (merge (:environment deploy-creds)
                                 {:ring ring
                                  :blobstore (blobstore-from-map data-creds)
                                  :edb-s3-keys data-creds})]]
    (apply converge {node-set count} edb-compute-args)
    (when (and (not local?) auth-groups)
      (let [region (my-region)
            sec-group (jclouds-group (str "edb-" ring))
            user-id (aws-user-id)]
        (doseq [from-group auth-groups]
          (try (authorize-group compute
                                region
                                from-group
                                sec-group
                                user-id)
               (catch ResourceNotFoundException _
                 (log-message (str from-group
                                   " doesn't exist, and couldn't be"
                                   " authorized!")))))))
    (apply lift
           node-set
           (concat [:phase :edb-config]
                   edb-compute-args))))

(defn converge-vmfest [n]
  (converge {node/vmfest-node n}
            :compute (service "virtualbox")))

(defn start! [ring & {local? :local?}]
  (let [{count :node-count} (edb-configs/read-global-conf! ring)]
    (converge-edb! ring count local?)
    (println "Cluster Started.")
    #_(ips! ring)))

(defn stop! [ring & {local? :local?}]
  (converge-edb! ring 0 local?)
  (print "Cluster Stopped."))

(defn -main [& args]
  (with-command-line args
    "Provisioning tool for ElephantDB Clusters."
    [[start? "Start Cluster?"]
     [stop? "Shutdown Cluster?"]
     [local? "Local mode?"]
     [ring "ElephantDB Ring Name"]
     [ips? "Cluster IPs"]]
    (if-not ring
      (println "Please pass in a ring name with --ring.")
      (cond  start? (start! ring :local? local?)
             stop? (stop! ring :local? local?)
             ips? (ips! ring)
             :else (println "Must pass --start or --stop")))))
