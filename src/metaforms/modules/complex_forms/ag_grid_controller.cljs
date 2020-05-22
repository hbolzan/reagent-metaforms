(ns metaforms.modules.complex-forms.ag-grid-controller
  (:require [clojure.string :as str]
            [metaforms.common.logic :as cl]
            [metaforms.modules.grid.api-helpers :as grid.api-helpers]
            [re-frame.core :as rf]))

(defn cell-focused-handler [state* e]
  (let [api           (.-api e)
        new-index     (.-rowIndex e)
        selected?     (= (:row-index @state*) new-index)]
    (when (not selected?)
      (grid.api-helpers/select-row-by-index! state* api new-index (-> e .-column .-colId)))))

(defn data-changed-handler [e]
  (let [api        (.-api e)
        first-node (first (grid.api-helpers/all-nodes api))]
    (grid.api-helpers/select-node api first-node nil)))

(defn get-row-node[api row]
  (when api
    (.getRowNode api row)))

(defn set-row-node-value[row-node field-name value]
  (.setDataValue row-node field-name value))

(defn set-modified-linked-field [api form-id row modified-data]
  (rf/dispatch-sync [:reset-last-modified-linked-field form-id])
  (set-row-node-value (get-row-node api row)
                      (last (str/split (:name modified-data) "."))
                      (:value modified-data)))
