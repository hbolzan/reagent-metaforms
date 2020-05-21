(ns metaforms.modules.grid.logic
  (:require [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.logic :as cf.logic]))

(defn grid-state [data diff pending?]
  (if (empty? data)
    :empty
    (if (empty? diff) (if pending? :pending :view) :edit)))

(defn clear-pk-fields [pk-fields row]
  (reduce (fn [r field] (dissoc r (keyword field))) row pk-fields))

(defn clear-data-pk-fields [data auto-pk? pk-fields]
  (if auto-pk?
    (mapv #(clear-pk-fields pk-fields %) data)
    data))

(defn replace-related-fields [row related-fields related-values]
  (reduce-kv (fn [r i field] (assoc r (keyword field) (nth related-values i)))
             row
             related-fields))

(defn related-values [parent-data master-fields]
  (mapv #(-> % keyword parent-data) master-fields))

(defn fill-related-fields [data parent-data related-fields master-fields]
  (mapv #(replace-related-fields % related-fields (related-values parent-data master-fields)) data))

(defn review-appended-rows
  [{:keys [pk-fields related-fields master-fields] auto-pk? :auto-pk}
   parent-data
   appended-rows]
  (-> appended-rows
      (clear-data-pk-fields auto-pk? pk-fields)
      (fill-related-fields parent-data related-fields master-fields)))

(defn row-with-diff [data diff row-id]
  (let [row-index (first (cl/filter-index #(= (:__uuid__ %) row-id) data))]
    (merge (nth data row-index) (get diff row-id))))

(defn prepare-to-save [{:keys [definition parent-data] :as child-form} data deleted-rows]
  "Returns a map with rows to append, delete and update"
  (let [fields-defs (:fields-defs definition)
        row->typed  (fn [row] (cf.logic/data-record->typed-data row fields-defs))
        data->typed (fn [rows] (mapv row->typed rows))]
    {:meta   {:pk-fields      (:pk-fields definition)
              :related-fields (:related-fields definition)
              :related-values (related-values parent-data (:master-fields definition))}
     :delete (data->typed (filterv #(not (:append? %)) deleted-rows))
     :append (data->typed (review-appended-rows definition
                                                parent-data
                                                (filterv #(and (:append? %) (:update? %)) data)))
     :update (data->typed (filterv #(and (:update? %) (-> % :append? not)) data))}))

(defn build-validation-url [base-url editing-row])

(defn record-count [db form-id]
  (-> db (cf.logic/form-by-id-data form-id) :records count))

(defn selected-row-index [db form-id]
  (cf.logic/form-by-id-some-prop db form-id :selected-row))

(defn nav-index* [nav-op max-index current-index]
  (case nav-op
    :first 0
    :prior (-> current-index dec cl/zero-if-negative)
    :next  (-> current-index inc (cl/current-if-greater max-index))
    :last  max-index))

(defn nav-index [db form-id nav-op]
  (let [current-index (selected-row-index db form-id)
        max-index     (- (record-count db form-id) 1)]
    (nav-index* nav-op max-index current-index)))
