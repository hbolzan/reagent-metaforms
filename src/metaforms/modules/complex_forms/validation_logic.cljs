(ns metaforms.modules.complex-forms.validation-logic
  (:require [clojure.string :as str]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.logic :as cf.logic]))

(defn replace-url-tags [url validation]
  (reduce (fn [u tag] (cl/replace-tag u (first tag) (last tag))) url [["service" (:service validation)]
                                                                     ["method" (:method validation)]]))

(defn no-named-arguments? [validation]
  (empty? (:named-arguments validation)))

(defn no-arguments-defined? [validation]
  (and (empty? (:single-argument validation)) (no-named-arguments? validation)))

(def actions-map {:clear-separators cl/clear-separators})

(defn apply-action [arg action]
  ((get actions-map (keyword action)) arg))

(defn apply-before-validate [arg actions]
  (reduce (fn [r action] (apply-action r action)) arg actions))

(defn before-validate [arg {actions :before-validate :as validation}]
  (if (empty? actions)
    arg
    (apply-before-validate arg actions)))

(defn single-argument [db form-id validation field-value]
  (if (no-arguments-defined? validation)
    field-value
    (cf.logic/form-by-id-field-value db form-id (-> validation :single-argument keyword))))

(defn with-single-argument [url db form-id validation field-value]
  "If single argument is present, appends the argument segment to the URL"
  (if-let [single-argument (single-argument db form-id validation field-value)]
    (str url (before-validate single-argument validation) "/")
    url))

(defn named-argument [db form-id field-name]
  (if-let [field-value (cf.logic/form-by-id-field-value db form-id (keyword field-name))]
    field-value
    field-name))

(defn named-arguments->url-params [db form-id named-arguments]
  (str/join "&" (map #(str (name (first %)) "=" (named-argument form-id db (last %)))
                     named-arguments)))

(defn with-named-arguments [url db form-id validation]
  "If there are named arguments, append them as url params (i.e. ?param_1=value_1&param_2=value2 ...)"
  (if (no-named-arguments? validation)
    url
    (str url "?" (named-arguments->url-params db form-id (:named-arguments validation)))))

(defn build-validation-url
  ([db base-url validation field-value]
   (build-validation-url db (:current-form db) base-url validation field-value))
  ([db form-id base-url validation field-value]
   (-> base-url
       (with-single-argument db form-id validation field-value)
       (replace-url-tags validation)
       (with-named-arguments db form-id validation))))

(defn get-in-path [path m]
  "returns value from map following dots path (i.e: path.to.some.key)"
  (get-in m (map keyword (str/split path "."))))

(defn expected-result-value [result-path response]
  (get-in-path (str "data.additional_information." result-path) response))

(defn expected-results->fields [validation response]
  (reduce-kv
   (fn [result field path] (assoc result field [(expected-result-value path response)]))
   {}
   (:expected-results validation)))
