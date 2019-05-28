(ns test.metaforms.modules.complex-bundles.logic-test
  (:require [cljs.test :refer (deftest is)]
            [metaforms.modules.complex-bundles.logic :as cb-logic]))

(deftest dissoc-definitions-test
  (let [bundled-tables [{:complex-id "BUNDLED_TABLE_001"
                         :definition {:id "table-001"}}]]
    (is (= (cb-logic/dissoc-definitions bundled-tables)
           [{:complex-id "BUNDLED_TABLE_001" :definition-id "table-001"}]))))

(deftest bundle-forms-ids-test
  (let [bundle   {:id "TEST_BUNDLE" :bundled-tables [{:definition-id "table-001"}]}
        bundle-2 {:id "TEST_BUNDLE" :bundled-tables [{:definition-id "table-002"}]}
        bundle-3 {:id "TEST_BUNDLE_3" :bundled-tables [{:definition-id "table-003"}
                                                       {:definition-id "table-004"}]}
        ]
    (is (= (cb-logic/bundle-forms-ids bundle) [:TEST_BUNDLE/table-001]))
    (is (= (cb-logic/bundle-forms-ids bundle-2) [:TEST_BUNDLE/table-002]))
    (is (= (cb-logic/bundle-forms-ids bundle-3) [:TEST_BUNDLE_3/table-003 :TEST_BUNDLE_3/table-004]))))
