(ns cqp-clj.core
  (:import [CqiClient]  [CqiResult] [CqiDump])
  (:require [cqp-clj.spec :refer [cqp-spec]]))

(defn- connection [spec]
  (let [client (CqiClient. (:host cqp-spec) (:port cqp-spec))]
    (doto client (.connect (:user cqp-spec) (:pass cqp-spec)))))

(defprotocol Paginator
  (next-page [this page-size])
  (prev-page [this page-size])
  (nth-page [this page-size n]))

(defrecord CQiPaginator [result index cache]
  Paginator 
  (next-page [this page-size]
    ))

(defn cqi-paginator [result] ;ctor
  (let [index (ref 0)]
    (CQiPaginator. result index)))

(def client (connection cqp-spec))
(def result (.query client "EUROPARL-DE" "'de'"  "s"))
(def result (.queryDump client "NL" "'.*éens' [word='.*']+ within s"  "s" "utf8"))
(.corpusCharset client "GER")
(vec (.listCorpora client))
(def cpos (first (map vec (.dumpRange result 0 10))))
(map vec (map #(.dumpPositionalAttributes client "NL" "word" % (+ 5 %) "utf8") cpos))
(.size result)
(.next result)
(.getIndex result)
(.getTarget result) 
(.getContextEnd result)
(def start (.getMatchStart result))
(def end (.getMatchEnd result))
(def iterator (partition 2 (map vec (iterator-seq (.iterator result)))))
(vec (.getValues result "word" start end))
(.getStructuralAttributeValue result 0)
(def matchstarts (first (map vec (.cposRange result 0 8))))
(map vec (map #(.getValues result "word" % (+ % 2)) (first (map vec (.cposRange result 20 28)))))

(.getMatchStart result)
(.next result)

(.getValues result )
(vec (.corpusPositionalAttributes client "DICKENS"))
