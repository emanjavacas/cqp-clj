(ns cqp-clj.spec
  (:require [clojure.java.io :refer [reader]]
            [clojure.string :as str]))

(defn spec-from-file [fname]
  (let [lines (line-seq (reader fname))]
    (into {} (for [line lines
                   :let [[k v] (str/split line #"\t")]]
               [(keyword k) (if (= "port" k) (Integer/parseInt v) v)]))))

(def default-spec
  {:host "127.0.0.1"
   :port 4877   
   :user "user"
   :pass "pass"})

(def cqp-spec (spec-from-file "cqp-clj.init"))
