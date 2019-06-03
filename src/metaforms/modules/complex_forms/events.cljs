(ns metaforms.modules.complex-forms.events
  (:require [clojure.string :as str]
            [re-frame.db :as rdb]
            [re-frame.core :as rf]
            [metaforms.common.dictionary :refer [l error-result->error-message]]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.constants :as cf.consts]
            [metaforms.modules.complex-forms.components.search :as search]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.complex-forms.validation-logic :as vl]))

(rf/reg-event-fx
 :set-form-definition
 (fn [{db :db} [_ form-pk]]
   (let [form-id (cf.logic/form-pk->form-id form-pk)]
     (if-let [form (cf.logic/get-form db form-id)]
       {:dispatch [:set-current-form form-id]}
       {:dispatch [:load-form-definition
                   form-pk
                   [::load-form-definition-success form-id]
                   [::load-form-definition-failure form-id]]}))))

(rf/reg-event-fx
 :load-form-definition
 (fn [{db :db} [_ form-pk on-success on-failure]]
   {:dispatch [:http-get
               (cl/replace-tag cf.consts/base-uri "id" form-pk)
               on-success
               on-failure]}))

(rf/reg-event-fx
 ::load-form-definition-success
 (fn [{db :db} [_ form-id response]]
   {:db        (cf.logic/load-form-definition-success db form-id response)
    :dispatch [:set-current-form form-id]}))

(rf/reg-event-fx
 ::load-form-definition-failure
 (fn [{db :db} [_ form-id result]]
   {:dispatch [:show-modal-alert
               (l :common/error)
               (str (l :form/load-definition-failure {:form-id form-id}) ". "
                    (error-result->error-message result (l :error/unknown)))]}))

(rf/reg-event-fx
 :set-current-form
 (fn [{db :db} [_ form-id]]
   {:db (merge db {:current-view :complex-form
                   :current-form form-id})}))

;; (rf/reg-event-fx
;;  :form-load-data
;;  (fn [{db :db} [_ form-id]]
;;    {:dispatch [:http-get
;;                (cf.logic/current-form-data-url db cf.consts/persistent-get-base-uri)
;;                [::form-load-data-success]
;;                [::form-load-data-failure form-id]]}))

(rf/reg-event-fx
 ::form-load-data-success
 (fn [{db :db} [_ response]]
   {:db (cf.logic/current-form-set-data db {:records (:data response)})}))

(rf/reg-event-fx
 ::form-load-data-failure
 (fn [{db :db} [_ form-id result]]
   {:dispatch [:show-modal-alert
               (l :common/error)
               (str (l :form/load-data-failure {:form-id form-id}) ". "
                    (error-result->error-message result (l :error/unknown)))]}))

(rf/reg-event-fx
 :current-form-current-record-changed
 (fn [{db :db} _]
   ;; update all children
   ;; (when (children? {:dispatch [:update-children]}))
   ))

;; (cf.logic/current-form-data-url db cf.consts/persistent-get-base-uri)

;; form actions
;; :append :edit :confirm :discard :delete :search :refresh
;; data navigation
;; :nav-first :nav-prior :nav-next :nav-last

;; TOOLSET ENTRY POINT
(rf/reg-event-fx
 :do-form-action
 (fn [{db :db} [_ form-action form-id]]
   (js/console.log form-id)
   (let [current-state (cf.logic/current-form-state db)
         next-state    (cf.logic/next-form-state form-action current-state)]
     (when (not= current-state next-state)
       {:dispatch [(keyword (str "do-form-"(name form-action)))]}))))

;; SEARCH
(rf/reg-event-fx
 :do-form-search
 (fn [{db :db} _]
   {:dispatch [:show-modal-window
               "Search" ;; TODO: get title from dictionary
               [search/data-grid
                (cf.logic/fields-defs db)
                (fn [search-value] (rf/dispatch [:search-button-click search-value]))
                (fn [row-index selected-object] (rf/dispatch [:form-search-select-record row-index]))
                (fn [selected-cell] (rf/dispatch [:search-grid-select-cell selected-cell]))]
               #(rf/dispatch
                 [:form-search-select-record
                  (get-in (cf.logic/current-form-data @rdb/app-db) [:search :selected-cell :rowIdx])])]}))

(rf/reg-event-fx
 :search-button-click
 (fn [{db :db} [_ search-value]]
   {:dispatch [:http-get
               (cf.logic/current-form-data-url db (str cf.consts/persistent-get-base-uri "?_search_=" search-value))
               [::form-load-data-success]
               [::form-load-data-failure]]}))

(rf/reg-event-fx
 :search-grid-select-cell
 (fn [{db :db} [_ selected-cell]]
   {:db (cf.logic/current-form-set-data db {:search {:selected-cell (cl/js-map->clj-map selected-cell)}})}))

(rf/reg-event-fx
 :form-search-select-record
 (fn [{db :db} [_ row-index]]
   {:db       (cf.logic/current-form-set-data db {:current-record row-index})
    :dispatch [:modal-close]}))

(rf/reg-event-fx
 :form-search-query
 (fn [{db :db} _]
   {:dispatch [:http-get
               (cf.logic/current-form-data-url db cf.consts/persistent-get-base-uri)
               [::form-search-query-success]
               [::form-search-query-failure]]}))

(rf/reg-event-fx
 ::form-search-query-success
 (fn [{db :db} [_ response]]
   ;;(:current-form db)
   {:dispatch [:show-modal-window "Search" "Search window test" nil]}))

(rf/reg-event-fx
 ::form-search-query-failure
 (fn [{db :db} [_ result]]
   {:dispatch [:show-modal-alert (l :common/error) (l :form/search-failure)]}))

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
     {:db       (cf.logic/current-form-set-data db {:new-record?  false
                                                    :editing-data (cf.logic/current-data-record db)})
      :dispatch [:set-current-form-state :edit]}
     {:dispatch [:do-form-append]})))

(rf/reg-event-fx
 :do-form-append
 (fn [{db :db} _]
   {:db       (cf.logic/current-form-set-data db {:new-record?  true
                                                  :editing-data (cf.logic/new-record (cf.logic/fields-defs db))})
    :dispatch [:set-current-form-state :edit]}))

(rf/reg-event-fx
 :do-form-discard
 (fn [{db :db} _]
   {:db       (cf.logic/current-form-set-data db {:new-record?  false
                                                  :editing-data nil})
    :dispatch [:set-current-form-state :view]}))

(rf/reg-event-fx
 :do-form-confirm
 (fn [{db :db} _]
   (let [new-record? (cf.logic/new-record? db)]
     {:dispatch [:ask-for-confirmation
                 (l (if new-record? :form/confirm-append? :form/confirm-edit?))
                 (if new-record? :do-confirmed-form-confirm-append :do-confirmed-form-confirm-edit)]})))

(defn form-confirm-dispatch-data [db method url]
  [method
   url
   {:data (cf.logic/data-record->typed-data (cf.logic/current-form-editing-data db)
                                            (cf.logic/fields-defs db))}
   [::form-confirm-success]
   [::form-confirm-failure]])

(rf/reg-event-fx
 :do-confirmed-form-confirm-append
 (fn [{db :db} _]
   {:dispatch (form-confirm-dispatch-data
               db
               :http-post
               (cf.logic/current-form-data-url db cf.consts/persistent-post-base-uri))}))

(rf/reg-event-fx
 :do-confirmed-form-confirm-edit
 (fn [{db :db} _]
   {:dispatch (form-confirm-dispatch-data
               db
               :http-put
               (cf.logic/current-form-replace-url-with-pk db cf.consts/persistent-put-base-uri "id"))}))

(rf/reg-event-fx
 ::form-confirm-success
 (fn [{db :db} [_ response]]
   (let [new-records          (cf.logic/records<-new-data db (-> response :data first))
         current-record-index (cf.logic/current-record-index db)
         current-record       (if (cf.logic/new-record? db) (-> new-records count dec) current-record-index)]
     {:db       (cf.logic/current-form-set-data db {:new-record?    false
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
     {:dispatch [:ask-for-confirmation (l :form/confirm-delete?) :do-form-delete-remote]})))

(rf/reg-event-fx
 :do-form-delete-remote
 (fn [{db :db} _]
   {:dispatch [:http-delete
               (cf.logic/current-form-replace-url-with-pk db cf.consts/persistent-delete-base-uri "id")
               [:form-delete-remote-success]
               [:form-delete-remote-failure]]}))

(rf/reg-event-fx
 :form-delete-remote-failure
 (fn [{db :db} _]
   {:dispatch [:show-modal-alert (l :common/error) (l :form/delete-failure)]}))

(rf/reg-event-fx
 :form-delete-remote-success
 (fn [{db :db} _]
   (let [after-delete-records (cf.logic/delete-current-record db)]
     {:db       (cf.logic/current-form-set-data db {:records        after-delete-records
                                                    :editing-data   nil
                                                    :new-record?    false
                                                    :current-record (cf.logic/record-index-after-delete db after-delete-records)})
      :dispatch [:set-current-form-state :view]})))

(rf/reg-event-fx
 :do-input-focus
 (fn [db [_ field-name]]
   (cf.logic/current-form-set-data db {:editing field-name})))

(defn db-on-blur [db field-name field-value]
  (cf.logic/current-form-set-data db {:editing      nil
                                      :editing-data (assoc
                                                     (cf.logic/current-form-editing-data db)
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
                                      cf.consts/validation-base-url
                                      validation new-value)]
     {:dispatch [:http-get url
                 [::validate-field-success validation field-name new-value]
                 [::validate-field-error validation field-name]]
      :db       (cl/set-spinner db true)})))

(rf/reg-event-fx
 ::validate-field-success
 (fn [{db :db} [_ validation field-name new-value response]]
   {:db       (cf.logic/current-form-set-data
               (cl/set-spinner db false)
               {:editing      nil
                :editing-data (cf.logic/set-current-form-editing-data db (vl/expected-results->fields validation response))})
    :dispatch [:input-blur field-name new-value]}))

(rf/reg-event-fx
 ::validate-field-error
 (fn [{db :db} [_ validation field-name result]]
   (merge
    {:db (cl/set-spinner db false)}
    (when (:show-message-on-error validation)
      {:dispatch [:show-modal-alert (l :common/warning) (-> result :response :data :messages :pt-br)]}))))
