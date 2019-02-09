(ns metaforms.modules.complex-forms.components.form
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.modules.complex-forms.components.input :as input]
            [metaforms.components.cards :as cards]
            [metaforms.modules.complex-forms.components.toolset :as toolset]
            [metaforms.modules.complex-forms.logic :as l-cf]))

;; form-data => {:records [hash-map] :current-record integer :editing-data hash-map :new-record? boolean}
(defn form-field
  [{:keys [id label] :as field} additional-group-class form-data]
  [:div {:key id :class (str "form-group" (some->> additional-group-class (str " ")))}
   [:label {:html-for id} label]
   (input/input field ((-> field :name keyword) (:editing-data form-data)))])

(defn form-row
  [form-id row-index row-def fields-defs form-data]
  [:div.form-row {:key (str "row-" row-index)}
   (doall
    (map (fn [field bootstrap-width] (form-field field (l-cf/width->col-md-class bootstrap-width) form-data))
         (l-cf/row-fields row-def fields-defs)
         (:bootstrap-widths row-def)))])

(defn form [{:keys [id title rows-defs fields-defs] :as form-definition}]
  (let [form-data @(rf/subscribe [:current-form-data])]
    [cards/card
     title
     (toolset/toolset)
     [:div
      (doall (map-indexed (fn [index row-def] (form-row id index row-def fields-defs form-data)) rows-defs))]]))
