(ns metaforms.modules.complex-forms.validation-logic
  (:require [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.logic :as cf.logic]))

(defn base-validation-url [base-url validation]
  (map #(cl/replace-tag base-url (first %) (last %)) [["service" (:service validation)]
                                                   ["method" (:method validation)]]))

(defn no-arguments-defined? [validation]
  (or (empty? (:single-argument validation)) (empty? (:named-arguments validation))))

(defn single-argument [db validation field-value]
  (if (no-arguments-defined? validation) field-value
      (cf.logic/current-form-field-value db (:single-argument validation))))

(defn build-validation-url [db base-url validation field-value]
  ; 
  )
