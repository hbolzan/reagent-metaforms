(ns metaforms.modules.complex-forms.components.form
  (:require [metaforms.common.helpers :as helpers]
            [metaforms.components.cards :as cards]
            [metaforms.modules.complex-forms.components.child-grid :as child-grid]
            [metaforms.modules.complex-forms.components.input :as input]
            [metaforms.modules.complex-forms.components.toolset :as toolset]
            [metaforms.modules.complex-forms.view-logic :as view-logic]
            [re-frame.core :as rf]
            [reagent-keybindings.keyboard :as kb]))

(defn form-field
  [form-id {:keys [id label] :as field} additional-group-class form-state all-defs]
  [:div {:key id :class (str "form-group" (some->> additional-group-class (str " ")))}
   [:label {:html-for id} label]
   [input/input form-id field form-state]])

(defn form-row
  [form-id row-index row-def fields-defs form-state]
  [:div.form-row {:key (str "row-" row-index)}
   (doall
    (map (fn [field bootstrap-width]
           (form-field form-id field (view-logic/width->col-md-class bootstrap-width) form-state fields-defs))
         (view-logic/row-fields row-def fields-defs)
         (:bootstrap-widths row-def)))])

(defn render-tabs
  [form-id pages-info active-page]
  (when (first pages-info)
    [:ul.nav.nav-tabs
     (doall
      (map
       (fn [page-info]
         (let [page-index (:index page-info)]
           [:li.nav-item {:key page-index}
            [:div {:class    (str "nav-link" (when (= active-page page-index) " active"))
                   :on-click #(rf/dispatch [:form-set-active-page form-id page-index])}
             (:title page-info)]]))
       pages-info))]))

(defn render-form [content form-id {pages-info :pages-info} active-page]
  (if-let [tabs (render-tabs form-id pages-info active-page)]
    [:div tabs
     [:div.card.border-top-0 content]]
    [:div content]))

(defn form [{:keys [id title rows-defs fields-defs children] :as form-definition}]
  (let [form-state  @(rf/subscribe [:current-form-state])
        form-id     @(rf/subscribe [:current-form-id])
        active-page @(rf/subscribe [:form-by-id-active-page form-id])]
    [cards/card
     title
     (toolset/toolset form-id)
     (render-form
      [:div.card-body
       [kb/kb-action "ctrl-f" #(rf/dispatch-sync [:do-form-action :search form-id])]
       (doall (map-indexed (fn [index row-def] (form-row form-id index row-def fields-defs form-state)) (get rows-defs active-page)))
       (when children (map-indexed (fn [i child] [child-grid/child-grid {:key i} form-id child]) children))]
      form-id
      form-definition
      active-page)]))
