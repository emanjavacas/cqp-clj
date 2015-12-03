(ns cqp-clj.core
  (:import [CqiClient]  [CqiResult] [CqiDump])
  (:require [cqp-clj.spec :refer [cqp-spec]]))

(defn- connection [spec]
  (let [client (CqiClient. (:host cqp-spec) (:port cqp-spec))]
    (doto client (.connect (:user cqp-spec) (:pass cqp-spec)))))

(def client (connection cqp-spec))
(def result (.cqpQuery client "DICKENS" "'the'"))
(def cpos (first (map vec (.dumpRange result 0 22221))))
(map identity cpos)
(vec (.dumpPositionalAttributes client "DICKENS" "word" (into-array Integer/TYPE cpos)))
(vec (.dumpStructuralAttributes client "DICKENS" "s" (into-array Integer/TYPE cpos)))
(.listSubcorpora client "DICKENS")

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

(defrecord CQiClient [conn query])

(defn make-cqi-client [{host :host port :port user :user pass :pass}]
  (let [conn (doto (CqiClient. host port)
               (.connect user pass))]
    (map->CQiClient {:conn conn})))

(def client (make-cqi-client cqp-spec))

;;; utility functions
(defn pager-next
  ([size page-size] (pager-next size page-size 0))
  ([size page-size from]
   (let [to (+ from page-size)]
     (if (>= to size) 
       [from 0]
       [from to]))))

(defn pager-prev
  ([size page-size] (pager-prev size page-size 0))
  ([size page-size from]
   (let [new-from (- from page-size)]
     (cond (zero? from) [(- size page-size) size]
           (zero? new-from) [0 page-size]
           (neg?  new-from)  [0 (+ new-from page-size)]
           :else [new-from from]))))

(defprotocol Paginate
  (length [_])
  (current [_])
  (next-page [_ page-size])
  (prev-page [_ page-size])
  (nth-page [_ page-size n]))

(defrecord Paginator [coll current]
  Paginate
  (length [this] (count coll))
  (current [this] @current)
  (next-page [this page-size]
    (let [[from to] (pager-next (count coll) page-size @current)]
      [from (dosync (ref-set current to))]))
  (prev-page [this page-size]
    (let [[from to] (pager-prev (count coll) page-size @current)]
      [(dosync (ref-set current from)) to]))
  (nth-page [this page-size n]
    [n (dosync (ref-set current (+ n page-size)))]))

;;; public API
(defn disconnect [client]
  (let [conn (:conn client)]
    (.disconnect client)))

(defn set-query! 
  ([client corpus query attr charset]
   (let [{conn :conn query :query} client]
     (when query (.clear query))
     (assoc client :query (.query conn corpus query attr charset))))
  ([client corpus query attr]
   (set-query! client corpus query attr "utf8"))
  ([client corpus query]
   (set-query! client corpus query "s")))

(defn query-size ^Integer [client]
  (let [{conn :conn query :query} client]
    (.subCorpusSize client query)))

(defn dump-query [client from to & fields]
  (let [{conn :conn query :query} client]
    (if (not query)
      nil
      (let [[start end target] (map vec (.dumpRange query from to))]
        
        ;; todo: [[start end target] [start end target]] -> [{:}]
        ))))


(def pager (Paginator. (range 10) (ref 0)))
(nth-page pager 14 1)
