(ns metaforms.modules.complex-forms.subs
  (:require [re-frame.core :as rf]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.logic :as cf.logic]))

(rf/reg-sub
 :current-form-id
 (fn [db _]
   (:current-form db)))

(rf/reg-sub
 :current-form
 (fn [db _]
   (cf.logic/current-form db)))

(rf/reg-sub
 :form-by-id
 (fn [db [_ form-id]]
   (cf.logic/get-form db form-id)))

(rf/reg-sub
 :form-by-id-definition
 (fn [db [_ form-id]]
   (cf.logic/form-by-id-definition db form-id)))

(rf/reg-sub
 :current-form-state
 (fn [db _]
   (cf.logic/current-form-state db)))

(rf/reg-sub
 :current-form-new-record?
 (fn [db _]
   (cf.logic/new-record? db)))

(rf/reg-sub
 :form-by-id-state
 (fn [db [_ form-id]]
   (cf.logic/form-by-id-state db form-id)))

(rf/reg-sub
 :current-record-index
 (fn [db _]
   (cf.logic/current-record-index db)))

(rf/reg-sub
 :form-by-id-active-page
 (fn [db [_ form-id]]
   (or (cf.logic/form-by-id-some-prop db form-id :active-page) 0)))

(rf/reg-sub
 :form-by-id-record-index
 (fn [db [_ form-id]]
   (cf.logic/form-by-id-current-record-index db form-id)))

(rf/reg-sub
 :current-record
 (fn [db _]
   (cf.logic/current-data-record db)))

(rf/reg-sub
 :current-form-editing-data
 (fn [db _]
   (cf.logic/current-form-editing-data db)))

(rf/reg-sub
 :form-by-id-validation-fx
 (fn [db [_ form-id {field-name :name}]]
   (cf.logic/form-by-id-validation-fx-field-by-name db form-id field-name)))

(rf/reg-sub
 :current-form-data
 (fn [db _]
   (cf.logic/current-form-data db)))

(rf/reg-sub
 :form-by-id-data
 (fn [db [_ form-id]]
   (cf.logic/form-by-id-data db form-id)))

(rf/reg-sub
 :form-by-id-request-id
 (fn [db [_ form-id]]
   (cf.logic/form-by-id-some-prop db form-id :request-id)))

(rf/reg-sub
 :linked-field-changed
 (fn [db [_ form-id]]
   (:last-modified-field (cf.logic/form-by-id-some-prop db form-id :linked-fields))))

(rf/reg-sub
 :current-form-records
 (fn [db _]
   (cf.logic/current-records db)))

(rf/reg-sub
 :field-value
 (fn [db [_ field-name]]
   (when (not-empty field-name)
     (let [{editing           :editing
            editing-data      :editing-data
            records           :records
            current-record-id :current-record} (cf.logic/current-form-data db)
           field-key                           (keyword field-name)
           current-value                       (if current-record-id
                                                 (or (field-key (get records current-record-id)) "")
                                                 "")
           editing-value                       (when editing-data (field-key editing-data))]
       (when-not (= editing field-name) (or editing-value current-value))))))

(rf/reg-sub
 :last-modified-field
 (fn [db _]
   (:last-modified-field db)))

(rf/reg-sub
 :last-modified-fields
 (fn [db _]
   (:last-modified-fields db)))

(rf/reg-sub
 :search-selected-row-index
 (fn [db [_ form-id]]
   (-> (cf.logic/form-by-id-data db form-id) :search :selected-row)))
