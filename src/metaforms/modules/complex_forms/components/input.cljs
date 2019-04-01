(ns metaforms.modules.complex-forms.components.input
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [react-input-mask :as InputElement]
            [metaforms.modules.complex-forms.components.dropdown :as dropdown]
            [metaforms.modules.complex-forms.components.checkbox :as checkbox]))

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

(defn update-state! [new-state local-state*]
  (reset! local-state* new-state))

(defn do-update-state!
  [new-value local-state* form-state last-modified-field outer-source-value]
  (-> new-value
      (update-value @local-state* form-state last-modified-field outer-source-value)
      (update-state! local-state*)))

(defn local-state-set! [local-state* key value]
  (update-state! (assoc @local-state* key value) local-state*))

(defn local-state-get! [local-state* key]
  (get @local-state* key))

(defn update-value! [new-value local-state*]
  (local-state-set! local-state* :value new-value))

(defn merge-common-change [props field-name local-state* common-onchange?]
  (when common-onchange?
    (merge props {:on-change (fn [e]
                         (let [value (-> e .-target .-value)]
                           (update-value! value local-state*)
                           #_(rf/dispatch [:field-value-changed field-name value])))})))

(defn value-changed? [local-state* new-value]
  (not= (local-state-get! local-state* :initial-value) new-value))

(defn common-on-blur [local-state* field-name validation event]
  (let [new-value (-> event .-target .-value)]
    ;; TODO: do this only if field is valid
    (rf/dispatch [:input-blur field-name new-value])
    ;; TODO: abort event bubbling if not valid
    (when (and validation (not-empty new-value) (value-changed? local-state* new-value))
      (rf/dispatch [:validate-field validation field-name new-value]))))

(defn field-def->common-props
  ([field-def local-state* form-state]
   (field-def->common-props field-def local-state* form-state true))
  ([{:keys [name read-only validation]} local-state* form-state common-onchange?]
   (-> {:onFocus  (fn [e] (local-state-set! local-state*
                                           :initial-value
                                           (-> e .-target .-value)))
        ;; :onBlur   (fn [e] (rf/dispatch [:input-blur name (-> e .-target .-value)]))
        :onBlur   (fn [e] (common-on-blur local-state* name validation e))
        :readOnly (or read-only (not= form-state :edit))}
       (merge-common-change name local-state* common-onchange?))))

(defmulti field-def->input
  (fn [field-def local-state* form-state]
    (keyword (-> field-def :field-kind name) (-> field-def :data-type name))))

(defmethod field-def->input :yes-no/char [field-def local-state* form-state]
  (checkbox/yes-no field-def (field-def->common-props field-def local-state* form-state false) local-state*))

(defmethod field-def->input :lookup/char [field-def local-state* form-state]
  [dropdown/dropdown field-def (field-def->common-props field-def local-state* form-state) local-state*])

(defmethod field-def->input :lookup/integer [field-def local-state* form-state]
  [dropdown/dropdown field-def (field-def->common-props field-def local-state* form-state) local-state*])

(defn field-def->input-params
  [{:keys [id name label read-only mask mask-char format-chars]} local-state form-state]
  (let [viewing? (not= form-state :edit)]
    {:type         "text"
     :className    "form-control"
     :name         name
     :mask         mask
     :mask-char    mask-char
     :format-chars format-chars
     :id           id
     :value        (:value @local-state)
     :readOnly     (or read-only viewing?)}))

(defmethod field-def->input :default [field-def local-state form-state]
  [:> InputElement (merge
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
  (let [local-state* (r/atom {:value "" :state form-state :last-modified-field nil})]
    (fn [field-def form-state]
      (let [outer-value         @(rf/subscribe [:field-value (:name field-def)])
            filter-source-field (filter-source-field field-def)
            outer-source-value (when filter-source-field @(rf/subscribe [:field-value filter-source-field]))
            last-modified-field (calc-last-modified-field @(rf/subscribe [:last-modified-field])
                                                          filter-source-field
                                                          outer-source-value
                                                          form-state
                                                          @local-state*)]
        (do-update-state! outer-value local-state* form-state last-modified-field outer-source-value)
        (field-def->input (assoc field-def :filter-source-value outer-source-value) local-state* form-state)))))
