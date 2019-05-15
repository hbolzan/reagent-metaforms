(ns metaforms.modules.complex-forms.components.search
  (:require [reagent.core :as r]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [react-data-grid :as ReactDataGrid]
            [react-dom :as react-dom]))

(defn fields-defs->data-grid-cols [fields-defs]
  (mapv (fn [field-def] {:key (:name field-def) :name (:label field-def)}) fields-defs))

(defn search-header []
  [:div.form-group
   [:label {:for "search-field"} "Search"] ;; TODO: use dictionary
   [:div.input-group.mb-3
    [:input.form-control {:type "text" :id "search-field"}]
    [:div.input-group-append
     [:button.btn.btn-primary {:type "button"}
      [:i.fa.fa-search]]]]])

(defn data-grid [data fields-defs on-row-double-click on-cell-selected]
  [:div {:style {:min-height "100%"}}
   [:div.row
    [:div.col-md-12
     (search-header)]]
   [:> ReactDataGrid {:columns          (fields-defs->data-grid-cols fields-defs)
                      :rowGetter        #(get (clj->js data) %)
                      :rowsCount        (count data)
                      ;; https://github.com/adazzle/react-data-grid/issues/736
                      :minHeight        (- (-> js/window .-visualViewport .-height) 340)
                      :onRowDoubleClick on-row-double-click
                      :onCellSelected   on-cell-selected
                      }]])
