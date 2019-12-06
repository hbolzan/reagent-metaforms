(ns metaforms.modules.complex-forms.ag-grid-logic
  (:require [metaforms.common.logic :as cl]))

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

(defn cell-focused-handler [state* e]
  (let [api           (.-api e)
        new-index     (.-rowIndex e)
        selected?     (= (:row-index @state*) new-index)]
    (when (not selected?)
      (swap! state* #(assoc % :row-index new-index))
      (select-row-by-index api new-index))))

(defn data-changed-handler [e]
  (let [api        (.-api e)
        first-node (first (all-nodes api))]
    (select-node api first-node)))
