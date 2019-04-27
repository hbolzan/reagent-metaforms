(ns metaforms.modules.complex-forms.events
  (:require [ajax.core :as ajax]
            [clojure.string :as str]
            [re-frame.core :as rf]
            [metaforms.common.dictionary :refer [l]]
            [metaforms.common.logic :as cl]
            [metaforms.modules.samples.db :as samples.db]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.complex-forms.validation-logic :as vl]))

;; release build must be served through the same domain and port
(def api-host (if goog.DEBUG  "http://localhost:8000/api/" "/api/"))

(def persistent-path "query/persistent/")
(def base-uri (str api-host persistent-path "complex-tables/?id={id}&middleware=complex_forms&depth=1"))
(def persistent-post-base-uri (str api-host persistent-path ":complex-id/"))
(def persistent-put-base-uri (str persistent-post-base-uri ":id/"))
(def persistent-delete-base-uri (str api-host persistent-path "delete/:complex-id/:id/"))

(def validations-path "service/get/{service}/{method}/")
(def validation-base-url (str api-host validations-path))

(rf/reg-event-fx
 :set-form-definition
 (fn [{db :db} [_ form-pk]]
   (let [form-id (-> (str form-pk) str/lower-case (str/replace #"_" "-") keyword)]
     (if-let [form (cf.logic/get-form db form-id)]
       {:dispatch [:set-current-form form-id]}
       {:dispatch [:load-form-definition form-pk form-id]}))))

(rf/reg-event-fx
 :load-form-definition
 (fn [{db :db} [_ form-pk form-id]]
   {:dispatch [:http-get
               (cl/replace-tag base-uri "id" form-pk)
               [::load-form-definition-success form-id]
               [::load-form-definition-failure]]}))

(defn load-form-definition-success [form-id response db]
  (let [form-definition (-> response :data first)]
    (assoc-in db [:complex-forms form-id] {:definition form-definition
                                           :state      :view
                                           :data       {:records        []
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
   {:dispatch [:http-post
               (cf.logic/post-form-data-url db persistent-post-base-uri)
               {:data (cf.logic/data-record->typed-data (cf.logic/editing-data db)
                                                        (cf.logic/fields-defs db))}
               [::form-confirm-success]
               [::form-confirm-failure]]}))

(rf/reg-event-fx
 ::form-confirm-success
 (fn [{db :db} [_ response]]
   (let [new-records          (cf.logic/records<-new-data db (-> response :data first))
         new-record?          (cf.logic/new-record? db)
         current-record-index (cf.logic/current-record-index db)
         current-record       (if new-record? (-> new-records count dec) current-record-index)]
     {:db       (cf.logic/set-current-form-data db {:new-record?    false
                                                    :editing-data   nil
                                                    :current-record current-record
                                                    :records        new-records})
      :dispatch [:set-current-form-state :view]})))

(rf/reg-event-fx
 ::form-confirm-failure
 (fn [{db :db} [_ result]]
   (js/console.log "ERROR" result)))

(rf/reg-event-fx
 :do-form-delete
 (fn [{db :db} _]
   (if (cf.logic/current-record-index db)
     {:dispatch [:ask-for-confirmation (l :form/confirm-delete?) :do-confirmed-form-delete]})))

(rf/reg-event-fx
 :do-confirmed-form-delete
 (fn [{db :db} _]
   (let [after-delete-records (cf.logic/delete-current-record db)]
     {:db       (cf.logic/set-current-form-data db {:records        after-delete-records
                                                    :editing-data   nil
                                                    :new-record?    false
                                                    :current-record (cf.logic/record-index-after-delete db after-delete-records)})
      :dispatch [:set-current-form-state :view]})))

(rf/reg-event-fx
 :do-input-focus
 (fn [db [_ field-name]]
   (cf.logic/set-current-form-data db {:editing field-name})))

(defn db-on-blur [db field-name field-value]
  (cf.logic/set-current-form-data db {:editing      nil
                                      :editing-data (assoc
                                                     (cf.logic/editing-data db)
                                                     (keyword field-name)
                                                     field-value)}))

(rf/reg-event-fx
 :input-blur
 (fn [{db :db} [_ field-name field-value]]
   {:db       (db-on-blur db field-name field-value)
    :dispatch [:field-value-changed field-name field-value]}))

(rf/reg-event-db
 :field-value-changed
 (fn [db [_ field-name field-value]]
   (assoc db :last-modified-field {:name field-name :value field-value})))

(rf/reg-event-db
 :set-current-form-state
 (fn [db [_ new-state]]
   (assoc-in db [:complex-forms (:current-form db) :state] new-state)))

;; this event only will be triggered by input if
;; - there is a validation definition for this field
;; - field value changed
;; - field value is not empty
(rf/reg-event-fx
 :validate-field
 (fn [{db :db} [_ validation field-name new-value]]
   ;; sends http request
   (let [url (vl/build-validation-url (db-on-blur db field-name new-value)
                                      validation-base-url
                                      validation new-value)]
     {:dispatch [:http-get url
                 [::validate-field-success validation field-name new-value]
                 [::validate-field-error validation field-name]]
      :db       (cl/set-spinner db true)})))

(rf/reg-event-fx
 ::validate-field-success
 (fn [{db :db} [_ validation field-name new-value response]]
   {:db       (cf.logic/set-current-form-data
               (cl/set-spinner db false)
               {:editing      nil
                :editing-data (cf.logic/set-editing-data db (vl/expected-results->fields validation response))})
    :dispatch [:input-blur field-name new-value]}))

(rf/reg-event-fx
 ::validate-field-error
 (fn [{db :db} [_ validation field-name result]]
   (merge
    {:db (cl/set-spinner db false)}
    (when (:show-message-on-error validation)
      {:dispatch [:show-modal-alert (l :common/warning) (-> result :response :data :messages :pt-br)]}))))
