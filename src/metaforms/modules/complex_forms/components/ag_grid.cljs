(ns metaforms.modules.complex-forms.components.ag-grid
  (:require [ag-grid-react :refer [AgGridReact]]
            [metaforms.common.helpers :as helpers]
            [metaforms.common.logic :as common.logic]
            [metaforms.modules.complex-forms.ag-grid-controller :as grid.controller]
            [re-frame.core :as rf]
            [reagent.core :as r :refer [atom]]))

(defn field-def->ag-grid-def
  [{:keys [label name data-type width visible renderer] read-only? :read-only :as field-def}]
  {:headerName label
   :field      name
   :width      (* 8 width)
   :resizable  true
   :sortable   true
   :filter     true
   :editable   (not read-only?)
   :hide       (not visible)
   :cellEditor "agTextCellEditor"
   ;; :cellRenderer renderer
   })

(defn cell-value-changed [form-id validations e]
  (let [field-name (-> e .-colDef .-field)]
    (helpers/dispatch-n [[:grid-set-pending-flag form-id true]
                         [:grid-set-data-diff
                          form-id
                          (-> e .-data .-__uuid__)
                          (keyword field-name)
                          (.-newValue e)
                          {:validation (get validations (keyword field-name))
                           :field-name field-name
                           :on-success (fn [db response]
                                         (js/console.log response))
                           :on-failure #(js/console.log %2)}]])))

(defn on-cell-focused [form-id state* e]
  (grid.controller/cell-focused-handler state* e)
  (helpers/dispatch-n [[:grid-set-selected-row form-id (.-rowIndex e)]
                       [:grid-rendered-selected-row
                        form-id
                        (-> e .-api .getSelectedRows js->clj first common.logic/str-keys->keywords)]]))

(defn data-grid [{form-id :form-id :as params} & [parent-state*]]
  (let [state*                (or parent-state* (atom {}))
        on-row-data-changed   (fn [soft-refresh? selected-row e]
                                (grid.controller/data-changed-handler selected-row e)
                                (when-not soft-refresh?
                                  (rf/dispatch [:grid-set-pending-flag form-id false])))
        on-cell-value-changed #(cell-value-changed form-id {} %)]
    (r/create-class
     {:display-name
      "ag-data-grid"
      :should-component-update
      (fn [this old-argv new-argv]
        (let [old            (-> (js->clj old-argv) second)
              new            (-> (js->clj new-argv) second)
              old-request-id (-> (js->clj old-argv) second :request-id)
              new-request-id (-> (js->clj new-argv) second :request-id)]
          (or (nil? new-request-id) (:soft-refresh? new) (not= (:request-id old) (:request-id new)))))
      :reagent-render
      (fn [{:keys [form-id data fields-defs soft-refresh? selected-row]}]
        [:div.ag-theme-balham {:style {:height "400px" :width "100%"}}
         [:> AgGridReact {:columnDefs         (map field-def->ag-grid-def fields-defs)
                          :rowData            data
                          :rowSelection       "single"
                          :onGridReady        (fn [e] (reset! state* {:api (.-api e)}))
                          :onRowDataChanged   #(on-row-data-changed soft-refresh? selected-row %)
                          :onCellValueChanged on-cell-value-changed
                          ;; :onCellDoubleClicked #(on-search-select-record (.-rowIndex %))
                          ;; :onCellKeyPress      #(on-cell-key-press on-search-select-record %)
                          ;; #(row-selected-handler on-search-focus-record %)
                          :onCellFocused      #(on-cell-focused form-id state* %)

                          ;; #(grid.logic/cell-focused-handler state* %)
                          }]])})))
