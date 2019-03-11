(ns test.metaforms.dropdown-test
  (:require [cljs.test :refer (deftest is)]
            [metaforms.modules.complex-forms.components.dropdown :as dropdown]))

(deftest review-dropdown-options-test
  (let [options [{:id "A"}
                 {:id "B"}
                 {:id "C"}]]
    (is (= (dropdown/review-dropdown-options options "Other Field" :id :id-dscr)
           [{:id nil :id-dscr "-- Other Field --"}
            {:id "A" :id-dscr "A"}
            {:id "B" :id-dscr "B"}
            {:id "C" :id-dscr "C"}]))))
