(ns metaforms.modules.complex-forms.components.search
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [react-data-grid :as ReactDataGrid]
            [react-dom :as react-dom]
            [metaforms.common.helpers :as helpers]
            [metaforms.modules.main.dom-helpers :as dom.helpers]))

(def search-value (dom.helpers/input-by-id-value-fn "search-field"))

(defn on-search-button-click* [on-search-button-click e]
  (on-search-button-click (search-value)))

(defn on-key-down* [on-search-button-click e]
  (let [key (.-key e)]
    (cond
      (helpers/is-key? key :enter) (on-search-button-click* on-search-button-click e)
      (helpers/is-key? key :esc)   (rf/dispatch [:modal-close]))))

(defn search-header [on-search-button-click]
  [:div.form-group
   [:label {:for "search-field"} "Search"] ;; TODO: use dictionary
   [:div.input-group.mb-3
    [:input.form-control {:type "text" :id "search-field" :onKeyDown (partial on-key-down* on-search-button-click)}]
    [:div.input-group-append
     [:button.btn.btn-primary {:type "button" :on-click (partial on-search-button-click* on-search-button-click)}
      [:i.fa.fa-search]]]]])

(defn grid-th [{:keys [label name width]}]
  [:th {:key (str "th-" name) :style {:min-width (str (* width 8) "px")}} label])

(defn grid-head [fields-defs]
  [:thead
   [:tr
    (doall (map grid-th fields-defs))]])

(defn grid-td [row-index col-index value]
  [:td {:key (str "td-" row-index "-" col-index)} value])

(defn grid-tr [fields-defs on-click on-double-click selected? row-index row-data]
  [:tr (merge {:key             (str "tr-row-" row-index)
               :on-click        on-click
               :on-double-click on-double-click}
              (when selected?
                {:style {:background-color "blue"
                         :color            "white"}}))
   (doall
    (map-indexed
     (fn [i {name :name}] (grid-td row-index i (get row-data (keyword name))))
     fields-defs))])

(defn set-row-index! [grid-state* row-index entrypoint-id]
  (let [current-id (:entrypoint-id @grid-state*)
        id         (or entrypoint-id current-id)]
    (when (or (nil? entrypoint-id) (not= id current-id))
      (swap! grid-state* assoc :selected-row-index row-index :entrypoint-id id))))

(defn grid-body [grid-state* fields-defs on-row-selected on-row-double-click data]
  (let [selected-index (:selected-row-index @grid-state*)
        on-row-click   (fn [row-index row-data e]
                         (when (fn? on-row-selected) (on-row-selected row-index row-data))
                         (set-row-index! grid-state* row-index nil))]
    [:tbody
     (doall
      (map-indexed
       (fn [i row] (grid-tr fields-defs
                            #(on-row-click i row %)
                            #(on-row-double-click i row)
                            (= i selected-index) i row))
       data))]))

(defn grid-render
  [grid-state* fields-defs on-search-button-click on-row-double-click on-row-selected]
  (let [data @(rf/subscribe [:current-form-records])]
    [:div {:style {:min-height "100%"}}
     [:div.row
      [:div.col-md-12
       (search-header on-search-button-click)]]
     [:div.tableFixHead {:style {:max-height (- (-> js/window .-visualViewport .-height) 340)}}
      [:table.table.table-bordered.table-hover
       (grid-head fields-defs)
       (grid-body grid-state* fields-defs on-row-selected on-row-double-click data)]]]))

(defn search-grid
  [fields-defs row-index entrypoint-id on-search-button-click on-row-double-click on-row-selected]
  (let [grid-state* (r/atom {})]
    (r/create-class
     {:display-name   "search-grid"
      :reagent-render (fn [fields-defs row-index entrypoint-id on-search-button-click on-row-double-click on-row-selected]
                        (set-row-index! grid-state* row-index entrypoint-id)
                        (grid-render grid-state*
                                     fields-defs
                                     on-search-button-click
                                     on-row-double-click
                                     on-row-selected))})))
