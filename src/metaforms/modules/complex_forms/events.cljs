(ns metaforms.modules.complex-forms.events
  (:require [re-frame.core :as rf]
            [metaforms.modules.samples.db :as samples.db]
            [metaforms.modules.complex-forms.logic :as cf.logic]))

(rf/reg-event-fx
 :set-form-definition
 (fn [{db :db} [_ form-id]]
   (if-let [form (cf.logic/get-form db form-id)]
     {:dispatch [:set-current-form form-id]}
     {:dispatch [:load-form-definition form-id]})))

(rf/reg-event-fx
 :load-form-definition
 (fn [{db :db} [_ form-id]]
   {:db       (assoc-in db [:complex-forms form-id] {:definition samples.db/form-definition
                                                     :state      :empty
                                                     :data       []})
    :dispatch [:set-current-form form-id]}))

(rf/reg-event-db
 :set-current-form
 (fn [db [_ form-id]]
   (merge db {:current-view :complex-form
              :current-form form-id})))

(rf/reg-event-db
 :set-current-form-state
 (fn [db [_ new-state]]
   (assoc-in db [:complex-forms (:current-form db) :state] new-state)))
