(ns metaforms.modules.complex-forms.components.search
  (:require [reagent.core :as r]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [react-data-grid :as ReactDataGrid]
            [react-dom :as react-dom]))

(defn fields-defs->data-grid-cols [fields-defs]
  (mapv (fn [field-def] {:key (:name field-def) :name (:label field-def)}) fields-defs))

(defn field-def->td [data-row field-def]
  [:td {:key (random-uuid)} (-> field-def :name keyword data-row)])

(defn data-row->table-row [field-defs data-row]
  [:tr {:key (random-uuid)}
   (mapv #(field-def->td data-row %) field-defs)])

(defn data-table-header [fields-defs]
  [:thead
   [:tr {:key (random-uuid)}
    (mapv (fn [field-def] [:th {:key (random-uuid)} (:label field-def)]) fields-defs)]])

#_(defn data-grid [data fields-defs]
  (r/create-class
   {:dislay-name "data-grid"

    :component-did-mount
    (fn [this] (js/console.log "did-mount" (-> js/document (.getElementById "main-modal-body") .-offsetHeight)))

    :component-did-update
    (fn [this] (js/console.log "did-update" (react-dom/findDOMNode this)))

    :reagent-render
    (fn [data fields-defs]
      [:div
       [:> ReactDataGrid {:columns   (fields-defs->data-grid-cols fields-defs)
                          :rowGetter #(get (clj->js data) %)
                          :rowsCount (count fields-defs)
                          :minHeight 150}]])}))

(defn data-grid [data fields-defs]
  [:div {:style {:min-height "100%"}}
   [:> ReactDataGrid {:columns   (fields-defs->data-grid-cols fields-defs)
                      :rowGetter #(get (clj->js data) %)
                      :rowsCount (count fields-defs)
                      ;; https://github.com/adazzle/react-data-grid/issues/736
                      :minHeight (- (-> js/window .-visualViewport .-height) 250)}]])

(defn data-table [data fields-defs]
  [:div.container-fluid
   [:div.row
    [:div.col-md-12
     [:table.table.table-responsive-sm.table-bordered.table-striped.table-sm
      (data-table-header fields-defs)
      [:tbody
       (mapv #(data-row->table-row fields-defs %) data)]]]]])
