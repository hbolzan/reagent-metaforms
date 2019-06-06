(ns metaforms.modules.complex-forms.components.form
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ag-grid-react :refer [AgGridReact]]
            [metaforms.modules.complex-forms.components.input :as input]
            [metaforms.components.cards :as cards]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.components.toolset :as toolset]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.complex-forms.view-logic :as view-logic]))

(defn complex-grid-header [fields-defs]
  (let [key-prefix (random-uuid)]
    [:thead
     [:tr
      (doall
       (map (fn [field-def]
              [:th {:key (str key-prefix "-" (:name field-def))} (:label field-def)])
            fields-defs))]]))

(defn complex-grid-row [fields-defs row scroll-left]
  (let [key-prefix (random-uuid)]
    [:tr {:key (str key-prefix "-row")}
     (doall
      (map (fn [field-def]
             [:td
              {:key (str key-prefix "-" (:name field-def)) :data-scroll-left scroll-left}
              (cl/log (get row (-> field-def :name keyword)))])
           fields-defs))]))

(defn complex-grid-body [fields-defs data scroll-left]
  [:tbody
   (doall (map (fn [row] (complex-grid-row fields-defs row scroll-left)) data))])

(defn complex-grid [fields-defs data]
  (let [scroll-info (atom {})
        table-width (atom "")]
    (fn [fields-defs data]
      [:div.pane.pane--table2
       {:onScroll #(swap! scroll-info assoc :left (-> % .-target .-scrollLeft))}
       [:table.table {:width (cl/log @table-width)
                      :data-scroll-left (-> @scroll-info :left)
                      :ref (fn [el] (when (not (nil? el))
                                      (reset! table-width
                                              (str (+
                                                    (cl/log (-> @scroll-info :left))
                                                    (js/parseInt (.-width (js/window.getComputedStyle el))))
                                                   "px"))))}
        (complex-grid-header fields-defs)
        (when (not-empty data)
          (complex-grid-body fields-defs data (:left @scroll-info)))]])))

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
        [complex-grid (-> child-form :definition :fields-defs) data]
        ]]]]))

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

(defn row-style [record-index params]
  (js/console.log params)
  (when (= record-index (-> (js->clj params) (get "data") (get "id")))
    (clj->js {:background-color "#b7e4ff"})))

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
