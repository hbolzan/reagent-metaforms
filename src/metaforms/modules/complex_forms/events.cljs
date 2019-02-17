(ns metaforms.modules.complex-forms.events
  (:require [ajax.core :as ajax]
            [clojure.string :as str]
            [re-frame.core :as rf]
            [metaforms.common.dictionary :refer [l]]
            [metaforms.common.logic :as cl]
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
    (assoc-in db [:complex-forms form-id] {:definition form-definition
                                           :state      :view
                                           :data {:records        []
                                                  :current-record nil
                                                  :editing-data   nil
                                                  :new-record?    false}})))

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

;; TOOLSET ENTRY POINT
(rf/reg-event-fx
 :do-form-action
 (fn [{db :db} [_ form-action]]
   (let [current-state (cf.logic/current-form-state db)
         next-state    (cf.logic/next-form-state form-action current-state)]
     (when (not= current-state next-state)
       {:dispatch [(keyword (str "do-form-"(name form-action)))]}))))

;; SEARCH
(rf/reg-event-fx
 :do-form-search
 (fn [{db :db} _]
   ))

;; NAVIGATION ACTIONS
(rf/reg-event-fx
 :do-form-nav-first
 (fn [{db :db} _]
   {:db (cf.logic/set-current-record-index db 0)}))

(rf/reg-event-fx
 :do-form-nav-prior
 (fn [{db :db} _]
   (let [record-index (cf.logic/current-record-index db)
         prior-index  (if (> record-index 0) (dec record-index) record-index)]
     {:db (cf.logic/set-current-record-index db prior-index)})))

(rf/reg-event-fx
 :do-form-nav-next
 (fn [{db :db} _]
   (let [record-index (cf.logic/current-record-index db)
         record-count (count (cf.logic/current-records db))
         next-index   (if (< record-index (dec record-count)) (inc record-index) record-index)]
     {:db (cf.logic/set-current-record-index db next-index)})))

(rf/reg-event-fx
 :do-form-nav-last
 (fn [{db :db} _]
   (let [record-count (count (cf.logic/current-records db))]
     {:db (cf.logic/set-current-record-index db (when (> record-count 0) (dec record-count)))})))

;; CRUD ACTIONS
(rf/reg-event-fx
 :do-form-edit
 (fn [{db :db} _]
   (if-let [current-record (cf.logic/current-data-record db)]
     {:db       (cf.logic/set-current-form-data db {:new-record?  false
                                                    :editing-data (cf.logic/current-data-record db)})
      :dispatch [:set-current-form-state :edit]}
     {:dispatch [:do-form-append]})))

(rf/reg-event-fx
 :do-form-append
 (fn [{db :db} _]
   {:db       (cf.logic/set-current-form-data db {:new-record?  true
                                                  :editing-data (cf.logic/new-record (cf.logic/fields-defs db))})
    :dispatch [:set-current-form-state :edit]}))

(rf/reg-event-fx
 :do-form-discard
 (fn [{db :db} _]
   {:db       (cf.logic/set-current-form-data db {:new-record?  false
                                                  :editing-data nil})
    :dispatch [:set-current-form-state :view]}))

(rf/reg-event-fx
 :do-form-confirm
 (fn [{db :db} _]
   {:dispatch [:ask-for-confirmation
               (l (if (cf.logic/new-record? db) :form/confirm-append? :form/confirm-edit?))
               :do-confirmed-form-confirm]}))

(rf/reg-event-fx
 :do-confirmed-form-confirm
 (fn [{db :db} _]
   (let [new-records          (cf.logic/records<-editing-data db)
         current-record-index (cf.logic/current-record-index db)]
     {:db       (cf.logic/set-current-form-data db {:new-record?    false
                                                    :editing-data   nil
                                                    :current-record (if (cf.logic/new-record? db)
                                                                      (-> new-records count dec)
                                                                      current-record-index)
                                                    :records        new-records})
      :dispatch [:set-current-form-state :view]})))

(rf/reg-event-fx
 :do-form-delete
 (fn [{db :db} _]
   (if (cf.logic/current-record-index db)
     {:dispatch [:ask-for-confirmation (l :form/confirm-delete?) :do-confirmed-form-delete]})))

(rf/reg-event-fx
 :do-nput-focus
 (fn [db [_ field-name]]
   (cf.logic/set-current-form-data db {:editing field-name})))

(rf/reg-event-db
 :input-blur
 (fn [db [_ field-name field-value]]
   (cf.logic/set-current-form-data db {:editing      nil
                                       :editing-data (assoc
                                                      (cf.logic/editing-data db)
                                                      (keyword field-name)
                                                      field-value)})))

(rf/reg-event-db
 :set-current-form-state
 (fn [db [_ new-state]]
   (assoc-in db [:complex-forms (:current-form db) :state] new-state)))
