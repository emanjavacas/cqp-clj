(ns cqp-clj.core-test
  (:require [clojure.test :refer :all]
            [cqp-clj.core :refer :all]
            [cqp-clj.paginator :refer :all]))

(deftest sort-position-test-a
  (testing "sort-position in the middle"
    (let [start   16
          end     18
          target  17
          context 2
          to      (+ end context)
          from    (max 0 (- start context))
          length  (- to from)]
      (is (= (vec (map #(apply sort-position %) 
                         (map-indexed 
                          cons 
                          (repeat length [start end target from]))))
             [{:id 14}
              {:id 15}
              {:id 16 :match true}
              {:id 17 :match true :target true}
              {:id 18 :match true}
              {:id 19}])))))

(deftest sort-position-test-b
  (testing "sort-position at the end (less matches than expected)"
    (let [start   16
          end     18
          target  17
          context 2
          to      (+ end context)
          from    (max 0 (- start context))
          length  (- to from)]
      (is (= (vec (map #(apply sort-position %) 
                       (map-indexed 
                        cons 
                        (repeat length [start end target from]))))
             [{:id 14}
              {:id 15}
              {:id 16 :match true}
              {:id 17 :match true :target true}
              {:id 18 :match true}
              {:id 19}])))))

(deftest paginator-test
  (testing "basic pagination functionality"
    (let [pager (paginator (range 10))]
      (is (= (repeatedly 5 (fn [] (next-page pager 2)))
             '([0 2] [2 4] [4 6] [6 8] [8 0])))
      (is (= (current pager)
             0))
      (is (= (repeatedly 5 (fn [] (prev-page pager 2)))
             '([8 0] [6 8] [4 6] [2 4] [0 2]))))))

