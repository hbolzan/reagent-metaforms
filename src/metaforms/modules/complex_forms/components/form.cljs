(ns metaforms.modules.complex-forms.components.form
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.modules.complex-forms.components.input :as input]
            [metaforms.components.cards :as cards]
            [metaforms.modules.complex-forms.components.toolset :as toolset]
            [metaforms.modules.complex-forms.view-logic :as view-logic]))

;; form-data => {:records [hash-map] :current-record integer :editing-data hash-map :new-record? boolean}
(defn form-field
  [{:keys [id label] :as field} additional-group-class form-state all-defs]
  [:div {:key id :class (str "form-group" (some->> additional-group-class (str " ")))}
   [:label {:html-for id} label]
   [input/input field form-state all-defs]])

(defn form-row
  [form-id row-index row-def fields-defs form-state]
  [:div.form-row {:key (str "row-" row-index)}
   (doall
    (map (fn [field bootstrap-width]
           (form-field field (view-logic/width->col-md-class bootstrap-width) form-state fields-defs))
         (view-logic/row-fields row-def fields-defs)
         (:bootstrap-widths row-def)))])

(defn form [{:keys [id title rows-defs fields-defs] :as form-definition}]
  (let [form-state @(rf/subscribe [:current-form-state])]
    [cards/card
     title
     (toolset/toolset)
     [:div
      (doall (map-indexed (fn [index row-def] (form-row id index row-def fields-defs form-state)) rows-defs))]]))
