(ns metaforms.modules.complex-forms.events
  (:require [ajax.core :as ajax]
            [clojure.string :as str]
            [re-frame.core :as rf]
            [metaforms.modules.samples.db :as samples.db]
            [metaforms.modules.complex-forms.logic :as cf.logic]))

  (def base-uri "http://127.0.0.1:8000/query/persistent/complex-tables/?id={id}&middleware=complex_forms&depth=1")

(rf/reg-event-fx
 :set-form-definition
 (fn [{db :db} [_ form-id]]
   (if-let [form (cf.logic/get-form db form-id)]
     {:dispatch [:set-current-form form-id]}
     {:dispatch [:load-form-definition form-id]})))

(rf/reg-event-fx
 :load-form-definition
 (fn [{db :db} [_ form-id]]
   {:http-xhrio {:method          :get
                 :uri             (str/replace base-uri #"\{id\}" form-id)
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :timeout         8000
                 :on-success      [::load-form-definition-success]
                 :on-failure      [::load-form-definition-failure]}}))

(rf/reg-event-fx
 :load-form-definition-success
 (fn [{db :db} [_ result]]
   (let [form-id (-> result :data :id)]
     {:db       (assoc-in db [:complex-forms form-id] {:definition (:data result)
                                                                     :state      :empty
                                                                     :data       []})
      :dispatch [:set-current-form form-id]})))

(rf/reg-event-fx
 ::load-form-definition-failure
 (fn [{db :db} [_ result]]
   (js/console.log "ERROR" result))
 )

(rf/reg-event-fx
 :load-form-definition-old
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
