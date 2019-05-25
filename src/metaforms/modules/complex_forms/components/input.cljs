(ns metaforms.modules.complex-forms.components.input
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [react-input-mask :as InputElement]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.components.dropdown :as dropdown]
            [metaforms.modules.complex-forms.components.checkbox :as checkbox]))

(defn apply-outer-value? [outer-value local-state form-state]
  "May update value if value changed AND
   * state is changing to :edit
   * or outer value changed when not editing
   * or value is wrapped into a list - this forces to outer value
   otherwise, only state and last-modified-value are updated"
  (let [state-changing-to-edit? (and (= form-state :edit) (not= (:state local-state) :edit))
        editing?                (= (:state local-state) :edit)
        outer-value-changed?    (not= outer-value (:value local-state))
        force-outer-value?      (coll? outer-value)]
    (and outer-value-changed?
         (or (not editing?) state-changing-to-edit? force-outer-value?))))

(defn new-value [outer-value local-state form-state]
  (if (apply-outer-value? outer-value local-state form-state)
    (if (coll? outer-value) (first outer-value) outer-value)
    (:value local-state)))

(defn update-value [outer-value local-state form-state last-modified-field outer-source-value]
  (let [last-modified-field' (if last-modified-field last-modified-field (:last-modified-field local-state))]
    (merge local-state {:value               (new-value outer-value local-state form-state)
                        :state               form-state
                        :last-modified-field last-modified-field'})))

(defn update-state! [new-state local-state*]
  (reset! local-state* new-state))

(defn do-update-state!
  [outer-value local-state* form-state last-modified-field outer-source-value]
  (-> outer-value
      (update-value @local-state* form-state last-modified-field outer-source-value)
      (update-state! local-state*)))

(defn local-state-set! [local-state* key value]
  (update-state! (assoc @local-state* key value) local-state*))

(defn local-state-get! [local-state* key]
  (get @local-state* key))

(defn update-value! [value local-state*]
  (local-state-set! local-state* :value value))

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
    ;; TODO: abort event bubbling if not valid
    (if (and validation (not-empty new-value) (value-changed? local-state* new-value))
      (rf/dispatch [:validate-field validation field-name new-value])
      (rf/dispatch [:input-blur field-name new-value]))))

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
    (if (-> field-def :mask empty?)
      (keyword (-> field-def :field-kind name) (-> field-def :data-type name))
      :masked-input)))

(defmethod field-def->input :yes-no/char [field-def local-state* form-state]
  (checkbox/yes-no field-def (field-def->common-props field-def local-state* form-state false) local-state*))

(defmethod field-def->input :lookup/char [field-def local-state* form-state]
  [dropdown/dropdown field-def (field-def->common-props field-def local-state* form-state) local-state*])

(defmethod field-def->input :lookup/integer [field-def local-state* form-state]
  [dropdown/dropdown field-def (field-def->common-props field-def local-state* form-state) local-state*])

(defn with-mask [params {mask :mask mask-char :mask-char}]
  (if (empty? mask)
    params
    (merge params {:mask         mask
                   :mask-char    mask-char})))

(defn field-def->input-params
  [{:keys [id name label read-only format-chars] :as field-def} local-state form-state]
  (let [viewing? (not= form-state :edit)]
    {:type         "text"
     :className    "form-control"
     :name         name
     :format-chars format-chars
     :id           id
     :value        (:value @local-state)
     :readOnly     (or read-only viewing?)}))


(defmethod field-def->input :masked-input [field-def local-state form-state]
  [:> InputElement (with-mask
                     (merge
                      (field-def->input-params field-def local-state form-state)
                      (field-def->common-props field-def local-state form-state))
                     field-def)])

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
