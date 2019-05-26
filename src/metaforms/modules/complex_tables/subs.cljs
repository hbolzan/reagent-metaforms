(ns metaforms.modules.complex-tables.subs
  (:require [re-frame.core :as rf]
            [metaforms.modules.complex-forms.logic :as cf.logic]))

(rf/reg-sub
 :complex-table-data
 (fn [db [_ table-id]]
   (cf.logic/form-by-id-data db table-id)))
