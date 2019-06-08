(ns metaforms.modules.complex-forms.components.grid
  (:require [clojure.string :as str]
            [goog.events :as events]
            [goog.i18n.NumberFormat.Format]
            [goog.object :as gobj]
            [metaforms.common.logic :as cl]
            [metaforms.components.cards :as cards]
            [metaforms.components.grid :as rt]
            [metaforms.modules.complex-forms.components.toolset :as toolset]
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
  (-> render-info :name row))

(defn- cell-blur
  [data-diff render-info e]
  (let [final-value (-> e .-target .-value)]
    (when (not= final-value (gobj/get e :initial-value))
      (swap! data-diff #(assoc-in % [:__uuid__ (:name render-info)] final-value)))))

(defn- cell-fn
  "Return the cell hiccup form for rendering.
  - render-info the specific column from :column-model
  - row the current row
  - row-num the row number
  - col-num the column number in model coordinates"
  [data-diff render-info row row-num col-num]
  [:div.form-group
   [:input.form-control {:type         "text"
                         :on-focus     (fn [e] (gobj/set e :initial-value (-> e .-target .-value)))
                         :on-blur      #(cell-blur data-diff render-info %)
                         :defaultValue (cell-data row render-info)}]])

(comment
  (def a-test (atom {}))
  (keyword (str 1))
  (get-in ["a" "b"] {"a" {"b" 2}})
  (swap! a-test #(assoc-in % [:a :b] 6))
  (swap! a-test #(assoc-in % [:a :c] 4))
  (swap! a-test #(assoc-in % [:x :y] 10))
  (let [v :y]
    (swap! a-test #(assoc-in % [:x v] 100)))
  )

(assoc-in {:a {:c "y"}} [:a :b] "x")

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
        data-diff   (atom {})
        table-state (atom {:draggable true})]
    (fn [{form-def        :form-def
          column-model    :column-model
          data            :data
          title           :title
          on-request-data :on-request-data}]
      (reset! table-data (mapv #(assoc % :__uuid__ (random-uuid)) data))
      (reset! data-diff {})
      (let [pk-fields (->> form-def :definition :pk-fields (mapv keyword))]
        [cards/card
         ^{:key key}
         title
         (toolset/toolbar {:form-state     :view
                           :action-buttons (dissoc toolset/action-buttons :search)
                           :nav-buttons    toolset/nav-buttons
                           :on-click       (fn [form-id e] (js/console.log form-id e))})
         [:div {:style {:min-height "100%"}}
          [:div.row
           [:div.col-md-12
            [rt/reagent-table table-data {:table            {:class "table table-hover table-striped table-bordered table-transition"
                                                             :style {:border-spacing 0}}
                                          :table-container  {:style {:border "1px solid green"}}
                                          :th               {:style {:border "1px solid white" :background-color "silver"}}
                                          :table-state      table-state
                                          :scroll-height    "350px"
                                          :key-scroll       {" " nil}
                                          :column-model     column-model
                                          :row-key          row-key-fn
                                          :render-cell      (partial cell-fn data-diff)
                                          :sort             sort-fn
                                          :column-selection {:ul {:li {:class "btn"}}}}]]]]]))))