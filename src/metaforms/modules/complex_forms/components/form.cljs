(ns metaforms.modules.complex-forms.components.form
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [react-sticky-table :refer [StickyTable Row Cell]]
            [metaforms.modules.complex-forms.components.input :as input]
            [metaforms.components.cards :as cards]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.components.toolset :as toolset]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.complex-forms.view-logic :as view-logic]))

(defn complex-grid-header [fields-defs]
  [:> Row
   (doall
    (map (fn [field-def]
           [:> Cell {:key (str "header-" (:name field-def))} (:label field-def)])
         fields-defs))])

(defn complex-grid-row [fields-defs row]
  [:> Row {:key (str "row-")}
   (doall
    (map (fn [field-def]
           [:> Cell
            {:key (str "field-" (:name field-def))}
            (get row (-> field-def :name keyword))])
         fields-defs))])

(defn complex-grid-body [fields-defs data]
  (doall (map (fn [row] (complex-grid-row fields-defs row)) data)))

(defn complex-grid [table-id fields-defs data]
  [:div
   [:div {:style {:width "100%" :height "350px"}}
    [:> StickyTable
     (complex-grid-header fields-defs)
     (when (not-empty data)
       (doall (map (fn [row] (complex-grid-row fields-defs row)) data)))]]])

(defn form-child [key parent-id child-id]
  (let [child-form   @(rf/subscribe [:form-by-id child-id])
        parent-data  @(rf/subscribe [:form-by-id-data parent-id])
        record-index @(rf/subscribe [:form-by-id-record-index child-id])
        data         (or (:records @(rf/subscribe [:form-by-id-data child-id])) [])]
    (rf/dispatch [:complex-table-parent-data-changed child-id])
    [cards/card
     ^{:key key}
     (-> child-form :definition :title)
     (toolset/toolset child-id (dissoc toolset/action-buttons :search) toolset/nav-buttons)
     [:div {:style {:min-height "100%"}}
      [:div.row
       [:div.col-md-12
        [complex-grid child-id (-> child-form :definition :fields-defs) data]]]]]))

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

#_(defn form-child [key parent-id child-id]
  (let [child-state (atom {})]
    (fn [key parent-id child-id]
      (let [child-form   @(rf/subscribe [:form-by-id child-id])
            parent-data  @(rf/subscribe [:form-by-id-data parent-id])
            record-index @(rf/subscribe [:form-by-id-record-index child-id])
            data         (or (:records @(rf/subscribe [:form-by-id-data child-id])) [])]
        (rf/dispatch [:complex-table-parent-data-changed child-id])
        (when-let [api (:api @child-state)]
          (js/console.log api))
        [cards/card
         ^{:key key}
         (-> child-form :definition :title)
         (toolset/toolset child-id (dissoc toolset/action-buttons :search) toolset/nav-buttons)
         [:div {:style {:min-height "100%"}}
          [:div.row
           [:div.col-md-12
            [:div.ag-theme-balham {:style {:height "350px" :width "100%"}}
             [:> AgGridReact {:reactNext                      true
                              :rowDataChangeDetectionStrategy "NoCheck"
                              :onGridReady                    (fn [params]
                                                                (js/console.log params)
                                                                (swap! child-state assoc :api params.api)
                                                                (swap! child-state assoc :columnApi params.columnApi))
                              :gridOptions                    {:columnDefs   (cf.logic/fields-defs->ag-grid-cols
                                                                              (-> child-form :definition :fields-defs)
                                                                              false)
                                                               :rowData      data
                                                               :rowSelection "single"
                                                               ;; :getRowStyle  (partial row-style record-index)
                                                               }}]]]]]]))))



(defn form [{:keys [id title rows-defs fields-defs children] :as form-definition}]
  (let [form-state @(rf/subscribe [:current-form-state])
        form-id @(rf/subscribe [:current-form-id])]
    [cards/card
     title
     (toolset/toolset form-id)
     [:div
      (doall (map-indexed (fn [index row-def] (form-row id index row-def fields-defs form-state)) rows-defs))
      (when children (map-indexed (fn [i child] [form-child {:key i} form-id child]) children))]]))
