(ns metaforms.modules.complex-tables.events
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [metaforms.common.dictionary :refer [l error-result->error-message]]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.constants :as cf.consts]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.complex-forms.validation-logic :as vl]))

;; table-id is :namespace/form-pk
;; so complex tables may be used as child tables

(rf/reg-event-fx
 :complex-table-load-definition
 (fn [{db :db} [_ table-ns form-pk]]
   (let [table-id (keyword table-ns (cf.logic/form-pk->form-id form-pk))]
     (if-not (cf.logic/get-form db table-id)
       {:dispatch [:load-form-definition
                   form-pk
                   [::load-table-definition-success table-id]
                   [::load-table-definition-failure table-id]]}))))

(rf/reg-event-fx
 ::load-table-definition-success
 (fn [{db :db} [_ table-id response]]
   {:db        (cf.logic/load-form-definition-success table-id response db)
    :dispatch [:complex-table-load-data table-id]}))

(rf/reg-event-fx
 ::load-table-definition-failure
 (fn [{db :db} [_ table-id result]]
   {:dispatch [:show-modal-alert
               (l :common/error)
               (str (l :form/load-definition-failure {:form-id table-id}) ". "
                    (error-result->error-message result (l :error/unknown)))]}))

(rf/reg-event-fx
 :complex-table-load-data
 (fn [{db :db} [_ table-id]]
   {:dispatch [:http-get
               (cf.logic/form-data-url db cf.consts/persistent-get-base-uri)
               [::table-load-data-success table-id]
               [::table-load-data-failure table-id]]}))

(rf/reg-event-fx
 ::table-load-data-success
 (fn [{db :db} [_ table-id response]]
   {:db (cf.logic/form-by-id-set-data db table-id {:records (:data response)})}))

(rf/reg-event-fx
 ::table-load-data-failure
 (fn [{db :db} [_ table-id result]]
   {:dispatch [:show-modal-alert
               (l :common/error)
               (str (l :form/load-data-failure {:form-id table-id}) ". "
                    (error-result->error-message result (l :error/unknown)))]}))
