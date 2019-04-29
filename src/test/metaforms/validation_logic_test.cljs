(ns test.metaforms.validation-logic-test
  (:require [cljs.test :refer (deftest is)]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.complex-forms.validation-logic :as vl]))

(deftest replace-url-tags-test
  (is (= (vl/replace-url-tags "http://host/{service}/{method}/" {:service "srv" :method "m"})
         "http://host/srv/m/")))

(deftest no-named-arguments?-test
  (is (= (vl/no-named-arguments? {}) true))
  (is (= (vl/no-named-arguments? {:named-arguments {}}) true))
  (is (= (vl/no-named-arguments? {:named-arguments {:a 1}}) false)))

(deftest no-arguments-defined?-test
  (is (= (vl/no-arguments-defined? {}) true))
  (is (= (vl/no-arguments-defined? {:single-argument nil}) true))
  (is (= (vl/no-arguments-defined? {:single-argument nil :named-arguments {}}) true))
  (is (= (vl/no-arguments-defined? {:single-argument "field_name" :named-arguments {}}) false))
  (is (= (vl/no-arguments-defined? {:single-argument nil :named-arguments {:k "v"}}) false)))

(deftest single-argument-test
  (with-redefs [cf.logic/current-form-field-value (fn [m k] (get m k))]
    (let [db {:a "A" :b "B"}]
      (is (= (vl/single-argument db {} "X") "X"))
      (is (= (vl/single-argument db {:single-argument "a"} "X") "A"))
      (is (= (vl/single-argument db {:named-arguments {:k "b"}} "X") nil)))))

(deftest with-single-argument-test
  (with-redefs [cf.logic/current-form-field-value (fn [m k] (get m k))]
    (let [db {:a "A" :b "B"}
          url "http://service/method/"]
      (is (= (vl/with-single-argument url db {} "X") "http://service/method/X/"))
      (is (= (vl/with-single-argument url db {:single-argument "a"} "X") "http://service/method/A/"))
      (is (= (vl/with-single-argument url db {:named-arguments {:k "b"}} "X") url)))))

(deftest with-named-arguments-test
  (with-redefs [cf.logic/current-form-field-value (fn [m k] (get m k))]
    (let [db {:a "A" :b "B"}
          url "http://service/method/X/"]
      (is (= (vl/with-named-arguments url db {}) url))
      (is (= (vl/with-named-arguments url db {:named-arguments {:k "b"}}) "http://service/method/X/?k=B"))
      (is (= (vl/with-named-arguments url db {:named-arguments {:k "b" :y "a"}})
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
