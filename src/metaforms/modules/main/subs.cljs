(ns metaforms.modules.main.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :sidebar-visible?
 (fn [db _]
   (-> db :main :sidebar-visible?)))

(rf/reg-sub
 :menu-items
 (fn [db _]
   (-> db :main :menu-items)))

(rf/reg-sub
 :breadcrumb-items
 (fn [db _]
   (-> db :main :breadcrumb-items)))

(rf/reg-sub
 :current-view
 (fn [db _]
   (:current-view db)))

(rf/reg-sub
 :modal-visible?
 (fn [db _]
   (-> db :modal :visible?)))

(rf/reg-sub
 :modal-params
 (fn [db _]
   (-> db :modal)))

(rf/reg-sub
 :spinner-params
 (fn [db _]
   (-> db :spinner)))
