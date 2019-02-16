(ns metaforms.modules.complex-forms.components.input
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn dropdown-options [options lookup-key lookup-result]
  (map
   (fn [option] [:option {:value (lookup-key option)
                         :key  (lookup-key option)} (lookup-result option)])
   options))

(defn dropdown [{:keys [field-id name label options on-change] :as defs} value]
  (let [lookup-key    (-> defs :lookup-key keyword)
        lookup-result (-> defs :lookup-result keyword)]
    [:select {:class    "form-control"
              :id       field-id
              :value    value
              :onFocus  #(rf/dispatch [:input-focus name])
              :onBlur   (fn [e] (rf/dispatch [:input-blur name (-> e .-target .-value)]))}
     (dropdown-options (concat [{lookup-key "" lookup-result (str "-- " label " --")}] options)
                       lookup-key
                       lookup-result)]))

(defmulti field-def->input
  (fn [field-def value form-state]
    (keyword (-> field-def :field-kind name) (-> field-def :data-type name))))

(defmethod field-def->input :lookup/char [field-def value form-state]
  (dropdown field-def value))

(defmethod field-def->input :lookup/integer [field-def value form-state]
  (dropdown field-def value))

(defn field-def->input-params
  [{:keys [id name label on-change read-only]} value form-state]
  {:type        "text"
   :className   "form-control"
   :name        name
   :id          id
   ;; :placeholder label
   :value       value
   ;; :onChange    (fn [e] (rf/dispatch [:input-change (-> e .-target .-value)]))
   :onFocus     #(rf/dispatch [:input-focus name])
   :onBlur      (fn [e] (rf/dispatch [:input-blur name (-> e .-target .-value)]))
   :readOnly    (or read-only (not= form-state :edit))})

(defmethod field-def->input :default [field-def value form-state]
  [:input (field-def->input-params field-def value form-state)])

(defn input [field-def form-state]
  (let [field-value @(rf/subscribe [:field-value (:name field-def)])]
    (field-def->input field-def field-value form-state)))
