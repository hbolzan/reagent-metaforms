(ns metaforms.modules.grid.events
  (:require [metaforms.common.dictionary :refer [l error-result->error-message]]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.constants :as cf.consts]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.grid.logic :as grid.logic]
            [metaforms.modules.complex-forms.validation-logic :as vl]
            [re-frame.core :as rf]))

(rf/reg-event-fx
 :grid-clear-data-diff
 (fn [{db :db} [_ form-id]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :data-diff {})}))

(rf/reg-event-fx
 :grid-soft-refresh-on
 (fn [{db :db} [_ form-id]]
   {:db (-> db
            (cf.logic/form-by-id-set-some-prop form-id :soft-refresh? true)
            (cf.logic/form-by-id-set-some-prop form-id :request-id (random-uuid)))}))

(rf/reg-event-fx
 :grid-soft-refresh-off
 (fn [{db :db} [_ form-id]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :soft-refresh? false)}))

(rf/reg-event-fx
 :grid-data-change
 (fn [{db :db} [_ form-id]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :data-diff {})}))

(rf/reg-event-fx
 :grid-set-data-diff
 (fn [{db :db} [_ form-id row-id column-id value validation-params]]
   (let [data-atom (cf.logic/form-by-id-some-prop db form-id :data-atom)
         new-diff (assoc-in (cf.logic/form-by-id-some-prop db form-id :data-diff)
                             [row-id column-id]
                             value)]
     (merge
      {:db (cf.logic/form-by-id-set-some-prop db form-id :data-diff new-diff)}
      (when (not-empty value)
        {:dispatch [:grid-validate-field
                    (grid.logic/row-with-diff @data-atom new-diff row-id)
                    validation-params]})))))

(first (cl/filter-index #(= (:a %) 10) [{:a 1 :b 2}{:a 5 :b 20}{:a 10 :b 200}]))

(rf/reg-event-fx
 :grid-set-data-atom
 (fn [{db :db} [_ form-id data-atom]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :data-atom data-atom)}))

(rf/reg-event-fx
 :grid-set-state-atom
 (fn [{db :db} [_ form-id state-atom]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :state-atom state-atom)}))

(rf/reg-event-fx
 :grid-set-selected-row
 (fn [{db :db} [_ form-id row-index]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :selected-row row-index)}))

(rf/reg-event-fx
 :grid-set-pending-flag
 (fn [{db :db} [_ form-id pending?]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :pending? pending?)}))

(rf/reg-event-fx
 :grid-clear-deleted-rows
 (fn [{db :db} [_ form-id]]
   {:db (cf.logic/form-by-id-set-some-prop db form-id :deleted-rows [])}))

(rf/reg-event-fx
 :grid-add-deleted-row
 (fn [{db :db} [_ form-id row]]
   {:db (cf.logic/form-by-id-set-some-prop
         db form-id :deleted-rows (into (cf.logic/form-by-id-some-prop db form-id :deleted-rows) [row]))}))

(rf/reg-event-fx
 :grid-post-data
 (fn [{db :db} [_ form-id data]]
   {:dispatch [:http-post
               (cl/log (str (cf.logic/form-by-id-data-url db form-id cf.consts/persistent-post-base-uri) "batch/"))
               {:data data}
               [::grid-post-data-success form-id]
               [::grid-post-data-failure form-id]]}))

(rf/reg-event-fx
 ::grid-post-data-success
 (fn [{db :db} [_ form-id response]]
   {:dispatch [:complex-table-parent-data-changed form-id true]}))

(rf/reg-event-fx
 ::grid-post-data-failure
 (fn [{db :db} [_ form-id result]]
   {:dispatch [:show-modal-alert
               (l :common/error)
               [:div.card
                [:div.card-header (l :grid/save-failure {:form-id form-id})]
                [:div.card-body (error-result->error-message result (l :error/unknown))]]]}))

(rf/reg-event-fx
 :grid-validate-field
 (fn [{db :db} [_ row {:keys [validation on-success on-failure]}]]
   (js/console.log row)))

#_(rf/reg-event-fx
 :grid-validate-field
 (fn [{db :db} [_ validation success-fn failure-fn]]
   ;; sends http request
   (let [url (vl/build-validation-url (db-on-blur db field-name new-value)
                                      cf.consts/validation-base-url
                                      validation
                                      new-value)]
     {:dispatch [:http-get
                 url
                 [::grid-validate-field-on-response success-fn]
                 [::grid-validate-field-on-response failure-fn]]
      :db       (cl/set-spinner db true)})))

(rf/reg-event-fx
 ::grid-validate-field-on-response
 (fn [{db :db} [_  response-fn response]]
   (if response-fn (response-fn response))
   {:db (cl/set-spinner db false)}))
