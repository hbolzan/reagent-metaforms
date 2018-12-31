(ns metaforms.modules.complex-forms.events
  (:require [re-frame.core :as rf]))

(rf/reg-event-db
 :set-current-form-state
 (fn [db [_ new-state]]
   (assoc-in db [:current-form :state] new-state)))
