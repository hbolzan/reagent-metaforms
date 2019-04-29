(ns metaforms.modules.main.events
  (:require [re-frame.core :as rf]
            [metaforms.common.dictionary :refer [l]]
            [metaforms.common.logic :as cl]))

(rf/reg-event-db
 :initialize
 (fn [__]
   {:current-view :home
    :main         {:sidebar-visible? true
                   :sidebar-items    nil
                   :breadcrumb-items [{:label "Início"}]}}))

(rf/reg-event-db
 :toggle-sidebar
 (fn [db _]
   (assoc-in db [:main :sidebar-visible?] (-> db :main :sidebar-visible? not))))

(rf/reg-event-db
 :set-breadcrumbs
 (fn [db [_ breadcrumb-items]]
   (assoc-in db [:main :breadcrumb-items] breadcrumb-items)))

(rf/reg-event-db
 :set-view
 (fn [db [_ view]]
   (assoc db :current-view view)))

(rf/reg-event-db
 :modal-close
 (fn [db _]
   (assoc-in db [:modal :visible?] false)))

(rf/reg-event-fx
 :ask-for-confirmation
 (fn [{db :db} [_ confirmation-message next-action]]
   {:dispatch [:modal-confirmation-dialog {:title                (l :dialog/verifying)
                                           :content              confirmation-message
                                           :confirmation-action  next-action
                                           :dismiss-button-label (l :common/no)
                                           :ok-button-label      (l :common/yes)}]}))

(rf/reg-event-fx
 :show-modal-alert
 (fn [{db :db} [_ title message]]
   {:dispatch [:modal-confirmation-dialog {:title           title
                                           :content         message
                                           :ok-button-label (l :common/ok)
                                           :dismiss-button? false
                                           :on-confirm      (rf/dispatch [:modal-close])}]}))

(rf/reg-event-fx
 :show-modal-window
 (fn [{db :db} [_ title content next-action]]
   {:dispatch [:modal-confirmation-dialog {:title                title
                                           :content              content
                                           :confirmation-action  next-action
                                           :modal-dialog-class   "modal-window-dialog"
                                           :modal-content-class   "modal-window-content"
                                           :dismiss-button-label (l :modal/dismiss)
                                           :ok-button-label      (l :modal/select)}]}))

(comment
  ;; modal-confimation-dialog confirmation-action may be
  ;; - a keyword representing an event id
  ;; - a vector with dispatch params
  ;; - a function

  ;; expected params
  {title                :title
   content              :content
   dismiss-button-label :dismiss-button-label
   ok-button-label      :ok-button-label
   confirmation-action  :confirmation-action}
)

(rf/reg-event-db
 :modal-confirmation-dialog
 (fn [db [_ {:keys [confirmation-action] :as modal-params}]]
   (assoc db :modal (merge {:visible? true
                            :on-confirm (cl/action->action-fn confirmation-action)}
                           modal-params))))

(rf/reg-event-db
 :modal-spinner
 (fn [db [_ {:keys [visible?] :as spinner-params}]]
   (cl/set-spinner db visible?)))
