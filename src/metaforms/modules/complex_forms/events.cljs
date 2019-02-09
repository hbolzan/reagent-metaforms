(ns metaforms.modules.complex-forms.events
  (:require [ajax.core :as ajax]
            [clojure.string :as str]
            [re-frame.core :as rf]
            [metaforms.common.logic :as common.logic]
            [metaforms.modules.samples.db :as samples.db]
            [metaforms.modules.complex-forms.logic :as cf.logic]))

  (def base-uri "http://localhost:8000/query/persistent/complex-tables/?id={id}&middleware=complex_forms&depth=1")

(rf/reg-event-fx
 :set-form-definition
 (fn [{db :db} [_ form-pk]]
   (let [form-id (-> form-pk str/lower-case (str/replace #"_" "-") keyword)]
     (if-let [form (cf.logic/get-form db form-id)]
       {:dispatch [:set-current-form form-id]}
       {:dispatch [:load-form-definition form-pk form-id]}))))

(rf/reg-event-fx
 :load-form-definition
 (fn [{db :db} [_ form-pk form-id]]
   {:http-xhrio {:method          :get
                 :uri             (str/replace base-uri #"\{id\}" form-pk)
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :timeout         8000
                 :on-success      [::load-form-definition-success form-id]
                 :on-failure      [::load-form-definition-failure]}}))

(defn load-form-definition-success [form-id response db]
  (let [form-definition (-> response :data first)]
    (assoc
     (assoc-in db [:complex-forms form-id] {:definition form-definition
                                            :state      :view})
     :current-form-data {:records        []
                         :current-record nil
                         :editing-data   nil
                         :new-record?    false})))

(rf/reg-event-fx
 ::load-form-definition-success
 (fn [{db :db} [_ form-id response]]
   {:db        (load-form-definition-success form-id response db)
    :dispatch [:set-current-form form-id]}))

(rf/reg-event-fx
 ::load-form-definition-failure
 (fn [{db :db} [_ result]]
   (js/console.log "ERROR" result)))

(rf/reg-event-db
 :set-current-form
 (fn [db [_ form-id]]
   (merge db {:current-view :complex-form
              :current-form form-id})))

;; form actions
;; :append :edit :confirm :discard :delete :search :refresh
;; data navigation
;; :nav-first :nav-prior :nav-next :nav-last
(rf/reg-event-fx
 :do-form-action
 (fn [{db :db} [_ form-action]]
   (let [current-state (cf.logic/current-form-state db)
         next-state    (cf.logic/next-form-state form-action current-state)]
     (when (not= current-state next-state)
       {:dispatch [(keyword (str "do-form-"(name form-action)))]}))))

(rf/reg-event-fx
 :do-form-append
 (fn [{db :db} [_]]
   (let [form-id (:current-form db)]
     {:db       (cf.logic/set-current-form-data db {:new-record?  true
                                                    :editing-data (cf.logic/new-record (cf.logic/field-defs db form-id))})
      :dispatch [:set-current-form-state :edit]})))

(rf/reg-event-fx
 :add-empty-record
 (fn [{db :db} [_ form-id]]
   {:db       (cf.logic/set-current-form-data db {:new-record?  true
                                                  :editing-data (cf.logic/new-record (cf.logic/field-defs db form-id))})
    :dispatch [:set-current-form-state :edit]}))

(rf/reg-event-fx
 :do-form-edit
 (fn [{db :db} [_ form-action]]
   {:dispatch [:set-current-form-state :edit]}))

(rf/reg-event-fx
 :do-form-discard
 (fn [{db :db} [_ form-action]]
   {:db       (cf.logic/set-current-form-data db {:new-record?  false
                                                  :editing-data nil})
    :dispatch [:set-current-form-state :view]}))

(rf/reg-event-fx
 :do-form-confirm
 (fn [{db :db} [_ form-action]]
   {:dispatch [:set-current-form-state :view]}))

(rf/reg-event-fx
 :do-form-delete
 (fn [{db :db} [_ form-action]]
   {:dispatch [:set-current-form-state :view]}))

(rf/reg-event-db
 :set-current-form-state
 (fn [db [_ new-state]]
   (assoc-in db [:complex-forms (:current-form db) :state] new-state)))
