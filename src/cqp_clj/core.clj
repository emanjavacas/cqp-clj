(ns cqp-clj.core
  (:import [CqiClient])
  (:require [cqp-clj.spec :refer [cqp-spec]]
            [cqp-clj.paginator :refer [paginator]]))

(set! *warn-on-reflection* true)

(declare connect!)

(defrecord CQiClient [client last])

(defn make-cqi-client
  "Ctor for CQiClient record."
  ([host port]
   (let [client (CqiClient. host port)]
     (map->CQiClient {:client client :last (atom nil)})))
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
  "Do a query on a given corpus. Returns corpus name when successful."
  ([cqi-client corpus query]
   (query! cqi-client corpus query "utf8"))
  ([cqi-client ^String corpus ^String query ^String charset]
   (let [{^CqiClient client :client last :last} cqi-client]
     (when-let [success (.query client corpus query charset)]
       (reset! last corpus)))))

(defn query-size
  ([cqi-client]
   (when-let [corpus @(:last cqi-client)]
     (query-size cqi-client corpus)))
  ([cqi-client corpus]
   (let [{client :client} cqi-client]
     (.querySize ^CqiClient client corpus))))

(defn cpos-range
  "Extract corpus positions for current query.
  Returns a vector with three vectors corresponding
  to match-start match-end & target for each match."
  ([cqi-client from to]
   (when-let [corpus @(:last cqi-client)]
     (cpos-range cqi-client corpus from to)))  
  ([cqi-client corpus from to]
   (let [{client :client} cqi-client]
     (map vec (.dumpSubCorpus ^CqiClient client corpus from to)))))

(defn target->idx 
  "Extract target position in a range"
  [start end target]
  (- target start))

(defn span->attr
  ([cqi-client name from to]
   (when-let [corpus @(:last cqi-client)]
     (span->attr cqi-client corpus name from to)))
  ([cqi-client ^String corpus ^String name ^Integer from ^Integer to]
   (let [{client :client} cqi-client]
     (vec (.dumpPositionalAttributes ^CqiClient client corpus name from to)))))

;;; check java source
(defn span->struc
  ([cqi-client name from to]
   (when-let [corpus @(:last cqi-client)]
     (span->attr cqi-client corpus name from to)))
  ([cqi-client ^String corpus ^String name ^Integer from ^Integer to]
   (let [{client :client} cqi-client]
     (vec (.dumpStructuralAttributes ^CqiClient client corpus name from to)))))

(defn span->value
  [cqi-client from to {attr-type :attr-type attr-name :attr-name}]
  (let [{client :client corpus :last} cqi-client
        tokens (case attr-type
                 :pos (span->attr cqi-client attr-name from to)
                 :struc (span->struc cqi-client attr-name from to))]
    (vec (map #(hash-map (keyword attr-name) %) tokens))))

(defn span->values 
  [cqi-client from to & attrs]
  (apply map merge (map #(span->value cqi-client from to %) attrs)))

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
  [cqi-client [start end target] context & attrs]
  (let [from (max 0 (- start context))
        to (+ end context)
        tokens (apply span->values cqi-client from to attrs)]
    (map-indexed (fn [idx i]
                   (merge i (sort-position idx start end target from)))
                 tokens)))

(defn cpos-seq-handler [cqi-client cpos context & attrs])

(defmacro with-cqi-client [client-bindings & body]
  (assert (vector? client-bindings) "binding has vector form")
  (assert (= 2 (count client-bindings)) "admits only one binding")
  `(let ~client-bindings
     (let [result# (into [] (do ~@body))]
       (disconnect! ~(client-bindings 0))
       result#)))

(def pos-attr {:attr-type :pos :attr-name "word"}) 
(def word-attr {:attr-type :pos :attr-name "pos"})
(def lemma-attr {:attr-type :pos :attr-name "lemma"})

(with-cqi-client [cqi-client (make-cqi-client cqp-spec)]
  (query! cqi-client "NL" "@[word='dick']")
  (let [cpos (cpos-range cqi-client "NL" 0 10)]
    (map #(cpos-token-handler cqi-client % 2 pos-attr word-attr)
         (apply map vector cpos))))

;;; sample run
(def client (make-cqi-client cqp-spec))
(query! client "NL" "'een' @[word='groot']")
(def cpos (cpos-range client 0 10))
(def output-matches
  (map #(cpos-token-handler client % 2 pos-attr word-attr)
       (apply map vector cpos)))
(true? (= matches (count output-matches)))
(disconnect! client)
;;; input:  [from to & context target?]
;;; output (vector length (to - from) + 2 * context:
;;; check attributes
