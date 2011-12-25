(ns elephantdb.deploy.node
  (:use pallet.core
        [pallet.phase :only (phase-fn)]
        [elephantdb.deploy.crate.raid :only [m1-large-raid-0]])
  (:require [pallet.crate.automated-admin-user :as automated-admin-user]
            [elephantdb.deploy.crate.daemontools :as daemontools]
            [elephantdb.deploy.crate.edb :as edb]
            [elephantdb.deploy.crate.edb-configs :as edb-configs]))

(defn edb-node-spec [ring]
  (let [{port :port} (edb-configs/read-global-conf! ring)]
    (node-spec
     :image {:image-id "us-east-1/ami-08f40561"
             :hardware-id "m1.large"
             :inbound-ports [22 port]})))

(defn edb-server-spec [admin-user]
  (let [fd-limit "500000"
        users ["root" (.username admin-user)]]
    (server-spec
     :phases {:bootstrap (phase-fn
                          (automated-admin-user/automated-admin-user
                           (.username admin-user)
                           (.public-key-path admin-user))
                          (m1-large-raid-0)
                          (edb/filelimits fd-limit users))
              :configure (phase-fn
                          (daemontools/daemontools))
              :edb-config (phase-fn
                           (edb/setup)
                           (edb/deploy))})))

(defn edb-group-spec [ring user]
  (group-spec (str "edb-" ring)
              :node-spec (edb-node-spec ring)
              :extends [(edb-server-spec user)]))
