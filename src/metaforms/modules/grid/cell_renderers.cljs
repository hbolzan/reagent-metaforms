(ns metaforms.modules.grid.cell-renderers
  (:require [metaforms.common.logic :as cl]
            [moment :as moment]))

(defn date-read-only [value]
  (-> (moment value) (.format "DD/MM/YYYY")))

(def read-only-renderers {:date      date-read-only
                          :timestamp date-read-only})

(defn search [field-def]
  (let [renderer (or (-> field-def :data-type keyword read-only-renderers) identity)]
    #(renderer (.-value %))))

(defn with-renderers [renderer-kind fields-defs]
  (mapv (fn [field-def] (assoc field-def :renderer (renderer-kind field-def))) fields-defs))

(def with-search-renderers (partial with-renderers search))
