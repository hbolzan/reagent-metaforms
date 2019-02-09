(ns metaforms.modules.complex-forms.components.input
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn dropdown-options [options lookup-key lookup-result]
  (map
   (fn [option] [:option {:value (lookup-key option)
                         :key  (lookup-key option)} (lookup-result option)])
   options))

(defn dropdown [{:keys [field-id label value options on-change] :as defs} default-value]
  (let [lookup-key    (-> defs :lookup-key keyword)
        lookup-result (-> defs :lookup-result keyword)]
    [:select {:class    "form-control"
              :id       field-id
              :value    value
              :onChange on-change}
     (dropdown-options (concat [{lookup-key "" lookup-result (str "-- " label " --")}] options)
                       lookup-key
                       lookup-result)]))


(defmulti field-def->input
  (fn [field-def default-value]
    (keyword (-> field-def :field-kind name) (-> field-def :data-type name))))

(defmethod field-def->input :lookup/char [field-def default-value]
  (dropdown field-def default-value))

(defmethod field-def->input :lookup/integer [field-def default-value]
  (dropdown field-def default-value))

(defn field-def->input-params
  [{:keys [id name label value on-change read-only]} default-value]
  {:type        "text"
   :className   "form-control"
   :name        name
   :id          id
   :placeholder label
   :value       (or value default-value)
   :onChange    (fn [e] (on-change e))
   :readOnly    read-only})

(defmethod field-def->input :default [field-def default-value]
  [:input (field-def->input-params field-def default-value)])

(defn input [field-def default-value]
  (field-def->input field-def default-value))
