(ns metaforms.modules.complex-forms.validation-logic
  (:require [clojure.string :as str]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.logic :as cf.logic]))

(defn replace-url-tags [url validation]
  (reduce (fn [u tag] (cl/replace-tag u (first tag) (last tag))) url [["service" (:service validation)]
                                                                     ["method" (:method validation)]]))

(defn named-arguments? [validation]
  (boolean
   (when-let [args (:named-arguments validation)]
     (-> args not-empty boolean))))

(defn apply-action [available-actions arg action]
  (if-let [action-fn (get available-actions (keyword action))]
    (action-fn arg)
    arg))

(defn apply-actions [available-actions arg actions]
  (reduce (fn [r action] (apply-action available-actions r action)) arg actions))

(defn before-validate [available-actions arg {actions' :before-validate :as validation}]
  (let [actions (filterv not-empty actions')]
    (if (empty? actions)
      arg
      (apply-actions available-actions arg actions))))

(defn single-argument-value [db form-id validation field-value]
  "If single argument is not explicitly defined and there are no named arguments, returns field-value"
  (if-let [arg (-> validation :single-argument keyword)]
    (cf.logic/form-by-id-field-editing-value db form-id arg)
    (when-not (named-arguments? validation) field-value)))

(defn with-single-argument [url db form-id validation field-value]
  (let [arg               (single-argument-value db form-id validation field-value)
        available-actions {:clear-separators cl/clear-separators}]
    (str url (before-validate available-actions arg validation) (when arg "/"))))

(defn named-argument-value [data field-name]
  (if-let [field-value (get data (keyword field-name))] field-value field-name))


(defn named-arguments->url-params [db form-id named-arguments]
  (let [editing-data (cf.logic/form-by-id-editing-data db form-id)]
    (str/join "&" (map #(str (name (first %)) "=" (named-argument-value editing-data (last %)))
                       named-arguments))))

(defn with-named-arguments [url db form-id validation]
  "If there are named arguments, append them as url params (i.e. ?param_1=value_1&param_2=value2 ...)"
  (if (named-arguments? validation)
    (str url "?" (named-arguments->url-params db form-id (:named-arguments validation)))
    url))

(defn build-validation-url
  ([db base-url validation field-value]
   (build-validation-url db (:current-form db) base-url validation field-value))
  ([db form-id base-url validation field-value]
   (-> base-url
       (with-single-argument db form-id validation field-value)
       (replace-url-tags validation)
       (with-named-arguments db form-id validation))))

(defn build-service-action-url
  [base-url action]
  (let [[service-name service-method] (str/split action ".")]
    (replace-url-tags base-url {:service service-name :method service-method})))

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
