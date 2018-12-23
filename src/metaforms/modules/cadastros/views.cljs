(ns metaforms.modules.cadastros.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.components.cards :as cards]
            [metaforms.modules.cadastros.db :as cadastros-db]))

(defn index []
  [cards/card "Cadastros" [:div.list-group
                           (map-indexed (fn [i a][:a.list-group-item.list-group-item-action
                                                  {:href (:link a) :key (str i)}
                                                  (:label a)])
                                        cadastros-db/actions)]])
