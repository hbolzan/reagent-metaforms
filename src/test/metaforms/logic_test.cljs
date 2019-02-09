(ns test.metaforms.logic-test
  (:require [cljs.test :refer (deftest is)]
            [test.metaforms.fixtures :as fixtures]
            [metaforms.modules.complex-forms.logic :as cf-logic]))

(deftest empty-record-test
  (is (=
       (cf-logic/new-record fixtures/fields-defs)
       {:id            19
        :name          "Some name"
        :address       nil
        :initial_date  "2019-01-18"
        :date_and_time "2019-01-18T12:34:15Z"})
      "A new data record should be created with fields definitions considering default values"))

(deftest next-form-state-test
  (is (= (cf-logic/next-form-state :append :view) :edit))
  (is (= (cf-logic/next-form-state :edit :view) :edit))
  (is (= (cf-logic/next-form-state :confirm :edit) :view))
  (is (= (cf-logic/next-form-state :discard :edit) :view))
  (is (= (cf-logic/next-form-state :delete :view) :deleting))
  ;; should not change state if not in expected transitions
  (is (= (cf-logic/next-form-state :append :edit) :edit))
  (is (= (cf-logic/next-form-state :edit :edit) :edit))
  (is (= (cf-logic/next-form-state :confirm :view) :view))
  (is (= (cf-logic/next-form-state :delete :edit) :edit)))
