(ns metaforms.modules.grid.subs
  (:require [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.common.logic :as cl]
            [re-frame.core :as rf]
            [clojure.string :as str]))

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
 :grid-rendered-rows
 (fn [db [_ form-id]]
   (get-in db [:rendered-rows (-> form-id namespace keyword)])))

(rf/reg-sub
 :grid-rendered-field
 (fn [db [_ form-id field-def]]
   (let [source-id (-> (:source field-def) name keyword)
         field-key (-> (:name field-def) (str/split ".") last keyword)]
     (get-in db [:rendered-rows (-> form-id namespace keyword) source-id :row field-key]))))

(rf/reg-sub
 :grid-rendered-element
 (fn [db [_ form-id field-def]]
   (let [source-id (-> (:source field-def) name keyword)
         field-key (-> (:name field-def) (str/split ".") last keyword)]
     (first (filter
             (fn [el] (= (:data-field-key el) field-key))
             (get-in db [:rendered-rows (-> form-id namespace keyword) source-id :elements]))))))

(rf/reg-sub
 :grid-pending?
 (fn [db [_ form-id]]
   (cf.logic/form-by-id-some-prop db form-id :pending?)))

(rf/reg-sub
 :deleted-rows
 (fn [db [_ form-id]]
   (cf.logic/form-by-id-some-prop db form-id :deleted-rows)))
