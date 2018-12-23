(ns metaforms.modules.complex-forms.components.input
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn dropdown [{:keys [field-id label value options on-change]}]
  [:select {:class    "form-control"
            :id       field-id
            :value    value
            :onChange on-change}
   [:option {:value ""} (str "-- " label " --")]
   (map
    (fn [option] [:option {:value (first option)
                          :key   (first option)} (last option)])
    options)])

(defmulti field-def->input
  (fn [field-def] (keyword (-> field-def :field-kind name) (-> field-def :data-type name))))

(defmethod field-def->input :lookup/char [field-def]
  (dropdown field-def))

(defmethod field-def->input :lookup/integer [field-def]
  (dropdown field-def))

(defn field-def->input-params
  [{:keys [id name label value on-change read-only]}]
  {:type        "text"
   :className   "form-control"
   :name        name
   :id          id
   :placeholder label
   :value       value
   :onChange    (fn [e] (on-change e))
   :readOnly    read-only})

(defmethod field-def->input :default [field-def _]
  [:input (field-def->input-params field-def)])

(defn input [field-def]
  (field-def->input field-def))
