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
   (:state (cf.logic/current-form db))))

(rf/reg-sub
 :current-form-data
 (fn [db]
   (-> db :current-form-data)))
