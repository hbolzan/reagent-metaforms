(ns metaforms.modules.complex-forms.events
  (:require [clojure.string :as str]
            [metaforms.common.dictionary :refer [l error-result->error-message]]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.components.search :as search]
            [metaforms.modules.complex-forms.constants :as cf.consts]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.complex-forms.validation-logic :as vl]
            [re-frame.core :as rf]
            [re-frame.db :as rdb]))

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

(rf/reg-event-fx
 ::form-load-data-success
 (fn [{db :db} [_ form-id response]]
   {:db (cf.logic/form-by-id-set-data db form-id {:records (:data response)})}))

(rf/reg-event-fx
 ::form-load-data-failure
 (fn [{db :db} [_ form-id result]]
   {:dispatch [:show-modal-alert
               (l :common/error)
               (str (l :form/load-data-failure {:form-id form-id}) ". "
                    (error-result->error-message result (l :error/unknown)))]}))

(rf/reg-event-db
 :form-set-active-page
 (fn [db [_ form-id page-index]]
   (cf.logic/form-by-id-set-some-prop db form-id :active-page page-index)))

(rf/reg-event-db
 :form-save-input-local-value
 (fn [db [_ field-id input-value]]
   (cf.logic/current-form-save-input-value db field-id input-value)))

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
   (let [current-state (cf.logic/form-by-id-state db form-id)
         next-state    (cf.logic/next-form-state form-action current-state)]
     (when (not= current-state next-state)
       {:dispatch [(keyword (str "do-form-"(name form-action))) form-id]}))))

;; SEARCH
(rf/reg-event-fx
 :do-form-search
 (fn [{db :db} [_ form-id]]
   (search/start db form-id)))

(rf/reg-event-fx
 :search-button-click
 (fn [{db :db} [_ form-id search-value]]
   {:dispatch [:http-get
               (cf.logic/form-by-id-data-url db form-id (str cf.consts/persistent-get-base-uri "?_search_=" search-value))
               [::form-load-data-success form-id]
               [::form-load-data-failure form-id]]}))

(rf/reg-event-fx
 :search-grid-select-cell
 (fn [{db :db} [_ form-id selected-cell]]
   {:db (cf.logic/form-by-id-set-data db form-id {:search {:selected-cell (cl/js-map->clj-map selected-cell)}})}))

(rf/reg-event-fx
 :search-grid-select-row
 (fn [{db :db} [_ form-id row-index]]
   {:db (cf.logic/form-by-id-set-data db form-id {:search {:selected-row row-index}})}))

(rf/reg-event-fx
 :form-search-select-record
 (fn [{db :db} [_ form-id row-index]]
   {:db       (cf.logic/form-by-id-set-data db form-id {:current-record row-index})
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

;; REFRESH
(rf/reg-event-fx
 :do-form-refresh
 (fn [{db :db} [_ form-id]]
   {:dispatch [:http-get
               (cf.logic/replace-url-tag
                (cf.logic/current-form-data-url db cf.consts/persistent-get-one)
                "pk"
                (-> db cf.logic/current-record-pk-values first))
               ;; [::form-confirm-success form-id]
               [::form-refresh-success]
               [::form-confirm-failure form-id]]}))

(rf/reg-event-fx
 ::form-refresh-success
 (fn [{db :db} [_ response]]
   (let [chilren  (-> (get (:complex-forms db) (:current-form db)) :definition :children)]
     )
   ))

(rf/reg-event-fx
 ::form-refresh-failure
 (fn [{db :db} [_ result]]
   {:dispatch [:show-modal-alert
               (l :common/error)
               (error-result->error-message result (l :error/unknown))]}))

;; NAVIGATION ACTIONS
(rf/reg-event-fx
 :do-form-nav-first
 (fn [{db :db} [_ form-id]]
   {:db (cf.logic/form-by-id-set-record-index db form-id 0)}))

(rf/reg-event-fx
 :do-form-nav-prior
 (fn [{db :db} [_ form-id]]
   (let [record-index (cf.logic/form-by-id-current-record-index db form-id)
         prior-index  (if (> record-index 0) (dec record-index) record-index)]
     {:db (cf.logic/form-by-id-set-record-index db form-id prior-index)})))

(rf/reg-event-fx
 :do-form-nav-next
 (fn [{db :db} [_ form-id]]
   (let [record-index (cf.logic/form-by-id-current-record-index db form-id)
         record-count (count (cf.logic/form-by-id-current-records db form-id))
         next-index   (if (< record-index (dec record-count)) (inc record-index) record-index)]
     {:db (cf.logic/form-by-id-set-record-index db form-id next-index)})))

(rf/reg-event-fx
 :do-form-nav-last
 (fn [{db :db} [_ form-id]]
   (let [record-count (count (cf.logic/current-records db))]
     {:db (cf.logic/form-by-id-set-record-index db form-id (when (> record-count 0) (dec record-count)))})))

(defn concat-prefix [prefix row]
  (into {} (map (fn [[k v]] [(keyword (str (name prefix) "." (name k))) v]) row)))

(defn all-rendered-rows [db form-id]
  "Retrieves the rendered row for each child grid"
  (reduce (fn [m [child-id data]] (merge m (concat-prefix child-id (:row data))))
          {}
          (get-in db [:rendered-rows (-> form-id namespace keyword)])))

;; CRUD ACTIONS
(rf/reg-event-fx
 :do-form-edit
 (fn [{db :db} [_ form-id]]
   (if-let [current-record (cf.logic/form-by-id-current-data-record db form-id)]
     {:db       (cf.logic/form-by-id-set-data db form-id {:new-record?  false
                                                          :editing-data (merge (cf.logic/current-data-record db)
                                                                               (all-rendered-rows db form-id))})
      :dispatch [:set-current-form-state form-id :edit]}
     {:dispatch [:do-form-append form-id]})))

(rf/reg-event-fx
 :do-form-append
 (fn [{db :db} [_ form-id]]
   {:db       (cf.logic/form-by-id-set-data db
                                            form-id
                                            {:new-record?  true
                                             :editing-data (cf.logic/new-record (cf.logic/fields-defs db form-id))})
    :dispatch [:set-current-form-state form-id :edit]}))

(rf/reg-event-fx
 :do-form-discard
 (fn [{db :db} [_ form-id]]
   {:db       (cf.logic/form-by-id-set-data db form-id {:new-record?  false
                                                        :editing-data nil})
    :dispatch [:set-current-form-state form-id :view]}))

(rf/reg-event-fx
 :do-form-confirm
 (fn [{db :db} [_ form-id]]
   (let [new-record? (cf.logic/form-by-id-new-record? db form-id)]
     {:dispatch [:ask-for-confirmation
                 (l (if new-record? :form/confirm-append? :form/confirm-edit?))
                 [(if new-record? :do-confirmed-form-confirm-append :do-confirmed-form-confirm-edit) form-id]]})))

(defn form-confirm-dispatch-data [db form-id method url]
  [method
   url
   {:data (cf.logic/data-record->typed-data (cf.logic/current-form-editing-data db)
                                            (cf.logic/fields-defs db))}
   [::form-confirm-success form-id]
   [::form-confirm-failure form-id]])

(rf/reg-event-fx
 :do-confirmed-form-confirm-append
 (fn [{db :db} [_ form-id]]
   {:dispatch (form-confirm-dispatch-data
               db
               form-id
               :http-post
               (cf.logic/current-form-data-url db cf.consts/persistent-post-base-uri))}))

(rf/reg-event-fx
 :do-confirmed-form-confirm-edit
 (fn [{db :db} [_ form-id]]
   {:dispatch (form-confirm-dispatch-data
               db
               form-id
               :http-put
               (cf.logic/current-form-replace-url-with-pk db cf.consts/persistent-put-base-uri "id"))}))

(rf/reg-event-fx
 ::form-confirm-success
 (fn [{db :db} [_ form-id response]]
   (let [new-records          (cf.logic/records<-new-data db (-> response :data first))
         current-record-index (cf.logic/current-record-index db)
         current-record       (if (cf.logic/new-record? db) (-> new-records count dec) current-record-index)]
     {:db       (cf.logic/current-form-set-data db {:new-record?    false
                                                    :editing-data   nil
                                                    :current-record current-record
                                                    :records        new-records})
      :dispatch [:set-current-form-state form-id :view]})))

(rf/reg-event-fx
 ::form-confirm-failure
 (fn [{db :db} [_ form-id result]]
   {:dispatch [:show-modal-alert
               (l :common/error)
               (str (l :form/load-data-failure {:form-id form-id}) ". "
                    (error-result->error-message result (l :error/unknown)))]}))

(rf/reg-event-fx
 :do-form-delete
 (fn [{db :db} [_ form-id]]
   (if (cf.logic/form-by-id-current-record-index db form-id)
     {:dispatch [:ask-for-confirmation (l :form/confirm-delete?) [:do-form-delete-remote form-id]]})))

(rf/reg-event-fx
 :do-form-delete-remote
 (fn [{db :db} [_ form-id]]
   {:dispatch [:http-delete
               (cf.logic/form-by-id-replace-url-with-pk db form-id cf.consts/persistent-delete-base-uri "id")
               [:form-delete-remote-success form-id]
               [:form-delete-remote-failure form-id]]}))

(rf/reg-event-fx
 :form-delete-remote-failure
 (fn [{db :db} _]
   {:dispatch [:show-modal-alert (l :common/error) (l :form/delete-failure)]}))

(rf/reg-event-fx
 :form-delete-remote-success
 (fn [{db :db} [_ form-id]]
   (let [after-delete-records (cf.logic/form-by-id-delete-current-record db form-id)]
     {:db       (cf.logic/form-by-id-set-data
                 db
                 form-id
                 {:records        after-delete-records
                  :editing-data   nil
                  :new-record?    false
                  :current-record (cf.logic/form-by-id-record-index-after-delete db
                                                                                 form-id
                                                                                 after-delete-records)})
      :dispatch [:set-current-form-state form-id :view]})))

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
 (fn [db [_ form-id new-state]]
   (cf.logic/form-by-id-set-some-prop db form-id :state new-state)))

;; this event only will be triggered by input if
;; - there is a validation definition for this field
;; - field value changed
;; - field value is not empty
(rf/reg-event-fx
 :validate-field
 (fn [{db :db} [_ validation field-name new-value]]
   ;; sends http request
   (let [value (cf.logic/typecast new-value
                                  (cf.logic/field-type-by-name db (:current-form db) field-name))
         url   (vl/build-validation-url (db-on-blur db field-name value)
                                        cf.consts/validation-base-url
                                        validation
                                        value)]
     {:dispatch [:http-get
                 url
                 [::validate-field-success validation field-name new-value]
                 [::validate-field-error validation field-name]]
      :db       (cl/set-spinner db true)})))

(rf/reg-event-fx
 ::validate-field-success
 (fn [{db :db} [_ validation field-name new-value response]]
   (let [validation-fx (vl/expected-results->fields validation response)]
     {:db (cf.logic/current-form-set-data
           (cl/set-spinner db false)
           {:editing       nil
            :editing-data  (cf.logic/set-current-form-editing-data
                            db
                            (into {} (map (fn [x] [(first x) (-> x last first)]) validation-fx)))
            :validation-fx validation-fx})})))

(rf/reg-event-fx
 ::validate-field-error
 (fn [{db :db} [_ validation field-name result]]
   (merge
    {:db (cl/set-spinner db false)}
    (when (:show-message-on-error validation)
      {:dispatch [:show-modal-alert (l :common/warning) (-> result :response :data :messages :pt-br)]}))))

(rf/reg-event-fx
 :clear-validation-fx-field
 (fn [{db :db} [_ form-id {field-name :name}]]
   (let [validation-fx (cf.logic/form-by-id-validation-fx db form-id)]
     {:db (cf.logic/form-by-id-set-data
           db
           form-id
           {:validation-fx (dissoc validation-fx (keyword field-name))})})))
