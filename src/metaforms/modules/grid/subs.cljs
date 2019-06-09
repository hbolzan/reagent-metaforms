(ns metaforms.modules.grid.subs
  (:require [metaforms.modules.complex-forms.logic :as cf.logic]
            [re-frame.core :as rf]))

(rf/reg-sub
 :grid-data-diff
 (fn [db [_ form-id]]
   (cf.logic/form-by-id-some-prop db form-id :data-diff)))

(rf/reg-sub
 :grid-data-atom
 (fn [db [_ form-id]]
   (cf.logic/form-by-id-some-prop db form-id :data-atom)))

(rf/reg-sub
 :grid-state-atom
 (fn [db [_ form-id]]
   (cf.logic/form-by-id-some-prop db form-id :state-atom)))

(rf/reg-sub
 :grid-soft-refresh?
 (fn [db [_ form-id]]
   (cf.logic/form-by-id-some-prop db form-id :soft-refresh?)))

(rf/reg-sub
 :grid-selected-row
 (fn [db [_ form-id]]
   (cf.logic/form-by-id-some-prop db form-id :selected-row)))

(rf/reg-sub
 :grid-pending?
 (fn [db [_ form-id]]
   (cf.logic/form-by-id-some-prop db form-id :pending?)))
