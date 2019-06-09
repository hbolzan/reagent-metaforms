(ns metaforms.modules.grid.events
  (:require [re-frame.core :as rf]
            [metaforms.modules.complex-forms.logic :as cf.logic]))

(rf/reg-event-fx
 :grid-clear-data-diff
 (fn [{db :db} [_ form-id]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :data-diff {})}))

(rf/reg-event-fx
 :grid-soft-refresh-on
 (fn [{db :db} [_ form-id]]
   {:db (-> db
            (cf.logic/form-by-id-set-some-prop form-id :soft-refresh? true)
            (cf.logic/form-by-id-set-some-prop form-id :request-id (random-uuid)))}))

(rf/reg-event-fx
 :grid-soft-refresh-off
 (fn [{db :db} [_ form-id]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :soft-refresh? false)}))

(rf/reg-event-fx
 :grid-data-change
 (fn [{db :db} [_ form-id]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :data-diff {})}))

(rf/reg-event-fx
 :grid-set-data-diff
 (fn [{db :db} [_ form-id row-id column-id value]]
   {:db (cf.logic/form-by-id-set-some-prop
         db
         form-id
         :data-diff
         (assoc-in (cf.logic/form-by-id-some-prop db form-id :data-diff)
                   [row-id column-id]
                   value))}))

(rf/reg-event-fx
 :grid-set-data-atom
 (fn [{db :db} [_ form-id data-atom]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :data-atom data-atom)}))

(rf/reg-event-fx
 :grid-set-state-atom
 (fn [{db :db} [_ form-id state-atom]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :state-atom state-atom)}))

(rf/reg-event-fx
 :grid-set-selected-row
 (fn [{db :db} [_ form-id row-index]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :selected-row row-index)}))

(rf/reg-event-fx
 :grid-set-pending-flag
 (fn [{db :db} [_ form-id pending?]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :pending? pending?)}))
