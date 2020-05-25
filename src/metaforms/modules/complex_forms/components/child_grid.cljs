(ns metaforms.modules.complex-forms.components.child-grid
  (:require [metaforms.common.helpers :as helpers]
            [metaforms.common.logic :as cl]
            [metaforms.components.cards :as cards]
            [metaforms.modules.complex-forms.ag-grid-controller :as grid.controller]
            [metaforms.modules.complex-forms.components.ag-grid :as ag-grid]
            [metaforms.modules.complex-forms.components.toolset :as toolset]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.grid.api-helpers :as grid.api-helpers]
            [metaforms.modules.grid.logic :as grid.logic]
            [re-frame.core :as rf]
            [reagent.core :as r :refer [atom]]))

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

(defn append-row [{:keys [fields-defs child-id]}]
  [[:append-grid-data-row child-id (cf.logic/new-record fields-defs)]])

(defn on-save-button-click [{:keys [child-id child-form data update-diff]}]
  [[:grid-post-data
    child-id
    (grid.logic/prepare-to-save child-form
                                (apply-update-diff data update-diff)
                                [])]
   [:grid-clear-data-diff child-id]])

(defn grid-nav! [grid-state* button-id]
  (let [api        (:api @grid-state*)
        max-index  (dec (grid.api-helpers/row-count api))
        nav-op     (-> button-id name (clojure.string/replace  #"nav-" "") keyword)
        next-index (->> @grid-state* :row-index (grid.logic/nav-index* nav-op max-index))]
    (grid.api-helpers/select-row-by-index! grid-state* api next-index (grid.api-helpers/column-key api))))

(defn handle-toolbar-button [grid-state* {child-id :child-id :as params} button-id]
  (case button-id
    :append     (append-row params)
    :save       (on-save-button-click params)
    (:nav-first
     :nav-last
     :nav-prior
     :nav-next) (grid-nav! grid-state* button-id)))

(defn toolbar-on-click [grid-state* button-id params]
  (when-let [effects (handle-toolbar-button grid-state* params button-id)]
    (helpers/dispatch-n effects)))

(defn read-only? [{{auto-pk? :auto-pk :keys [pk-fields related-fields] :as form-def} :definition}
                  {:keys [name read-only] :as field-def}]
  (let [in-list? #(= name %)]
    (boolean (or
              read-only
              (and auto-pk? (some in-list? pk-fields))
              (some in-list? related-fields)))))

(defn child-grid [key parent-id child-id]
  (let [grid-state* (atom {})]
    (r/create-class
     {:display-name
      "child-ag-grid"
      :reagent-render
      (fn [key parent-id child-id]
        (let [api                   (:api @grid-state*)
              child-form            @(rf/subscribe [:form-by-id child-id])
              parent-data           @(rf/subscribe [:form-by-id-data parent-id])
              request-id            @(rf/subscribe [:form-by-id-request-id child-id])
              parent-new?           @(rf/subscribe [:current-form-new-record?])
              data-diff             @(rf/subscribe [:grid-data-diff child-id])
              data                  (:records @(rf/subscribe [:form-by-id-data child-id]))
              pending?              @(rf/subscribe [:grid-pending? child-id])
              soft-refresh?         @(rf/subscribe [:grid-soft-refresh? child-id])
              selected-row          @(rf/subscribe [:grid-selected-row child-id])
              fields-defs           (-> child-form :definition :fields-defs)
              pk-fields             (->> child-form :definition :pk-fields (mapv keyword))
              modified-linked-field @(rf/subscribe [:linked-field-changed child-id])]

          ;; (grid.api-helpers/select-row-by-index! grid-state* api selected-row (grid.api-helpers/column-key api))

          (js/console.log selected-row)
          (when modified-linked-field
            (grid.controller/set-modified-linked-field api child-id selected-row modified-linked-field))

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
                             :on-click       (fn [_ button-id]
                                               (toolbar-on-click
                                                grid-state*
                                                button-id
                                                {:child-id    child-id
                                                 :child-form  child-form
                                                 :data        data
                                                 :update-diff data-diff
                                                 :fields-defs fields-defs}))})
           [:div {:style {:min-height "100%"}}
            [:div.row
             [:div.col-md-12
              [ag-grid/data-grid {:form-id       child-id
                                  :fields-defs   fields-defs
                                  :data          data
                                  :soft-refresh? soft-refresh?
                                  :request-id    (if parent-new? (random-uuid) request-id)}
               grid-state*]]]]]))})))
