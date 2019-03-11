(ns test.metaforms.logic-test
  (:require [cljs.test :refer (deftest is)]
            [test.metaforms.fixtures :as fixtures]
            [metaforms.modules.complex-forms.logic :as cf-logic]))

(deftest new-record-test
  (is (=
       (cf-logic/new-record fixtures/fields-defs)
       {:id            19
        :name          "Some name"
        :address       ""
        :initial_date  "2019-01-18"
        :date_and_time "2019-01-18T12:34:15Z"})
      "A new data record should be created with fields definitions considering default values"))

(deftest empty-record-test
  (is (=
       (cf-logic/empty-record fixtures/fields-defs)
       {:id            ""
        :name          ""
        :address       ""
        :initial_date  ""
        :date_and_time ""})
      "A new data record should be created with empty values"))

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

(deftest typecast-test
  (is (= (cf-logic/typecast "abc" :char) "abc"))
  (is (= (cf-logic/typecast "" :integer) nil))
  (is (= (cf-logic/typecast "" :float) nil))
  (is (= (cf-logic/typecast "123" :integer) 123))
  (is (= (cf-logic/typecast "1.23" :float) 1.23))
  (is (= (cf-logic/typecast "" :date) nil))
  (is (= (cf-logic/typecast "2019-01-20" :date) "2019-01-20"))
  (is (= (cf-logic/typecast "10:23" :time) "10:23"))
  (is (= (cf-logic/typecast "2019-01-20T10:23" :time) "2019-01-20T10:23")))

(deftest field-typecast-test
  (is (= (cf-logic/field-typecast {:a "123" :b "abc" :c "1.23"}
                         {:name "a" :data-type "integer"})
         {"a" 123}))

  (is (= (cf-logic/field-typecast {:a "123" :b "abc" :c "1.23"}
                                  {:name "b" :data-type "char"})
         {"b" "abc"})))

(deftest data-record->typed-data-test
  (let [data-record {:a "123" :b "abc" :c "1.23"}
        fields-defs [{:name "a" :data-type "integer"}
                     {:name "b" :data-type "char"}
                     {:name "c" :data-type "float"}]]
    (is (= (cf-logic/data-record->typed-data data-record fields-defs)
           {"a" 123 "b" "abc" "c" 1.23})))

  (let [data-record {:id "" :a "123" :b "abc" :c "1.23"}
        fields-defs [{:name "id" :data-type "integer"}
                     {:name "a" :data-type "integer"}
                     {:name "b" :data-type "char"}
                     {:name "c" :data-type "float"}]]
    (is (= (cf-logic/data-record->typed-data data-record fields-defs)
           {"id" nil "a" 123 "b" "abc" "c" 1.23}))))
