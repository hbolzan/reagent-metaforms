(ns metaforms.modules.grid.logic)

(defn grid-state [data diff pending?]
  (if (empty? data)
    :empty
    (if (empty? diff) (if pending? :pending :view) :edit)))

(defn prepare-to-save [data deleted-rows]
  "Returns a map with rows to append, delete and update"
  {:delete (filterv #(not (:append? %)) deleted-rows)
   :append (filterv #(and (:append? %) (:update? %)) data)
   :update (filterv #(and (:update? %) (-> % :append? not)) data)})
