(ns metaforms.modules.complex-forms.components.form
  (:require [metaforms.common.logic :as cl]
            [metaforms.components.cards :as cards]
            [metaforms.modules.complex-forms.components.grid :as grid]
            [metaforms.modules.complex-forms.components.input :as input]
            [metaforms.modules.complex-forms.components.toolset :as toolset]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.complex-forms.view-logic :as view-logic]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(defn form-child [key parent-id child-id]
  (let [child-form   @(rf/subscribe [:form-by-id child-id])
        parent-data  @(rf/subscribe [:form-by-id-data parent-id])
        record-index @(rf/subscribe [:form-by-id-record-index child-id])
        data         (:records @(rf/subscribe [:form-by-id-data child-id]))
        column-model (mapv (fn [d] {:key    (:name d)
                                    :path   [:name]
                                    :name   (-> d :name keyword)
                                    :header (:label d)})
                           (-> child-form :definition :fields-defs))]
    (rf/dispatch [:complex-table-parent-data-changed child-id])
    [grid/child-grid {:form-def     child-form
                      :column-model column-model
                      :data         data
                      :title        (-> child-form :definition :title)
                      :on-request-data (fn [data data-diff] (js/console.log data) (js/console.log data-diff))}]))

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

(defn form [{:keys [id title rows-defs fields-defs children] :as form-definition}]
  (let [form-state @(rf/subscribe [:current-form-state])
        form-id @(rf/subscribe [:current-form-id])]
    [cards/card
     title
     (toolset/toolset form-id)
     [:div
      (doall (map-indexed (fn [index row-def] (form-row id index row-def fields-defs form-state)) rows-defs))
      (when children (map-indexed (fn [i child] [form-child {:key i} form-id child]) children))]]))
