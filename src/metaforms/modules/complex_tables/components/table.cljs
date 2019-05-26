(ns metaforms.modules.complex-tables.components.table
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.components.cards :as cards]
            [metaforms.modules.complex-forms.components.toolset :as toolset]))

(defn complex-table [{:keys [id title rows-defs fields-defs] :as form-definition}]
  (let [table-data @(rf/subscribe [:complex-table-data id])]
    [cards/card
     title
     (toolset/toolset)
     [:div
      (doall (map-indexed (fn [index row-def] (form-row id index row-def fields-defs form-state)) rows-defs))]]))
