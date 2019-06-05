(ns metaforms.modules.complex-forms.components.form
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [react-data-grid :as ReactDataGrid]
            [metaforms.modules.complex-forms.components.input :as input]
            [metaforms.components.cards :as cards]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.components.toolset :as toolset]
            [metaforms.modules.complex-forms.logic :as cf.logic]
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

(defn form-child [key parent-id child-id]
  (r/create-class
   {:display-name         "Test Grid"
    :component-did-mount  (fn [this])
    :component-did-update (fn [this old-argv])
    :reagent-render
    (fn [key parent-id child-id]
      (let [child-form  @(rf/subscribe [:form-by-id child-id])
            parent-data @(rf/subscribe [:form-by-id-data parent-id])
            data        (or (:records @(rf/subscribe [:form-by-id-data child-id])) [])
            grid        [:> ReactDataGrid {:columns             (cf.logic/fields-defs->data-grid-cols
                                                                 (-> child-form :definition :fields-defs)
                                                                 false)
                                           :rowGetter           #(get (clj->js data) %)
                                           :rowsCount           (count data)
                                           :minHeight           350
                                           :enableCellAutoFocus true
                                           :enableCellSelect    true
                                           :selectedRows        [0]
                                           }]]
        (rf/dispatch [:complex-table-parent-data-changed child-id])
        [cards/card
         ^{:key key}
         (-> child-form :definition :title)
         (toolset/toolset child-id (dissoc toolset/action-buttons :search) toolset/nav-buttons)
         [:div {:style {:min-height "100%"}}
          [:div.row
           [:div.col-md-12
            [:div
             grid]]]]]))}))



(defn form [{:keys [id title rows-defs fields-defs children] :as form-definition}]
  (let [form-state @(rf/subscribe [:current-form-state])
        form-id @(rf/subscribe [:current-form-id])]
    [cards/card
     title
     (toolset/toolset form-id)
     [:div
      (doall (map-indexed (fn [index row-def] (form-row id index row-def fields-defs form-state)) rows-defs))
      (when children (map-indexed (fn [i child] [form-child {:key i} form-id child]) children))]]))
