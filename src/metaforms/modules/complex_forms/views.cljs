(ns metaforms.modules.complex-forms.views
  (:require [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.components.form :as complex.form]
            [metaforms.modules.complex-forms.view-logic :as view-logic]
            [re-frame.core :as rf]))

(defn fields-defs-with-id [form-id fields-defs]
  (mapv #(assoc % :id (str (name form-id) "-" (:name %))) fields-defs))

(defn index [{:keys [id fields-defs] :as form-definition}]
  (let [fields-defs (fields-defs-with-id id (filterv :visible fields-defs))]
    (complex.form/form
     (merge form-definition
            {:fields-defs fields-defs
             :rows-defs   (view-logic/distribute-fields-by-page fields-defs view-logic/bootstrap-md-width)}))))

(defn generic-view []
  (let [current-form @(rf/subscribe [:current-form])
        {form-definition :definition} current-form]
    (index form-definition)))
