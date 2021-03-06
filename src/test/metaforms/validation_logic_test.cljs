(ns test.metaforms.validation-logic-test
  (:require [cljs.test :refer (deftest is)]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.complex-forms.validation-logic :as vl]))

(deftest replace-url-tags-test
  (is (= (vl/replace-url-tags "http://host/{service}/{method}/" {:service "srv" :method "m"})
         "http://host/srv/m/")))

(deftest named-arguments?-test
  (is (= (vl/named-arguments? {}) false))
  (is (= (vl/named-arguments? {:named-arguments {}}) false))
  (is (= (vl/named-arguments? {:named-arguments {:a 1}}) true)))

(deftest single-argument-value-test
  (with-redefs [cf.logic/form-by-id-field-editing-value (fn [m id k] (get m k))]
    (let [db {:a "A" :b "B"}]
      (is (= (vl/single-argument-value db :form-id {} "X") "X"))
      (is (= (vl/single-argument-value db :form-id {:single-argument "a"} "X") "A"))
      (is (= (vl/single-argument-value db :form-id {:named-arguments {:k "b"}} "X") nil)))))

(deftest with-single-argument-test
  (with-redefs [cf.logic/form-by-id-field-editing-value (fn [m id k] (get m k))]
    (let [db {:a "A" :b "B"}
          url "http://service/method/"]
      (is (= (vl/with-single-argument url db :form-id {} "X") "http://service/method/X/"))
      (is (= (vl/with-single-argument url db :form-id {:single-argument "a"} "X") "http://service/method/A/"))
      (is (= (vl/with-single-argument url db :form-id {:named-arguments {:k "b"}} "X") url)))))

(deftest with-named-arguments-test
  (with-redefs [cf.logic/form-by-id-field-editing-value (fn [m id k] (get m k))]
    (let [db {:a "A" :b "B"}
          url "http://service/method/X/"]
      (is (= (vl/with-named-arguments url db :form-id {}) url))
      (is (= (vl/with-named-arguments url db :form-id {:named-arguments {:k "B"}}) "http://service/method/X/?k=B"))
      (is (= (vl/with-named-arguments url db :form-id {:named-arguments {:k "B" :y "A"}})
             "http://service/method/X/?k=B&y=A")))))

(deftest get-in-path-test []
  (is (= (vl/get-in-path "a.b.c" {:a {:b {:c "x"}}}) "x")))

(deftest expected-result-value-test []
  (is (= (vl/expected-result-value "a.b.c" {:data {:additional_information {:a {:b {:c "x"}}}}}) "x")))

(deftest expected-results->fields-test []
  (let [validation {:expected-results {:field_a "a.b" :field_b "a.c"}}
        response_a {:data {:additional_information {:a {:b "B" :c "C"}}}}
        response_b {:data {:additional_information {:a {:b "X" :c "Y"}}}}]
    ;; values wrapped into vectors force unconditional form fields to be updated
    (is (= (vl/expected-results->fields validation response_a) {:field_a ["B"] :field_b ["C"]}))
    (is (= (vl/expected-results->fields validation response_b) {:field_a ["X"] :field_b ["Y"]})
        (is (= (vl/expected-results->fields {} response_b) {})))))

(deftest before-validate-test []
  "Before validate is expected to modify argument value applying before-validate actions"
  (let [actions-map {:x identity
                     :y (constantly "mod-arg")}]
    (is (= (vl/before-validate actions-map "arg" {}) "arg"))
    (is (= (vl/before-validate actions-map "arg" {:before-validate [""]}) "arg"))
    (is (= (vl/before-validate actions-map "arg" {:before-validate ["no-available-action"]}) "arg"))
    (is (= (vl/before-validate actions-map "arg" {:before-validate ["x"]}) "arg"))
    (is (= (vl/before-validate actions-map "arg" {:before-validate ["y"]}) "mod-arg"))))
