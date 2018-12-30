(ns metaforms.modules.complex-forms.views
  (:require [re-frame.core :as rf]
            [metaforms.modules.complex-forms.components.form :as complex.form]
            [metaforms.modules.complex-forms.logic :as cf-l]))

(defn fields-defs-with-id [form-id fields-defs]
  (map #(assoc % :id (str (name form-id) "-" (:name %))) fields-defs))

(defn index [form-definition data]
  (let [fields-defs (fields-defs-with-id (:id form-definition)
                                         (:fields-defs form-definition))]
    (complex.form/form
     (merge form-definition
            {:data        data
             :fields-defs fields-defs
             :rows-defs   (cf-l/distribute-fields fields-defs cf-l/bootstrap-md-width)}))))

(defn generic-view []
  (let [form-definition @(rf/subscribe [:current-form])]
    (index form-definition [])
    ))
