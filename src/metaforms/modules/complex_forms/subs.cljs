(ns metaforms.modules.complex-forms.subs
  (:require [re-frame.core :as rf]
            [metaforms.modules.complex-forms.logic :as cf.logic]))

(rf/reg-sub
 :current-form
 (fn [db _]
   (cf.logic/current-form db)))

(rf/reg-sub
 :current-form-state
 (fn [db _]
   (cf.logic/current-form-state db)))

(rf/reg-sub
 :current-form-data
 (fn [db _]
   (cf.logic/current-form-data db)))

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
