(ns metaforms.modules.main.events
  (:require [re-frame.core :as rf]))

(rf/reg-event-db
 :initialize
 (fn [__]
   {:current-view :home
    :main         {:sidebar-visible? true
                   :sidebar-items    nil
                   :breadcrumb-items [{:label "Início"}]
                   }}))

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
 :set-complex-form
 (fn [db [_ form-definition]]
   (merge db {:current-view :complex-form
              :current-form form-definition})))
