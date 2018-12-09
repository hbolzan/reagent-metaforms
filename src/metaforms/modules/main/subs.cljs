(ns metaforms.modules.main.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :sidebar-visible?
 (fn [db _]
   (-> db :main :sidebar-visible?)))

(rf/reg-sub
 :breadcrumb-items
 (fn [db _]
   (-> db :main :breadcrumb-items)))

(rf/reg-sub
 :current-view
 (fn [db _]
   (:current-view db)))
