(ns cqp-clj.core
  (:import [CqiClient] [CqiClientException])
  (:require [cqp-clj.spec :refer [read-init]]
            [cqp-clj.paginator :refer [paginator]]))

(set! *warn-on-reflection* true)

(declare connect!)

(defrecord CQiClient [client])

(defn make-cqi-client
  "Ctor for CQiClient record."
  ([host port]
   (let [client (CqiClient. host port)]
     (map->CQiClient {:client client})))
  ([host port user pass]
   (let [cqi-client (make-cqi-client host port)]
     (do (connect! cqi-client user pass)
         cqi-client)))
  ([{host :host port :port user :user pass :pass}]
   (make-cqi-client host port user pass)))

(defn connect! [cqi-client user pass]
  (let [{client :client} cqi-client] 
    (.connect ^CqiClient client user pass)))

(defn disconnect! ^Boolean [cqi-client]
  (let [{client :client} cqi-client]
    (.disconnect ^CqiClient client)))

(defn query!
  "Do a query on a given corpus."
  ([cqi-client corpus query]
   (query! cqi-client corpus query "utf8"))
  ([cqi-client ^String corpus ^String query ^String charset]
   (let [{client :client} cqi-client]
     (.query ^CqiClient client corpus query charset))))

(defn query-size
  ([cqi-client corpus]
   (let [{client :client} cqi-client]
     (.querySize ^CqiClient client corpus))))

(defn cpos-range
  "Extract corpus positions for current query.
  Returns a vector with three vectors corresponding
  to match-start match-end & target for each match."
  ([cqi-client corpus from]
   (let [to (query-size cqi-client corpus)]
     (cpos-range cqi-client corpus from to)))
  ([cqi-client corpus from to]   
   (let [{client :client} cqi-client]
     (map vec (.dumpSubCorpus ^CqiClient client corpus from to)))))

(defn target->idx 
  "Extract target position in a range"
  [start end target]
  (- target start))

(defn span->attr
  ([cqi-client corpus name from to]
   (span->attr cqi-client corpus name from to "utf8"))
  ([cqi-client ^String corpus ^String name ^Integer from ^Integer to ^String charset]
   (let [{client :client} cqi-client]
     (vec (.dumpPositionalAttributes ^CqiClient client corpus name from to charset)))))

;;; check java source
(defn span->struc
  ([cqi-client corpus name from to]
   (span->struc cqi-client corpus name from to "utf8"))
  ([cqi-client ^String corpus ^String name ^Integer from ^Integer to ^String charset]
   (let [{client :client} cqi-client]
     (vec (.dumpStructuralAttributes ^CqiClient client corpus name from to charset)))))

(defn span->value
  ([cqi-client corpus from to {attr-type :attr-type attr-name :attr-name}]
   (span->value corpus from to "utf8" {:attr-type attr-type :attr-name attr-name}))
  ([cqi-client corpus from to charset {attr-type :attr-type attr-name :attr-name}]
   (let [{client :client} cqi-client
         tokens (case attr-type
                  :pos (span->attr cqi-client corpus attr-name from to charset)
                  :struc (span->struc cqi-client corpus attr-name from to charset))]
     (vec (map #(hash-map (keyword attr-name) %) tokens)))))

(defn span->values 
  ([cqi-client corpus from to attrs]
   (span->values cqi-client corpus from to "utf8" attrs))
  ([cqi-client corpus from to charset attrs]
   (apply mapv merge (map #(span->value cqi-client corpus from to charset %) attrs))))

(defn sort-position
  "from might not be equal to (- start context)"
  [idx start end target from]
  (let [idx (+ idx from)]
    (cond
      (= idx target) {:id idx :match true :target true}
      (< idx start)  {:id idx}
      (> idx end)    {:id idx}
      (or 
       (>= idx start)
       (<= idx end)) {:id idx :match true})))

(defn cpos-token-handler
  "Extract desired info from cpos token adding positional
  information as defined in `sort-position`"
  ([cqi-client corpus [start end target] context attrs]
   (cpos-token-handler cqi-client corpus [start end target] context "utf-8" attrs))
  ([cqi-client corpus [start end target] context charset attrs]
   (let [from (max 0 (- start context))
         to (+ end context)
         tokens (span->values cqi-client corpus from to charset attrs)]
     (map-indexed (fn [idx i]
                    (merge i (sort-position idx start end target from)))
                  tokens))))

(defn cpos-seq-handler 
  ([cqi-client corpus cpos context attrs]
   (cpos-seq-handler cqi-client corpus cpos context "utf8" attrs))
  ([cqi-client corpus cpos context charset attrs]
   (map #(cpos-token-handler cqi-client corpus % context charset attrs)
        (apply map vector cpos))))

(defmacro with-cqi-client [client-bindings & body]
  (assert (vector? client-bindings) "binding has vector form")
  (assert (= 2 (count client-bindings)) "admits only one binding")
  `(let ~client-bindings
     (try
       (into [] (do ~@body))
       (catch Exception e#
         (throw (ex-info (:message (bean e#)) {})))
       (finally (disconnect! ~(client-bindings 0))))))

(def pos-attr {:attr-type :pos :attr-name "pos"}) 
(def word-attr {:attr-type :pos :attr-name "word"})
(def lemma-attr {:attr-type :pos :attr-name "lemma"})
(def head-attr {:attr-type :struc :attr-name "np_h"})






