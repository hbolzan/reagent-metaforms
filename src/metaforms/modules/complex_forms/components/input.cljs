(ns metaforms.modules.complex-forms.components.input
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.modules.complex-forms.components.dropdown :as dropdown]))

(defn state-changing-to-edit? [local-state form-state]
  (and (= form-state :edit) (not= (:state local-state) :edit)))

(defn outer-value-changed-when-not-editing? [new-value local-state form-state]
  (and (not= (:state local-state) :edit) (not= new-value (:value local-state))))
  ;; (and (not= form-state :edit) (not= new-value (:value local-state))))

(defn update-value [new-value local-state form-state last-modified-field outer-source-value]
  "May update value if changing state to :edit
   or outer value changed when not editing
   otherwise, only state and last-modified-value are updated"
  (let [update-value?        (or (state-changing-to-edit? local-state form-state)
                                 (outer-value-changed-when-not-editing? new-value local-state form-state))
        last-modified-field' (if last-modified-field last-modified-field (:last-modified-field local-state))]
    (merge local-state {:value               (if update-value? new-value (:value local-state))
                        :state               form-state
                        :last-modified-field last-modified-field'})))

(defn update-state! [new-state local-state]
  (reset! local-state new-state))

(defn do-update-state!
  [new-value local-state form-state last-modified-field outer-source-value]
  (-> new-value
      (update-value @local-state form-state last-modified-field outer-source-value)
      (update-state! local-state)))

(defn update-value! [new-value local-state]
  (update-state! (assoc @local-state :value new-value) local-state))

(defn field-def->common-props
  [{:keys [name read-only]} local-state form-state]
  {:onBlur    (fn [e] (rf/dispatch [:input-blur name (-> e .-target .-value)]))
   :on-change (fn [e]
                (let [value (-> e .-target .-value)]
                  (update-value! value local-state)
                  (rf/dispatch [:field-value-changed name value])))
   :readOnly  (or read-only (not= form-state :edit))})

(defmulti field-def->input
  (fn [field-def value form-state]
    (keyword (-> field-def :field-kind name) (-> field-def :data-type name))))

(defmethod field-def->input :lookup/char [field-def local-state form-state]
  [dropdown/dropdown field-def (field-def->common-props field-def local-state form-state) local-state])

(defmethod field-def->input :lookup/integer [field-def local-state form-state]
  [dropdown/dropdown field-def (field-def->common-props field-def local-state form-state) local-state])

(defn field-def->input-params
  [{:keys [id name label read-only]} local-state form-state]
  (let [viewing? (not= form-state :edit)]
    {:type        "text"
     :className   "form-control"
     :name        name
     :id          id
     :value       (:value @local-state)
     :readOnly    (or read-only viewing?)}))

(defmethod field-def->input :default [field-def local-state form-state]
  [:input (merge
           (field-def->input-params field-def local-state form-state)
           (field-def->common-props field-def local-state form-state))])

(defn filter-source-field [field-def]
  (let [lookup-filter (-> field-def :lookup-filter str/trim)
        filter-args   (if (not (empty? lookup-filter)) (str/split lookup-filter ";") [])]
    (first filter-args)))

(defn calc-last-modified-field
  [last-modified-field filter-source-field outer-source-value form-state local-state]
  (if (not filter-source-field)
    last-modified-field
    (if (= form-state :view)
      (if (or
           (not= (:name last-modified-field) filter-source-field)
           (= (:value last-modified-field) outer-source-value))
        last-modified-field
        {:name filter-source-field :value outer-source-value}
        #_(assoc last-modified-field :value outer-source-value))
      last-modified-field)))

(defn input [field-def form-state all-defs]
  (let [local-state (r/atom {:value "" :state form-state :last-modified-field nil})]
    (fn [field-def form-state]
      (let [outer-value         @(rf/subscribe [:field-value (:name field-def)])
            filter-source-field (filter-source-field field-def)
            outer-source-value (when filter-source-field @(rf/subscribe [:field-value filter-source-field]))
            last-modified-field (calc-last-modified-field @(rf/subscribe [:last-modified-field])
                                                          filter-source-field
                                                          outer-source-value
                                                          form-state
                                                          @local-state)]
        (do-update-state! outer-value local-state form-state last-modified-field outer-source-value)
        (field-def->input (assoc field-def :filter-source-value outer-source-value) local-state form-state)))))
