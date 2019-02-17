(ns metaforms.modules.complex-forms.components.input
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn dropdown-options [options lookup-key lookup-result]
  (map
   (fn [option] [:option {:value (lookup-key option)
                         :key  (lookup-key option)} (lookup-result option)])
   options))

(defn dropdown [{:keys [field-id name label options] :as defs} common-props value]
  (let [lookup-key    (-> defs :lookup-key keyword)
        lookup-result (-> defs :lookup-result keyword)]
    [:select (merge {:class    "form-control"
                     :id       field-id
                     :value    value}
                    common-props)
     (dropdown-options (concat [{lookup-key "" lookup-result (str "-- " label " --")}] options)
                       lookup-key
                       lookup-result)]))

(defn field-def->common-props
  [{:keys [name read-only]} value form-state]
  {:onFocus  #(rf/dispatch [:input-focus name])
   :onBlur   (fn [e] (rf/dispatch [:input-blur name (-> e .-target .-value)]))
   :readOnly (or read-only (not= form-state :edit))})

(defmulti field-def->input
  (fn [field-def value form-state]
    (keyword (-> field-def :field-kind name) (-> field-def :data-type name))))

(defmethod field-def->input :lookup/char [field-def value form-state]
  (dropdown field-def (field-def->common-props field-def value form-state) value))

(defmethod field-def->input :lookup/integer [field-def value form-state]
  (dropdown field-def (field-def->common-props field-def value form-state) value))

(defn field-def->input-params
  [{:keys [id name label read-only]} value form-state]
  {:type        "text"
   :className   "form-control"
   :name        name
   :id          id
   :value       value
   :readOnly    (or read-only (not= form-state :edit))})

(defmethod field-def->input :default [field-def value form-state]
  [:input (merge
           (field-def->input-params field-def value form-state)
           (field-def->common-props field-def value form-state))])

(defn input [field-def form-state]
  (let [field-value @(rf/subscribe [:field-value (:name field-def)])]
    (field-def->input field-def field-value form-state)))
