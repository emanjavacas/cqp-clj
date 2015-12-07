(ns cqp-clj.spec
  (:require [clojure.java.io :refer [reader]]
            [clojure.string :as str]))

(def default-spec
  {:host "127.0.0.1"
   :port 4877})

(defn uncomment [line]
  (first (str/split line #";")))

(defn read-init [fname]
  (let [lines (line-seq (reader fname))]
    (reduce merge default-spec 
            (for [line lines
                  :let [tokens (str/split (uncomment line) #" ")]]            
              (if (= 2 (count tokens))
                {(keyword (first tokens)) (second tokens)}
                {:user (second tokens)
                 :pass (str/replace (last tokens) #"\W+" "")})))))
