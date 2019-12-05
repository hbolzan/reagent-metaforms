(ns metaforms.modules.complex-forms.components.ag-grid
  (:require [ag-grid-react :refer [AgGridReact]]
            [re-frame.core :as rf]
            [reagent.core :as r :refer [atom]]))

(defn field-def->ag-grid-def
  [{:keys [label name data-type width visible renderer] read-only? :read-only :as field-def}]
  {:headerName   label
   :field        name
   :width        (* 8 width)
   :resizable    true
   :sortable     true
   :filter       true
   :editable     (not read-only?)
   :hide         (not visible)
   ;; :cellRenderer renderer
   })

(defn data-grid [params]
  (let [state* (atom {})]
    (r/create-class
     {:display-name
      "ag-data-grid"
      :reagent-render
      (fn [{:keys [data fields-defs]}]
        [:div.ag-theme-balham {:style {:height "400px" :width "100%"}}
         [:> AgGridReact {:columnDefs   (map field-def->ag-grid-def fields-defs)
                          :rowData      data
                          :rowSelection "single"
                          :onGridReady  (fn [e] (reset! state* {:api (.-api e)}))
                          ;; :onRowDataChanged    data-changed-handler
                          ;; :onCellDoubleClicked #(on-search-select-record (.-rowIndex %))
                          ;; :onCellKeyPress      #(on-cell-key-press on-search-select-record %)
                          ;; :onRowSelected       #(row-selected-handler on-search-focus-record %)
                          ;; :onCellFocused       #(cell-focused-handler state* %)
                          }]])})))
