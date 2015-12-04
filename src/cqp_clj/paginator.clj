(ns cqp-clj.paginator)

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
     (cond (zero? from) [(- size page-size) 0] ;`size` instead of 0?
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
      [from (reset! current to)]))
  (prev-page [this page-size]
    (let [[from to] (pager-prev (count coll) page-size @current)]
      [(reset! current from) to]))
  (nth-page [this page-size n]
    [n (reset! current (+ n page-size))]))

(defn paginator [coll]
  (Paginator. coll (atom 0)))

