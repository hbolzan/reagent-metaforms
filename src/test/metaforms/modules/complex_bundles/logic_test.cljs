(ns test.metaforms.modules.complex-bundles.logic-test
  (:require [cljs.test :refer (deftest is)]
            [metaforms.modules.complex-bundles.logic :as cb.logic]))

(deftest dissoc-definitions-test
  (let [bundled-tables [{:complex-id "BUNDLED_TABLE_001"
                         :definition [{:id "table-001"}]}]]
    (is (= (cb.logic/dissoc-definitions bundled-tables)
           [{:complex-id "BUNDLED_TABLE_001" :definition-id "table-001"}]))))

(deftest parse-element-test []
  (is (=
       (cb.logic/parse-element ":a")
       :a)))

(deftest parse-branch-test []
  (is (=
       (cb.logic/parse-branch [":a" "A"])
       [:a "A"]))
  (is (=
       (cb.logic/parse-branch [":b" "B"])
       [:b "B"]))
  (is (=
       (cb.logic/parse-branch [":a" "A" [":b" "B"]])
       [:a "A" [:b "B"]]))
  (is (=
       (cb.logic/parse-branch [":a" "A" {:x 1 :y 2} [":b" "B"]])
       [:a "A" {:x 1 :y 2} [:b "B"]]))
  (is (=
       (cb.logic/parse-branch [":a" "A" [":b" "B"] [":c" "C"]])
       [:a "A" [:b "B"] [:c "C"]]))
  (is (=
       (cb.logic/parse-branch [":a" "A" {:x 1 :y 2} [":b" "B" [":c" "C"]] [":d" "D"]])
       [:a "A" {:x 1 :y 2} [:b "B" [:c "C"]] [:d "D"]]))
  (is (=
       (cb.logic/parse-branch [":table" [":tr", [":td", 1], [":td", 2], [":td", 3]],
                               [":tr", [":td", 10], [":td", 22], [":td", 33]]])
       [:table [:tr, [:td, 1], [:td, 2], [:td, 3]],
        [:tr, [:td, 10], [:td, 22], [:td, 33]]])))
