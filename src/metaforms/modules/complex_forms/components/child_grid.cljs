(ns metaforms.modules.complex-forms.components.child-grid
  (:require [metaforms.common.helpers :as helpers]
            [metaforms.components.cards :as cards]
            [metaforms.modules.complex-forms.components.ag-grid :as ag-grid]
            [metaforms.modules.complex-forms.components.grid :as grid]
            [metaforms.modules.complex-forms.components.toolset :as toolset]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.grid.logic :as grid.logic]
            [re-frame.core :as rf]
            [reagent.core :as r :refer [atom]]))

(defn data-append! [fields-defs data-atom]
  (swap! data-atom conj (assoc (cf.logic/new-record fields-defs) :__uuid__ (random-uuid) :append? true)))

(defn merge-update-diff [update-diff row]
  (if-let [diff (get update-diff (:__uuid__ row))]
    (merge row diff {:update? true})
    row))

(defn apply-update-diff [data update-diff]
  (mapv (partial merge-update-diff update-diff) data))

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

;; (defn handle-toolset-button! [child-id
;;                               child-form
;;                               fields-defs
;;                               pk-fields
;;                               data
;;                               data-diff
;;                               data-atom
;;                               state-atom
;;                               selected-row
;;                               deleted-rows
;;                               button-id]
;;   (case button-id
;;     :append    (data-append! fields-defs data-atom)
;;     :confirm   (apply-diff-to-data-atom! child-id fields-defs data-diff state-atom data-atom)
;;     :refresh   (helpers/dispatch-n [[:child-reset-data-records child-id data]
;;                                     [:grid-set-pending-flag child-id false]])
;;     :discard   (rf/dispatch [:grid-soft-refresh-on child-id])
;;     :delete    (delete-row! child-id data-atom selected-row)
;;     :nav-next  (grid-nav! child-id data-atom state-atom inc)
;;     :nav-prior (grid-nav! child-id data-atom state-atom dec)
;;     :nav-first (grid-nav! child-id data-atom state-atom (constantly 0))
;;     :nav-last  (grid-nav! child-id data-atom state-atom (constantly (-> @data-atom count dec)))
;;     :save      (rf/dispatch [:grid-post-data
;;                              child-id
;;                              (grid.logic/prepare-to-save child-form @data-atom deleted-rows)])))

(defn on-save-button-click [{:keys [child-id child-form data update-diff]}]
  [[:grid-post-data
    child-id
    (grid.logic/prepare-to-save child-form
                                (apply-update-diff data update-diff)
                                [])]
   [:grid-clear-data-diff child-id]])

(defn handle-toolbar-button [params button-id]
  (case button-id
    :save (on-save-button-click params)))

(defn toolbar-on-click [button-id params]
  (when-let [effects (handle-toolbar-button params button-id)]
    (helpers/dispatch-n effects)))

(defn read-only? [{{auto-pk? :auto-pk :keys [pk-fields related-fields] :as form-def} :definition}
                  {:keys [name read-only] :as field-def}]
  (let [in-list? #(= name %)]
    (boolean (or
              read-only
              (and auto-pk? (some in-list? pk-fields))
              (some in-list? related-fields)))))

(defn field-def->column-model
  [d child-form]
  {:key         (:name d)
   :path        [:name]
   :name        (-> d :name keyword)
   :header      (:label d)
   :lookup-info {:lookup-key    (:lookup-key d)
                 :lookup-result (:lookup-result d)
                 :options       (:options d)}
   :field-def   (assoc d :read-only (read-only? child-form d))
   :col-hidden  (-> d :visible not)})

(defn child-grid [key parent-id child-id]
  (let [grid-state* (atom {})]
    (r/create-class
     {:display-name
      "child-ag-grid"
      :reagent-render
      (fn [key parent-id child-id]
        (let [child-form   @(rf/subscribe [:form-by-id child-id])
              parent-data  @(rf/subscribe [:form-by-id-data parent-id])
              request-id   @(rf/subscribe [:form-by-id-request-id child-id])
              parent-new?  @(rf/subscribe [:current-form-new-record?])
              data-diff    @(rf/subscribe [:grid-data-diff child-id])
              data         (:records @(rf/subscribe [:form-by-id-data child-id]))
              selected-row (or @(rf/subscribe [:grid-selected-row child-id]) 0)
              pending?     @(rf/subscribe [:grid-pending? child-id])
              fields-defs  (-> child-form :definition :fields-defs)
              pk-fields    (->> child-form :definition :pk-fields (mapv keyword))
              column-model #(field-def->column-model % child-form)]
          (helpers/dispatch-n [[:complex-table-parent-data-changed child-id]
                               [:grid-soft-refresh-off child-id]])
          [cards/card
           ^{:key key}
           (-> child-form :definition :title)
           (toolset/toolbar {:form-id        child-id
                             :form-state     (when-not parent-new? (grid.logic/grid-state data nil pending?))
                             :buttons-groups [(dissoc toolset/action-buttons :search :edit)
                                              toolset/nav-buttons
                                              toolset/extra-buttons]
                             :on-click       (fn [_ e] (toolbar-on-click e {:child-id    child-id
                                                                            :child-form  child-form
                                                                            :data        data
                                                                            :update-diff data-diff}))})
           [:div {:style {:min-height "100%"}}
            [:div.row
             [:div.col-md-12
              [ag-grid/data-grid {:form-id      child-id
                                  :form-def     child-form
                                  :fields-defs  fields-defs
                                  :column-model column-model
                                  :data         data
                                  :request-id   (if parent-new? (random-uuid) request-id)}
               grid-state*]]]]]))})))

;; (defn form-child [key parent-id child-id]
;;   (let [child-form  @(rf/subscribe [:form-by-id child-id])
;;         parent-data @(rf/subscribe [:form-by-id-data parent-id])
;;         request-id  @(rf/subscribe [:form-by-id-request-id child-id])
;;         parent-new? @(rf/subscribe [:current-form-new-record?])
;;         data        (:records @(rf/subscribe [:form-by-id-data child-id]))

;;         ;; TODO: put all grid information in one only subscription
;;         soft-refresh? @(rf/subscribe [:grid-soft-refresh? child-id])
;;         pending?      @(rf/subscribe [:grid-pending? child-id])
;;         selected-row  (or @(rf/subscribe [:grid-selected-row child-id]) 0)
;;         data-diff     @(rf/subscribe [:grid-data-diff child-id])
;;         data-atom     @(rf/subscribe [:grid-data-atom child-id])
;;         state-atom    @(rf/subscribe [:grid-state-atom child-id])
;;         deleted-rows  @(rf/subscribe [:deleted-rows child-id])

;;         fields-defs  (-> child-form :definition :fields-defs)
;;         pk-fields    (->> child-form :definition :pk-fields (mapv keyword))
;;         column-model #(field-def->column-model % child-form)]
;;     (helpers/dispatch-n [[:complex-table-parent-data-changed child-id]
;;                          [:grid-soft-refresh-off child-id]])
;;     [cards/card
;;      ^{:key key}
;;      (-> child-form :definition :title)
;;      (toolset/toolbar {:form-id        child-id
;;                        :form-state     (when-not parent-new? (grid.logic/grid-state data data-diff pending?))
;;                        :buttons-groups [(dissoc toolset/action-buttons :search :edit)
;;                                         toolset/nav-buttons
;;                                         toolset/extra-buttons]
;;                        :on-click       (fn [form-id e] (handle-toolset-button! child-id
;;                                                                                child-form
;;                                                                                fields-defs
;;                                                                                pk-fields
;;                                                                                data
;;                                                                                data-diff
;;                                                                                data-atom
;;                                                                                state-atom
;;                                                                                selected-row
;;                                                                                deleted-rows
;;                                                                                e))})
;;      [:div {:style {:min-height "100%"}}
;;       [:div.row
;;        [:div.col-md-12
;;         [ag-grid/data-grid {:form-id         child-id
;;                             :form-def        child-form
;;                             :fields-defs     fields-defs
;;                             :column-model    column-model
;;                             :data            (if parent-new? [] (if soft-refresh? @data-atom  data))
;;                             :soft-refresh?   soft-refresh?
;;                             :request-id      (if parent-new? (random-uuid) request-id)
;;                             :on-request-data (fn [data data-diff] (js/console.log data) (js/console.log data-diff))}]]]]]))
