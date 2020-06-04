(ns metaforms.modules.grid.api-helpers
  )

(defn node-by-index [nodes index]
  (first (filter #(= index (.-rowIndex %)) nodes)))

(defn all-nodes [api]
  (.-allLeafChildren (.-rootNode (.-rowModel api))))

(defn row-count [api]
  (.getDisplayedRowCount api))

(defn focused-cell [api]
  (.getFocusedCell api))

(defn column-key [api]
  (-> api focused-cell .-column .-colId))

(defn select-node [api node column-key]
  (when (and api node)
    (.setSelected node true true)
    (.setFocusedCell api
                     (.-rowIndex node)
                     (or column-key (first (.-allDisplayedColumns (.-columnController node)))))))

(defn select-row-by-index! [state* api row-index column-key]
  (when (and api row-index)
    (swap! state* #(assoc % :row-index row-index))
    (select-node api (node-by-index (all-nodes api) row-index) column-key)))
