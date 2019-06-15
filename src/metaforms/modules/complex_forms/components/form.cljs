(ns metaforms.modules.complex-forms.components.form
  (:require [metaforms.common.helpers :as helpers]
            [metaforms.common.logic :as cl]
            [metaforms.components.cards :as cards]
            [metaforms.modules.complex-bundles.logic :as bl]
            [metaforms.modules.complex-forms.components.grid :as grid]
            [metaforms.modules.complex-forms.components.input :as input]
            [metaforms.modules.complex-forms.components.toolset :as toolset]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.complex-forms.view-logic :as view-logic]
            [metaforms.modules.grid.logic :as grid.logic]
            [re-frame.core :as rf]
            [re-frame.db :as rdb]
            [reagent.core :as r]))

(defn data-append! [fields-defs data-atom]
  (swap! data-atom conj (assoc (cf.logic/new-record fields-defs) :__uuid__ (random-uuid) :append? true)))

(defn apply-diff-to-data-atom! [child-id fields-defs data-diff state-atom data-atom]
  (when (not-empty data-diff)
    (reset! data-atom
            (mapv (fn [row]
                    (if-let [diff (get data-diff (:__uuid__ row))]
                      (merge row diff {:update? true})
                      row))
                  @data-atom))
    (helpers/dispatch-n [[:grid-clear-data-diff child-id]
                         [:grid-set-pending-flag child-id true]])))

(defn delete-row! [child-id data-atom row-index]
  (let [data @data-atom]
    (reset! data-atom
            (into [] (keep-indexed (fn [idx row] (if (not= idx row-index) row)) data)))
    (helpers/dispatch-n [[:grid-add-deleted-row child-id (nth data row-index)]
                         [:grid-set-pending-flag child-id true]])))

(defn grid-nav! [child-id data-atom state-atom nav-fn]
  (let [selected-row (:selected-row @state-atom)
        max-row      (-> @data-atom count dec)
        new-row      (nav-fn (:selected-row @state-atom))]
    (when (and (<= new-row max-row) (>= new-row 0))
      (swap! state-atom assoc :selected-row new-row)
      (rf/dispatch [:grid-set-selected-row child-id new-row]))))

(defn handle-toolset-button! [child-id
                              child-form
                              fields-defs
                              pk-fields
                              data
                              data-diff
                              data-atom
                              state-atom
                              selected-row
                              deleted-rows
                              button-id]
  (case button-id
    :append    (data-append! fields-defs data-atom)
    :confirm   (apply-diff-to-data-atom! child-id fields-defs data-diff state-atom data-atom)
    :refresh   (helpers/dispatch-n [[:child-reset-data-records child-id data]
                                    [:grid-set-pending-flag child-id false]])
    :discard   (rf/dispatch [:grid-soft-refresh-on child-id])
    :delete    (delete-row! child-id data-atom selected-row)
    :nav-next  (grid-nav! child-id data-atom state-atom inc)
    :nav-prior (grid-nav! child-id data-atom state-atom dec)
    :nav-first (grid-nav! child-id data-atom state-atom (constantly 0))
    :nav-last  (grid-nav! child-id data-atom state-atom (constantly (-> @data-atom count dec)))
    :save      (rf/dispatch [:grid-post-data
                             child-id
                             (grid.logic/prepare-to-save child-form @data-atom deleted-rows)])
    (js/console.log button-id)))

(defn read-only? [{{auto-pk? :auto-pk :keys [pk-fields related-fields] :as form-def} :definition}
                  {:keys [name read-only] :as field-def}]
  (let [in-list? #(= name %)]
    (boolean (or
              read-only
              (and auto-pk? (some in-list? pk-fields))
              (some in-list? related-fields)))))


(defn form-child [key parent-id child-id]
  (let [child-form  @(rf/subscribe [:form-by-id child-id])
        parent-data @(rf/subscribe [:form-by-id-data parent-id])
        request-id  @(rf/subscribe [:form-by-id-request-id child-id])
        parent-new? @(rf/subscribe [:current-form-new-record?])
        data        (:records @(rf/subscribe [:form-by-id-data child-id]))

        ;; TODO: put all grid information in one only subscription
        soft-refresh? @(rf/subscribe [:grid-soft-refresh? child-id])
        pending?      @(rf/subscribe [:grid-pending? child-id])
        selected-row  (or @(rf/subscribe [:grid-selected-row child-id]) 0)
        data-diff     @(rf/subscribe [:grid-data-diff child-id])
        data-atom     @(rf/subscribe [:grid-data-atom child-id])
        state-atom    @(rf/subscribe [:grid-state-atom child-id])
        deleted-rows  @(rf/subscribe [:deleted-rows child-id])

        fields-defs  (-> child-form :definition :fields-defs)
        pk-fields    (->> child-form :definition :pk-fields (mapv keyword))
        column-model (mapv (fn [d] {:key       (:name d)
                                    :path      [:name]
                                    :name      (-> d :name keyword)
                                    :header    (:label d)
                                    :field-def (assoc d :read-only (read-only? child-form d))})
                           fields-defs)]
    (helpers/dispatch-n [[:complex-table-parent-data-changed child-id]
                         [:grid-soft-refresh-off child-id]])
    [cards/card
     ^{:key key}
     (-> child-form :definition :title)
     (toolset/toolbar {:form-id        child-id
                       :form-state     (when-not parent-new? (grid.logic/grid-state data data-diff pending?))
                       :buttons-groups [(dissoc toolset/action-buttons :search :edit)
                                        toolset/nav-buttons
                                        toolset/extra-buttons]
                       :on-click       (fn [form-id e] (handle-toolset-button! child-id
                                                                               child-form
                                                                               fields-defs
                                                                               pk-fields
                                                                               data
                                                                               data-diff
                                                                               data-atom
                                                                               state-atom
                                                                               selected-row
                                                                               deleted-rows
                                                                               e))})
     [:div {:style {:min-height "100%"}}
      [:div.row
       [:div.col-md-12
        [grid/child-grid {:form-id         child-id
                          :form-def        child-form
                          :column-model    column-model
                          :data            (if parent-new? [] (if soft-refresh? @data-atom  data))
                          :soft-refresh?   soft-refresh?
                          :request-id      (if parent-new? (random-uuid) request-id)
                          :on-request-data (fn [data data-diff] (js/console.log data) (js/console.log data-diff))}]]]]]


    ))

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
