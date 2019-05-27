(ns metaforms.modules.complex-bundles.events
  (:require [clojure.string :as str]
            [re-frame.db :as rdb]
            [re-frame.core :as rf]
            [metaforms.common.dictionary :refer [l error-result->error-message]]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.constants :as cf.consts]
            [metaforms.modules.complex-forms.components.search :as search]
            [metaforms.modules.complex-bundles.logic :as cb.logic]
            [metaforms.modules.complex-forms.validation-logic :as vl]))

(rf/reg-event-fx
 :set-complex-bundle-definition
 (fn [{db :db} [_ bundle-id]]
   (if-let [bundle (cb.logic/get-bundle bundle-id)]
     {:dispatch [:set-current-bundle bundle-id]}
     {:dispatch [:load-complex-bundle-definition bundle-id]})))

(rf/reg-event-fx
 :load-complex-bundle-definition
 (fn [{db :db} [_ bundle-id]]
   {:dispatch [:http-get
               (cl/replace-tag cf.consts/complex-bundles-base-uri "bundle-id" bundle-id)
               [::load-complex-bundle-success bundle-id]
               [::load-complex-bundle-failure bundle-id]]}))

(rf/reg-event-fx
 ::load-complex-bundle-success
 (fn [{db :db} [_ bundle-id response]]
   {:db (assoc-in db [:complex-bundles bundle-id] (-> response :data first))
    :dispatch [:set-current-bundle bundle-id]}))

(rf/reg-event-fx
 ::load-complex-bundle-failure
 (fn [{db :db} [_ bundle-id result]]
   {:dispatch [:show-modal-alert
               (l :common/error)
               (str (l :bundle/load-definition-failure {:bundle-id bundle-id}) ". "
                    (error-result->error-message result (l :error/unknown)))]}))
