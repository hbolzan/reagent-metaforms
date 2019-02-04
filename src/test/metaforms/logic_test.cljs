(ns test.metaforms.logic-test
  (:require [cljs.test :refer (deftest is)]
            [test.metaforms.fixtures :as fixtures]
            [metaforms.modules.complex-forms.logic :as cf-logic]))

(deftest new-record-test
  (is (=
       (cf-logic/new-record fixtures/fields-defs)
       {:id            19
        :name          "Some name"
        :address       nil
        :initial_date  "2019-01-18"
        :date_and_time "2019-01-18T12:34:15Z"})
      "A new data record should be created with fields definitions considering default values"))
