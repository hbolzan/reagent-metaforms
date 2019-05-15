(ns metaforms.modules.main.dom-helpers)

(defn element-by-id [id]
  (.getElementById js/document id))

(defn input-value [el]
  (-> el .-value))

(defn input-by-id-value-fn [id]
  #(-> id element-by-id input-value))
