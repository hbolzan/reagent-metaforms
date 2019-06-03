(ns test.metaforms.modules.complex-bundles.logic-test
  (:require [cljs.test :refer (deftest is)]
            [metaforms.modules.complex-bundles.logic :as cb-logic]))

(deftest dissoc-definitions-test
  (let [bundled-tables [{:complex-id "BUNDLED_TABLE_001"
                         :definition [{:id "table-001"}]}]]
    (is (= (cb-logic/dissoc-definitions bundled-tables)
           [{:complex-id "BUNDLED_TABLE_001" :definition-id "table-001"}]))))
