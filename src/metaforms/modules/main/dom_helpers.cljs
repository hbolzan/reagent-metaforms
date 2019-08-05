(ns metaforms.modules.main.dom-helpers)

(defn element-by-id [id]
  (.getElementById js/document id))

(defn first-element-by-class-name [class-name]
  (-> (.getElementsByClassName js/document class-name) array-seq first))

(defn input-value [el]
  (-> el .-value))

(defn input-by-id-value-fn [id]
  #(-> id element-by-id input-value))

(defn set-scroll-top [el scroll-top]
  (set! (.-scrollTop el) scroll-top))
