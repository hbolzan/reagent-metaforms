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

(defn selected-node [api selected-row]
  (let [nodes      (grid.api-helpers/all-nodes api)
        first-node (first nodes)]
    (if (nil? selected-row)
      first-node
      (nth nodes selected-row first-node))))

(defn data-changed-handler
  ([e] (data-changed-handler nil e))
  ([selected-row e]
   (let [api (.-api e)]
     (grid.api-helpers/select-node api (selected-node api selected-row) nil))))

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
