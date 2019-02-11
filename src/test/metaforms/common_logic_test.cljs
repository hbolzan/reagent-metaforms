(ns test.metaforms.common-logic-test
  (:require [cljs.test :refer (deftest is)]
            [test.metaforms.fixtures :as fixtures]
            [metaforms.common.logic :as common.logic]))

(deftest remove-nth-test
  (is (=
       (common.logic/remove-nth [1 2 3 4 5] 2)
       [1 2 4 5])))
