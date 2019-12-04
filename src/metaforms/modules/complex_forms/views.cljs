(ns metaforms.modules.complex-forms.views
  (:require [re-frame.core :as rf]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.components.form :as complex.form]
            [metaforms.modules.complex-forms.view-logic :as view-logic]))

(defn fields-defs-with-id [form-id fields-defs]
  (mapv #(assoc % :id (str (name form-id) "-" (:name %))) fields-defs))

(defn index [{:keys [id fields-defs] :as form-definition}]
  (let [fields-defs (filterv :visible (fields-defs-with-id id fields-defs))]
    (complex.form/form
     (merge form-definition
            {:fields-defs fields-defs
             :rows-defs   (view-logic/distribute-fields-by-page fields-defs view-logic/bootstrap-md-width)}))))

(defn generic-view []
  (let [current-form @(rf/subscribe [:current-form])
        {form-definition :definition} current-form]
    (index form-definition)))
