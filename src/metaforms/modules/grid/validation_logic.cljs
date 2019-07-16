(ns metaforms.modules.grid.validation-logic
  (:require [clojure.string :as str]
            [metaforms.modules.complex-forms.validation-logic :as vl]))

(defn with-single-argument [url row validation field-value]
  (let [single-argument (or (-> validation :single-argument keyword row) field-value)]
    (str url single-argument "/")))

(defn named-argument [row arg-value]
  (or
   (get row (keyword arg-value))
   arg-value))

(defn named-arguments->url-params [row named-arguments]
  (str/join "&" (map #(str (name (first %)) "=" (named-argument row (last %)))
                     named-arguments)))

(defn with-named-arguments [url row validation]
  (if-let [named-arguments (:named-arguments validation)]
    (str url "?" (named-arguments->url-params row named-arguments))
    url))

(defn build-validation-url [db base-url row validation field-value]
  (-> base-url
      (with-single-argument row validation field-value)
      (vl/replace-url-tags validation)
      (with-named-arguments row validation)))
