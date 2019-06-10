(ns test.metaforms.modules.grid.logic-test
  (:require [cljs.test :refer (deftest is)]
            [metaforms.modules.grid.logic :as grid.logic]))

(deftest clear-pk-fields-test
  (is (= (grid.logic/clear-pk-fields ["a"] {:a 1 :b 2 :c 3}) {:b 2 :c 3}))
  (is (= (grid.logic/clear-pk-fields ["a" "b"] {:a 1 :b 2 :c 3}) {:c 3})))

(deftest clear-data-pk-fields-test
  (is (= (grid.logic/clear-data-pk-fields [{:a 1 :b 2 :c 3}{:a 10 :b 20 :c 30}] true ["a"])
         [{:b 2 :c 3}{:b 20 :c 30}]))
  (is (= (grid.logic/clear-data-pk-fields [{:a 1 :b 2 :c 3}{:a 10 :b 20 :c 30}] false ["a"])
         [{:a 1 :b 2 :c 3}{:a 10 :b 20 :c 30}])))

(deftest fill-related-fields-test
  (let [data        [{:id 1 :parent 100 :a "x" :b "y"}
                     {:id 2 :parent nil :a "A" :b "B"}]
        parent-data {:id 200}]
    (is (= (grid.logic/fill-related-fields data parent-data ["parent"] ["id"])
           [{:id 1 :parent 200 :a "x" :b "y"}
            {:id 2 :parent 200 :a "A" :b "B"}]))))
