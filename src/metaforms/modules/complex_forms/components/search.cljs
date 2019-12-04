(ns metaforms.modules.complex-forms.components.search
  (:require [ag-grid-react :refer [AgGridReact]]
            [metaforms.common.dictionary :refer [l]]
            [metaforms.common.helpers :as helpers]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.grid.cell-renderers :as renderers]
            [metaforms.modules.main.dom-helpers :as dom.helpers]
            [re-frame.core :as rf]
            [re-frame.db :as rdb]
            [react-data-grid :as ReactDataGrid]
            [react-dom :as react-dom]
            [reagent-keybindings.keyboard :as kb]
            [reagent.core :as r]))

(def search-field-id  "search-field")

(def search-value (dom.helpers/input-by-id-value-fn search-field-id))

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
   [:label {:for search-field-id} (l :common/search)]
   [:div.input-group.mb-3
    [:input.form-control {:type      "text"
                          :id        search-field-id
                          :onKeyDown (partial on-key-down* on-search-button-click)}]
    [:div.input-group-append
     [:button.btn.btn-primary {:type "button" :on-click (partial on-search-button-click* on-search-button-click)}
      [:i.fa.fa-search]]]]])

(defn field-def->ag-grid-def
  [{:keys [label name data-type search-visible renderer additional-params] :as field-def}]
  (let [width (or (:alt-width additional-params) (:width field-def))]
    {:headerName   label
     :field        name
     :width        (* 8 width)
     :resizable    true
     :sortable     true
     :filter       true
     :hide         (not search-visible)
     :cellRenderer renderer}))

(defn fields-defs->ag-grid-defs [fields-defs]
  (map field-def->ag-grid-def fields-defs))

(defn on-cell-key-press [on-search-select-record e]
  (let [event    (.-event e)
        key-code (.-keyCode event)]
    (when (= key-code 13) (on-search-select-record (.-rowIndex e)))))

(defn all-nodes [api]
  (.-allLeafChildren (.-rootNode (.-rowModel api))))

(defn node-by-index [nodes index]
  (first (filter #(= index (.-rowIndex %)) nodes)))

(defn select-node [api node]
  (when (and api node)
    (.setSelected node true true)
    (.setFocusedCell api
                     (.-rowIndex node)
                     (first (.-allDisplayedColumns (.-columnController node))))))

(defn select-row-by-index [api row-index]
  (when (and api row-index)
    (select-node api (node-by-index (all-nodes api) row-index))))

(defn data-changed-handler [e]
  (let [api        (.-api e)
        first-node (first (all-nodes api))]
    (select-node api first-node)))

(defn row-selected-handler [on-search-focus-record e]
  (let [row-index (.-rowIndex e)
        selected (.-selected (.-node e))]
    (when selected
      (on-search-focus-record row-index))))

(defn cell-focused-handler [state* e]
  (let [api           (.-api e)
        new-index     (.-rowIndex e)
        selected?     (= (:row-index @state*) new-index)]
    (when (not selected?)
      (swap! state* #(assoc % :row-index new-index))
      (select-row-by-index api new-index))))

(defn ag-grid-render [form-id defs height width rows events]
  (let [state* (atom {})]
    (r/create-class
     {:display-name
      "ag-search-grid"
      :reagent-render
      (fn [form-id defs height width rows {:keys [on-search-focus-record
                                                  on-search-select-record]}]
        (let [row-index @(rf/subscribe [:form-by-id-record-index form-id])]
          (select-row-by-index (:api @state*) row-index)
          [:div.ag-theme-balham {:style {:height height :width width}}
           [:> AgGridReact {:columnDefs          defs
                            :rowData             rows
                            :rowSelection        "single"
                            :onGridReady         (fn [e] (reset! state* {:api (.-api e)}))
                            :onCellDoubleClicked #(on-search-select-record (.-rowIndex %))
                            :onCellKeyPress      #(on-cell-key-press on-search-select-record %)
                            :onRowDataChanged    data-changed-handler
                            :onRowSelected       #(row-selected-handler on-search-focus-record %)
                            :onCellFocused       #(cell-focused-handler state* %)}]]))})))

(defn ag-search-grid
  [form-id fields-defs {:keys [on-search-button-click] :as events}]
  (let [height    (str (- (-> js/window .-visualViewport .-height) 340) "px")
        width     (str (- (-> js/window .-visualViewport .-width) 50) "px")
        ag-defs   (fields-defs->ag-grid-defs fields-defs)
        rows      @(rf/subscribe [:current-form-records])]
    [:div {:style {:min-height "100%"}}
     [:div.row
      [:div.col-md-12
       (search-header on-search-button-click)]]
     [:div.row
      [:div.col-md-12
       [ag-grid-render form-id ag-defs height width rows events]]]]))

(defn start
  [db form-id]
  {:dispatch
   [:show-modal-window
    (l :common/search)
    [ag-search-grid
     form-id
     (-> db cf.logic/fields-defs renderers/with-search-renderers)
     {:on-search-button-click  #(rf/dispatch [:search-button-click form-id %])
      :on-search-focus-record  #(rf/dispatch [:search-grid-select-row form-id %])
      :on-search-select-record #(rf/dispatch [:form-search-select-record form-id %])}]
    #(on-confirm-button-click form-id)]})
