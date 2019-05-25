(ns metaforms.modules.complex-forms.components.search
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [react-data-grid :as ReactDataGrid]
            [react-dom :as react-dom]
            [metaforms.common.helpers :as helpers]
            [metaforms.modules.main.dom-helpers :as dom.helpers]))

(defn fields-defs->data-grid-cols [fields-defs]
  (mapv (fn [field-def] {:key (:name field-def) :name (:label field-def)}) fields-defs))

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
