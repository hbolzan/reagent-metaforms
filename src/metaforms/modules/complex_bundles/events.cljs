(ns metaforms.modules.complex-bundles.events
  (:require [clojure.string :as str]
            [re-frame.db :as rdb]
            [re-frame.core :as rf]
            [metaforms.common.dictionary :refer [l error-result->error-message]]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.constants :as cf.consts]
            [metaforms.modules.complex-forms.components.search :as search]
            [metaforms.modules.complex-bundles.logic :as cb.logic]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.complex-forms.validation-logic :as vl]))

(rf/reg-event-fx
 :set-complex-bundle-definition
 (fn [{db :db} [_ bundle-id]]
   (if-let [bundle (cb.logic/get-bundle db bundle-id)]
     {:dispatch [:set-current-bundle (keyword bundle-id)]}
     {:dispatch
      [:load-complex-bundle-definition (keyword bundle-id)]})))

(rf/reg-event-fx
 :load-complex-bundle-definition
 (fn [{db :db} [_ bundle-id]]
   {:dispatch [:http-get
               (cl/replace-tag cf.consts/complex-bundles-base-uri "bundle-id" (name bundle-id))
               [::load-complex-bundle-success bundle-id]
               [::load-complex-bundle-failure bundle-id]]}))

(rf/reg-event-fx
 ::load-complex-bundle-success
 (fn [{db :db} [_ bundle-id response]]
   (let [bundle (-> response :data) db-with-bundles
         (cb.logic/load-bundle-definition-success db bundle-id bundle)]
     {:db (cb.logic/load-bundled-forms db-with-bundles
                                       bundle-id
                                       (:bundled-tables bundle)
                                       cf.logic/load-form-definition)
      :dispatch [:set-current-bundle bundle-id]})))

(rf/reg-event-fx
 ::load-complex-bundle-failure
 (fn [{db :db} [_ bundle-id result]]
   {:dispatch [:show-modal-alert
               (l :common/error)
               (str (l :bundle/load-definition-failure {:bundle-id bundle-id}) ". "
                    (error-result->error-message result (l :error/unknown)))]}))

(rf/reg-event-fx
 :set-current-bundle
 (fn [{db :db} [_ bundle-id]]
   (let [bundle            (cb.logic/get-bundle db bundle-id)
         bundled-forms-ids (map #(keyword (:id bundle) (:definition-id %)) (:bundled-tables bundle))]
     {:db       (assoc db :current-bundle bundle-id)
      :dispatch [:set-current-form (first bundled-forms-ids)]})))

(rf/reg-event-fx
 :complex-table-parent-data-changed
 (fn [{db :db} [_ table-id force?]]
   (let [bundle          (->> db :current-bundle (cb.logic/get-bundle db))
         bundled-table   (first (filter #(= (:definition-id %) (name table-id)) (:bundled-tables bundle)))
         parent-data     (or (cf.logic/current-form-editing-data db) (cf.logic/current-data-record db))
         old-parent-data (cf.logic/form-by-id-some-prop db (keyword (:id bundle) (:definition-id bundled-table)) :parent-data)
         master-fields   (:master-fields bundled-table)
         related-fields  (:related-fields bundled-table)]
     (let [with-parent-data (cf.logic/form-by-id-set-some-prop db table-id :parent-data parent-data)]
       (when (or force? (cb.logic/parent-data-changed? old-parent-data parent-data master-fields))
         (if (cb.logic/empty-parent-data? parent-data master-fields)
           {:db (cf.logic/form-by-id-set-data with-parent-data table-id {:records []})}
           {:db       with-parent-data
            :dispatch [:http-get (cb.logic/child-url db table-id parent-data bundled-table)
                       [::child-load-data-success table-id]
                       [::child-load-data-failure table-id]]}))))))

(rf/reg-event-fx
 :child-reset-data-records
 (fn [{db :db} [_ complex-table-id records]]
   {:dispatch [::child-load-data-success complex-table-id {:data records}] }))

(rf/reg-event-fx
 ::child-load-data-success
 (fn [{db :db} [_ complex-table-id response]]
   (let [records (-> response :data)]
     {:db (-> db
              (cf.logic/form-by-id-set-data complex-table-id {:records records})
              (cf.logic/form-by-id-set-record-index complex-table-id (when (count records) 0))
              (cf.logic/form-by-id-set-some-prop complex-table-id :request-id (random-uuid)))})))

(rf/reg-event-fx
 ::child-load-data-failure
 (fn [{db :db} [_ complex-table-id result]]
   {:dispatch [:show-modal-alert
               (str (l :form/load-data-failure {:form-id complex-table-id}) ". "
                    (error-result->error-message result (l :error/unknown)))]}))
