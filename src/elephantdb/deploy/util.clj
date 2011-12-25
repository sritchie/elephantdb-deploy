(ns elephantdb.deploy.util
  (:require [clojure.string :as s]
            [pallet.execute :as execute]))

(defn keywordize [x]
  (if (keyword? x)
    x
    (keyword x)))

(defn deep-merge-with
  "Copied here from clojure.contrib.map-utils. The original may have
   been a casualty of the clojure.contrib cataclysm.

   Like merge-with, but merges maps recursively, applying the given fn
   only when there's a non-map at a particular level.

   (deepmerge + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
                {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
   -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}"
  [f & maps]
  (apply
   (fn m [& maps]
     (if (every? map? maps)
       (apply merge-with m maps)
       (apply f maps)))
   maps))

(def env-keys-to-resolve [:public-key-path :private-key-path])

(defn resolve-path [path]
  (s/trim (:out (execute/local-script (echo ~path)))))

(defn resolve-keypaths
  [user-map]
  (reduce #(%2 %1)
          user-map
          (for [kwd env-keys-to-resolve]
            #(if (kwd %)
               (update-in % [kwd] resolve-path)
               %))))
