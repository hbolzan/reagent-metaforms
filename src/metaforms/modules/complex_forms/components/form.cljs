(ns metaforms.modules.complex-forms.components.form
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.modules.complex-forms.components.input :as input]
            [metaforms.components.cards :as cards]
            [metaforms.modules.complex-forms.logic :as l-cf]))

(defn form-field
  [{:keys [id label] :as field}
   additional-group-class]
  [:div {:class (str "form-group" (some->> additional-group-class (str " ")))}
   [:label {:htmlFor id} label]
   (input/input field)])

(defn form-row
  [form-id row-index row-def fields]
  [:div.form-row {:key (str "row-" row-index)} ]
  (map (fn
         [field bootstrap-width]
         (form-field field (l-cf/width->col-md-class bootstrap-width)))
       (l-cf/row-fields row-def fields)
       (:bootstrap-widths row-def)))

(defn form [{:keys [id title rows-defs fields]}]
  [cards/card
   title
   [:div
    (map-indexed (fn [index row-def] (form-row id index row-def fields)) rows-defs)]])
