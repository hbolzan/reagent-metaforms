(ns metaforms.modules.complex-forms.components.input
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn dropdown-options [options lookup-key lookup-result]
  (map
   (fn [option] [:option {:value (lookup-key option)
                         :key  (lookup-key option)} (lookup-result option)])
   options))

(defn dropdown [{:keys [field-id label options on-change] :as defs} value]
  (let [lookup-key    (-> defs :lookup-key keyword)
        lookup-result (-> defs :lookup-result keyword)]
    [:select {:class    "form-control"
              :id       field-id
              :value    (or value "")
              :onChange on-change}
     (dropdown-options (concat [{lookup-key "" lookup-result (str "-- " label " --")}] options)
                       lookup-key
                       lookup-result)]))

(defmulti field-def->input
  (fn [field-def current-value editing-value]
    (keyword (-> field-def :field-kind name) (-> field-def :data-type name))))

(defmethod field-def->input :lookup/char [field-def current-value editing-value]
  (dropdown field-def (or editing-value current-value)))

(defmethod field-def->input :lookup/integer [field-def current-value editing-value]
  (dropdown field-def (or editing-value current-value)))

(defn field-def->input-params
  [{:keys [id name label on-change read-only]} current-value editing-value]
  {:type        "text"
   :className   "form-control"
   :name        name
   :id          id
   :placeholder label
   :value       (or editing-value current-value)
   :onChange    (fn [e] (on-change e))
   :readOnly    read-only})

(defmethod field-def->input :default [field-def current-value editing-value]
  [:input (field-def->input-params field-def current-value editing-value)])

(defn input [field-def {editing-data :editing-data records :records current-record-id :current-record}]
  (let [field-key     (-> field-def :name keyword)
        editing-value (field-key editing-data)
        current-value (field-key (get records current-record-id))]
    (field-def->input field-def current-value editing-value)))
