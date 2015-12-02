(ns cqp-clj.spec
  (:require [clojure.java.io :refer [reader]]
            [clojure.string :as str]))

(def cqp-spec
  (try 
    (let [lines (line-seq (reader "cqp-clj.init"))]
      (into {} (for [line lines
                     :let [[k v] (str/split line #"\t")]]
                 [(keyword k) (if (= "port" k) (Integer/parseInt v) v)])))
    (catch java.io.IOException e
      (prn "Couldn't open init file: initiating default parameters")
      {:port 4877
       :host "127.0.0.1"
       :user "user"
       :pass "pass"})))
