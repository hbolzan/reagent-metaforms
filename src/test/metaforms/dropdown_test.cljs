(ns test.metaforms.dropdown-test
  (:require [cljs.test :refer (deftest is)]
            [metaforms.modules.complex-forms.components.dropdown :as dropdown]))

(deftest dropdown-options-test
  (let [options [{:id "A"}
                 {:id "B"}
                 {:id "C"}]]
    (is (= (dropdown/dropdown-options options "Other Field" :id :id)
           [[:option {:value nil, :key 0} "-- Other Field --"]
            [:option {:value "A", :key "A"} "A"]
            [:option {:value "B", :key "B"} "B"]
            [:option {:value "C", :key "C"} "C"]])))

  (let [options [{:id 1 :name "First"}
                 {:id 2 :name "Second"}
                 {:id 3 :name "Third"}]]
    (is (= (dropdown/dropdown-options options "Other Field" :id :name)
           [[:option {:value nil, :key 0} "-- Other Field --"]
            [:option {:value 1, :key 1} "First"]
            [:option {:value 2, :key 2} "Second"]
            [:option {:value 3, :key 3} "Third"]]))))
