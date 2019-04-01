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

(defn single-argument [db validation field-value]
  (if (no-arguments-defined? validation)
    field-value
    (cl/log (cf.logic/current-form-field-value db (-> validation :single-argument keyword)))))

(defn with-single-argument [url db validation field-value]
  (if-let [single-argument (single-argument db validation field-value)]
    (str url single-argument "/")
    url))

(defn named-argument [db field-name]
  (if-let [field-value (cf.logic/current-form-field-value db (keyword field-name))]
    field-value
    field-name))

(defn named-arguments->url-params [db named-arguments]
  (str/join "&" (map #(str (name (first %)) "=" (named-argument db (last %)))
                     named-arguments)))

(defn with-named-arguments [url db validation]
  (if (no-named-arguments? validation)
    url
    (str url "?" (named-arguments->url-params db (:named-arguments validation)))))

(defn build-validation-url [db base-url validation field-value]
  (-> base-url
      (with-single-argument db validation field-value)
      (replace-url-tags validation)
      (with-named-arguments db validation)))
