(ns metaforms.modules.complex-forms.components.search
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [react-data-grid :as ReactDataGrid]
            [react-dom :as react-dom]))

(defn fields-defs->data-grid-cols [fields-defs]
  (mapv (fn [field-def] {:key (:name field-def) :name (:label field-def)}) fields-defs))

(defn search-value []
  (-> (.getElementById js/document "search-field") .-value))

(defn search-header [on-search-button-click]
  [:div.form-group
   [:label {:for "search-field"} "Search"] ;; TODO: use dictionary
   [:div.input-group.mb-3
    [:input.form-control {:type "text" :id "search-field"}]
    [:div.input-group-append
     [:button.btn.btn-primary {:type "button" :on-click #(on-search-button-click (search-value))}
      [:i.fa.fa-search]]]]])

(defn data-grid [fields-defs on-search-button-click on-row-double-click on-cell-selected]
  (let [data  @(rf/subscribe [:current-form-records])]
    [:div {:style {:min-height "100%"}}
     [:div.row
      [:div.col-md-12
       (search-header on-search-button-click)]]
     [:> ReactDataGrid {:columns          (fields-defs->data-grid-cols fields-defs)
                        :rowGetter        #(get (clj->js data) %)
                        :rowsCount        (count data)
                        ;; https://github.com/adazzle/react-data-grid/issues/736
                        :minHeight        (- (-> js/window .-visualViewport .-height) 340)
                        :onRowDoubleClick on-row-double-click
                        :onCellSelected   on-cell-selected
                        }]]))
