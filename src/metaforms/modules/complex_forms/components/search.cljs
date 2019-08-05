(ns metaforms.modules.complex-forms.components.search
  (:require [metaforms.common.dictionary :refer [l]]
            [metaforms.common.helpers :as helpers]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.main.dom-helpers :as dom.helpers]
            [re-frame.core :as rf]
            [re-frame.db :as rdb]
            [react-data-grid :as ReactDataGrid]
            [react-dom :as react-dom]
            [reagent-keybindings.keyboard :as kb]
            [reagent.core :as r]))

(def search-value (dom.helpers/input-by-id-value-fn "search-field"))

(defn on-search-button-click* [on-search-button-click e]
  (on-search-button-click (search-value)))

(defn on-key-down* [on-search-button-click e]
  (let [key (.-key e)]
    (cond
      (helpers/is-key? key :enter) (on-search-button-click* on-search-button-click e)
      (helpers/is-key? key :esc)   (rf/dispatch [:modal-close]))))

(defn on-confirm-button-click [form-id]
  (rf/dispatch
   [:form-search-select-record
    form-id
    (get-in (cf.logic/form-by-id-data @rdb/app-db form-id)
            [:search :selected-row])]))

(defn search-header [on-search-button-click]
  [:div.form-group
   [:label {:for "search-field"} "Search"] ;; TODO: use dictionary
   [:div.input-group.mb-3
    [:input.form-control {:type      "text"
                          :id        "search-field"
                          :onKeyDown (partial on-key-down* on-search-button-click)}]
    [:div.input-group-append
     [:button.btn.btn-primary {:type "button" :on-click (partial on-search-button-click* on-search-button-click)}
      [:i.fa.fa-search]]]]])

(defn grid-th [{:keys [label name width]}]
  [:th {:key (str "th-" name) :style {:min-width (str (* width 8) "px")}} label])

(defn grid-head [fields-defs]
  [:thead {:id "search-thead-id"}
   [:tr
    (doall (map grid-th fields-defs))]])

(defn grid-td [row-index col-index value]
  [:td {:key (str "td-" row-index "-" col-index)} value])

(defn bounding-info [el thead container]
  (let [container-rect (-> container .getBoundingClientRect)
        el-rect        (-> el .getBoundingClientRect)
        thead-rect     (-> thead .getBoundingClientRect)]
    {:container-rect        container-rect
     :container-rect-top    (-> container-rect .-top)
     :container-rect-height (-> container-rect .-height)
     :container-scroll-top  (.-scrollTop container)
     :thead-rect-height     (-> thead-rect .-height)
     :el-rect-top           (-> el-rect .-top)
     :el-rect-height        (-> el-rect .-height)}))

(defn row-visible? [{:keys [container-rect-top container-rect-height el-rect-top thead-rect-height]}]
  (and (> el-rect-top (+ container-rect-top thead-rect-height))
       (< el-rect-top (+ container-rect-top container-rect-height))))

(defn scroll-to-visible! [container {:keys [container-rect-top container-scroll-top el-rect-top]}]
  (dom.helpers/set-scroll-top container (+ container-scroll-top el-rect-top -240)))

(defn grid-tr-ref [selected? el]
  (when (and el selected?)
    (let [thead     (dom.helpers/element-by-id "search-thead-id")
          container (dom.helpers/first-element-by-class-name "tableFixHead")
          boundings (bounding-info el thead container)]
      (when-not (row-visible? boundings)
        (scroll-to-visible! container boundings)))))

(defn grid-tr [fields-defs on-click on-double-click selected? row-index row-data]
  [:tr (merge {:key             (str "tr-row-" row-index)
               :on-click        on-click
               :on-double-click on-double-click
               :ref             #(grid-tr-ref selected? %)}
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

(defn set-row-index [grid-state data set-fn]
  (if-let [current-index (:selected-row-index grid-state)]
    (let [index (set-fn current-index)]
      (if (or (< index 0) (> index (-> data count dec))) current-index index))
    0))

(defn set-row-index!! [grid-state* on-row-selected set-fn]
  (let [data      @(rf/subscribe [:current-form-records])
        row-index (set-row-index @grid-state* data set-fn)]
    (set-row-index! grid-state* row-index nil)
    (when (fn? on-row-selected) (on-row-selected row-index (get data row-index)))))

(defn grid-body [grid-state* form-id fields-defs on-row-selected on-row-double-click data]
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

(defn set-keybindings [grid-state* on-row-selected]
  [:<>
   [kb/kb-action "esc" (fn [] (rf/dispatch [:modal-close]))]
   [kb/kb-action "up" (fn [] (set-row-index!! grid-state* on-row-selected dec))]
   [kb/kb-action "down" (fn [] (set-row-index!! grid-state* on-row-selected inc))]])

(defn grid-render
  [grid-state* form-id fields-defs on-search-button-click on-row-double-click on-row-selected]
  (let [data @(rf/subscribe [:current-form-records])]
    [:div {:style {:min-height "100%"}}
     (set-keybindings grid-state* on-row-selected)
     [:div.row
      [:div.col-md-12
       (search-header on-search-button-click)]]
     [:div.tableFixHead {:style {:max-height (- (-> js/window .-visualViewport .-height) 340)}}
      [:table.table.table-bordered.table-hover
       (grid-head fields-defs)
       (grid-body grid-state* form-id fields-defs on-row-selected on-row-double-click data)]]]))

(defn search-grid
  [form-id fields-defs row-index entrypoint-id on-search-button-click on-row-double-click on-row-selected]
  (let [grid-state* (r/atom {})]
    (r/create-class
     {:display-name   "search-grid"
      :reagent-render (fn [form-id
                           fields-defs
                           row-index
                           entrypoint-id
                           on-search-button-click
                           on-row-double-click
                           on-row-selected]
                        (set-row-index! grid-state* row-index entrypoint-id)
                        (grid-render grid-state*
                                     form-id
                                     fields-defs
                                     on-search-button-click
                                     on-row-double-click
                                     on-row-selected))})))

(defn start
  [db form-id]
  {:dispatch
   [:show-modal-window
    (l :common/search)
    [search-grid
     form-id
     (cf.logic/fields-defs db)
     (cf.logic/form-by-id-current-record-index db form-id)
     (random-uuid)
     (fn [search-value] (rf/dispatch [:search-button-click form-id search-value]))
     (fn [row-index selected-object] (rf/dispatch [:form-search-select-record form-id row-index]))
     (fn [row-index row-data] (rf/dispatch [:search-grid-select-row form-id row-index]))]
    #(on-confirm-button-click form-id)]})
