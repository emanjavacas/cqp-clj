(ns cqp-clj.core
  (:import [CqiClient]  [CqiResult] [CqiDump])
  (:require [cqp-clj.spec :refer [cqp-spec]]))

(defrecord CQiClient [conn])

(defn make-cqi-client [{host :host port :port user :user pass :pass}]
  (let [conn (doto (CqiClient. host port)
               (.connect user pass))]
    (map->CQiClient {:conn conn})))

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
(defn disconnect! [client]
  (let [conn (:conn client)]
    (.disconnect conn)))

(defn query! 
  ([client corpus query charset]
   (let [{conn :conn} client]
     (.query conn corpus query charset)))
  ([client corpus query]
   (query! client corpus query "utf8")))

(defn query-size ^Integer [client corpus]
  (let [{conn :conn} client]
    (.querySize conn corpus)))

(defn dump-query [client corpus from to & fields]
  (let [{conn :conn} client
        [start end target] (map vec (.dumpSubCorpus conn corpus from to))]
    (map vec (map #(.dumpPositionalAttributes conn corpus "word" % %2) start end))))


(def client (make-cqi-client cqp-spec))

(query! client "EUROPARL-DE" "'.*' 'de'" "utf8")
(query-size client "EUROPARL-DE")
(dump-query client "EUROPARL-DE" 0 50)
(disconnect! client)

(def pager (Paginator. (range 10) (ref 0)))
(nth-page pager 14 1)

;;; input:  [from to & context target?]
;;; output (vector length (to - from) + 2 * context:
;;;;;; [{:cpos Int :token String :fieldX String :match bool :target bool} {} ...}]

;;; todo:
;;; include corpus in cqiClient?
