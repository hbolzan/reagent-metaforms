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
  (fn [field-def value]
    (keyword (-> field-def :field-kind name) (-> field-def :data-type name))))

(defmethod field-def->input :lookup/char [field-def value]
  (dropdown field-def value))

(defmethod field-def->input :lookup/integer [field-def value]
  (dropdown field-def value))

(defn field-def->input-params
  [{:keys [id name label on-change read-only]} value]
  {:type        "text"
   :className   "form-control"
   :name        name
   :id          id
   ;; :placeholder label
   :value       value
   ;; :onChange    (fn [e] (rf/dispatch [:input-change (-> e .-target .-value)]))
   :onFocus     #(rf/dispatch [:input-focus name])
   :onBlur      (fn [e] (rf/dispatch [:input-blur name (-> e .-target .-value)]))
   :readOnly    read-only})

(defmethod field-def->input :default [field-def value]
  [:input (field-def->input-params field-def value)])

(defn input [field-def {editing :editing editing-data :editing-data records :records current-record-id :current-record} form-state]
  (let [field-name    (:name field-def)
        field-key     (-> field-name keyword)
        current-value (or (field-key (get records current-record-id)) "")
        editing-value (field-key editing-data)]
    (field-def->input field-def (when-not (= editing field-name) (or editing-value current-value)))))
