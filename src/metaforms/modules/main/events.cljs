(ns metaforms.modules.main.events
  (:require [re-frame.core :as rf]))

(rf/reg-event-db
 :initialize
 (fn [__]
   {:main {:sidebar-visible? true
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
