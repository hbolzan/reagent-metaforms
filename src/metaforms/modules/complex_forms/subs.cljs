(ns metaforms.modules.complex-forms.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :current-form-state
 (fn [db _]
   (-> db :current-form :state)))
