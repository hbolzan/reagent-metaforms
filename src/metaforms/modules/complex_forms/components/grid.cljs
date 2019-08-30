(ns metaforms.modules.complex-forms.components.grid
  (:require [clojure.string :as str]
            [goog.events :as events]
            [goog.i18n.NumberFormat.Format]
            [goog.object :as gobj]
            [metaforms.common.helpers :as helpers]
            [metaforms.common.logic :as cl]
            [metaforms.components.cards :as cards]
            [metaforms.components.grid :as rt]
            [metaforms.modules.complex-forms.components.dropdown :as dropdown]
            [metaforms.modules.complex-forms.validation-logic :as vl]
            [re-frame.core :as rf]
            [reagent.core :as r :refer [atom]])
  (:import
   (goog.i18n NumberFormat)
   (goog.i18n.NumberFormat Format)))

(def nff (NumberFormat. Format/DECIMAL))

(defn format-number
  [num]
  (.format nff (str num)))

(defn- row-key-fn
  "Return the reagent row key for the given row"
  [row row-num]
  (str "row-" (:__uuid__ row)))

(defn- cell-data
  [row render-info]
  (try
    (-> render-info :name row)
    (catch js/Object e (js/console.log e))))

(defn- cell-value-changed [e]
  (let [final-value (-> e .-target .-value)]
    (when (not= final-value (gobj/get e :initial-value))
      final-value)))

(defn row-input-elements [column-model row-num]
  (reduce (fn [elements col-def]
            (assoc elements (-> col-def :name keyword)
                   (js/document.getElementById (str "id-" (-> col-def :name name) "-" row-num))))
          {}
          column-model))

(defn apply-row-values [row-values input-elements]
  (doseq [[field value] row-values]
    (let [el (get input-elements field)]
      (set! (.-value el) (first value)))))

(defn- cell-blur
  [form-id column-model {{validation :validation} :field-def name :name :as render-info} row row-num e]
  (when-let [final-value (if (:outer? e) (:value e) (cell-value-changed e))]
    (rf/dispatch [:grid-set-data-diff
                  form-id
                  (:__uuid__ row)
                  name
                  final-value
                  {:validation validation
                   :field-name name
                   :on-success (fn [db response]
                                 (apply-row-values
                                  (vl/expected-results->fields validation response)
                                  (row-input-elements column-model row-num)))
                   :on-failure #(js/console.log %2)}])))

(defn render-info->common-props
  [form-id
   column-model
   state-atom
   {{read-only? :read-only default :default validation :validation :as field-def} :field-def :as render-info}
   row
   row-num]
  (-> {:id             (str "id-" (:name field-def) "-" row-num)
       :data-field-key (-> field-def :name keyword)
       :onFocus        (fn [e]
                         (gobj/set e :initial-value (-> e .-target .-value))
                         (swap! state-atom assoc :selected-row row-num)
                         (rf/dispatch [:grid-set-selected-row form-id row-num]))
       :onBlur         #(cell-blur form-id column-model render-info row row-num %)
       :defaultValue   (or (cell-data row render-info) default)
       :readOnly       read-only?}))

(defmulti cell-input
  (fn [form-id column-model state-atom {field-def :field-def} row row-num col-num]
    (if (-> field-def :mask empty?)
      (case (keyword (-> field-def :field-kind) (-> field-def :data-type))
        :lookup/integer :dropdown
        :lookup/char    :dropdown
        :yes-no/char    :checkbox
        :data/char      :text
        :data/integer   :number
        :data/date      :date
        :data/memo      :memo)
      :masked-input)))

(defmethod cell-input :memo [form-id column-model state-atom render-info row row-num col-num]
  [:div.form-group
   [:textarea.form-control
    (assoc (render-info->common-props form-id column-model state-atom render-info row row-num) :rows 4)]])

(defmethod cell-input :dropdown
  [form-id
   column-model
   state-atom
   {{:keys [lookup-key lookup-result options]} :lookup-info label :header :as render-info}
   row
   row-num
   col-num]
  [:div.form-group
   [:select.form-control
    (render-info->common-props form-id column-model state-atom render-info row row-num)
    (dropdown/dropdown-options options label lookup-key lookup-result)]])

(defmethod cell-input :default [form-id column-model state-atom render-info row row-num col-num]
  [:div.form-group
   [:input.form-control
    (assoc (render-info->common-props form-id column-model state-atom render-info row row-num) :type "text")]])

(defn- cell-fn
  [form-id
   column-model
   state-atom
   {{read-only? :read-only default :default :as field-def} :field-def :as render-info}
   row
   row-num
   col-num]
  (cell-input form-id column-model state-atom render-info row row-num col-num))

(defn date?
  "Returns true if the argument is a date, false otherwise."
  [d]
  (instance? js/Date d))

(defn date-as-sortable
  "Returns something that can be used to order dates."
  [d]
  (.getTime d))

(defn compare-vals
  "A comparator that works for the various types found in table structures.
  This is a limited implementation that expects the arguments to be of
  the same type. The :else case is to call compare, which will throw
  if the arguments are not comparable to each other or give undefined
  results otherwise.

  Both arguments can be a vector, in which case they must be of equal
  length and each element is compared in turn."
  [x y]
  (cond
    (and (vector? x)
         (vector? y)
         (= (count x) (count y)))
    (reduce #(let [r (compare (first %2) (second %2))]
               (if (not= r 0)
                 (reduced r)
                 r))
            0
            (map vector x y))

    (or (and (number? x) (number? y))
        (and (string? x) (string? y))
        (and (boolean? x) (boolean? y)))
    (compare x y)

    (and (date? x) (date? y))
    (compare (date-as-sortable x) (date-as-sortable y))

    :else ;; hope for the best... are there any other possiblities?
    (compare x y)))

(defn- sort-fn
  "Generic sort function for tabular data. Sort rows using data resolved from
  the specified columns in the column model."
  [rows column-model sorting]
  (sort (fn [row-x row-y]
          (reduce
            (fn [_ sort]
              (let [column (column-model (first sort))
                    direction (second sort)
                    cell-x (cell-data row-x column)
                    cell-y (cell-data row-y column)
                    compared (if (= direction :asc)
                               (compare-vals cell-x cell-y)
                               (compare-vals cell-y cell-x))]
                (when-not (zero? compared)
                  (reduced compared))
                ))
            0
            sorting))
        rows))

(defn child-grid [grid-params]
  (let [table-data  (r/atom [])
        table-state (atom {:draggable    true
                           :selected-row 0})]
    (r/create-class
     {:display-name            "child-grid"
      :should-component-update (fn [this old-argv new-argv]
                                 (or (-> old-argv last :soft-refresh?)
                                     (not= (-> old-argv last :request-id) (-> new-argv last :request-id))))
      :reagent-render
      (fn [{form-id         :form-id
            form-def        :form-def
            column-model    :column-model
            data            :data
            request-id      :request-id
            on-request-data :on-request-data
            soft-refresh?   :soft-refresh?}]
        (when-not soft-refresh?
          (reset! table-data (mapv #(assoc % :__uuid__ (random-uuid)) data))
          (swap! table-state assoc :selected-row 0))
        (helpers/dispatch-n (into [[:grid-clear-data-diff form-id]
                                   [:grid-set-data-atom form-id table-data]
                                   [:grid-set-state-atom form-id table-state]]
                                  (when-not soft-refresh? [[:grid-set-pending-flag form-id false]
                                                           [:grid-clear-deleted-rows form-id]
                                                           [:grid-set-selected-row form-id 0]])))
        (let [pk-fields (->> form-def :definition :pk-fields (mapv keyword))]
          [rt/reagent-table
           table-data
           form-id
           {:table           {:class "table table-hover table-striped table-bordered table-transition"
                              :style {:border-spacing 0}}
            :table-container {:style {:border "1px solid gray"}}
            :th              {:style {:border           "1px solid #f2f2f2"
                                      :background-color "silver"}}
            :table-state     table-state
            :scroll-height   "350px"
            :key-scroll      {" " nil}
            :column-model    column-model
            :row-key         row-key-fn
            :render-cell     (partial cell-fn form-id column-model table-state)
            :sort            sort-fn
            ;; :column-selection {:ul {:li {:class "btn"}}}
            }]))})))
