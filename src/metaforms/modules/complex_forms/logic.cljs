(ns metaforms.modules.complex-forms.logic
  (:require [re-frame.db :as rdb]
            [cljs-time.core :as tc]
            [cljs-time.format :as tf]
            [clojure.string :as str]
            [metaforms.common.logic :as cl]))

(def empty-row {:width 0 :widths []})
(def field-width-multiplier 7)
(def bootstrap-grid-cols 12)
(def bootstrap-md-width 720)
(def date-formatter (tf/formatter "yyyy-MM-dd"))
(def date-time-formatter (tf/formatter "yyyy-MM-dd'T'HH':'mm':'ssZ"))

(defn set-last [coll x]
  (if (< (count coll) 1)
    [x]
    (conj (pop coll) x)))

(defn add-width-to-row [row width]
  (merge row {:width  (+ (:width row) width)
              :widths (conj (:widths row) width)}))

(defn add-to-row? [row width container-width]
  (boolean (and (< (count (:widths row)) bootstrap-grid-cols)
                (<= (+ (:width row) width) container-width))))

(defn row-reducer [container-width rows width]
  (let [current-row (last rows)]
    (if (add-to-row? current-row width container-width)
      (set-last rows (add-width-to-row current-row width))
      (conj rows {:width width :widths [width]}))))

(defn distribute-widths [widths container-width]
  (reduce (partial row-reducer container-width) [empty-row] widths))

(defn review-grid-widths [widths grid-size]
  (let [total-width (cl/sum widths)]
    (cond
      (< (cl/sum widths) grid-size) (review-grid-widths (cl/inc-nth widths (cl/min-index widths)) grid-size)
      (> (cl/sum widths) grid-size) (review-grid-widths (cl/dec-nth widths (cl/max-index widths)) grid-size)
      :else                         widths)))

(defn row-widths->grid-widths [grid-size row]
  (let [row-width (:width row)
        rate      (/ row-width grid-size)]
    (review-grid-widths (mapv (fn [width] (-> (/ width rate) double Math/round)) (:widths row)) grid-size)))

(defn assoc-bootstrap-widths [row bootstrap-widths]
  (assoc row :bootstrap-widths bootstrap-widths))

(defn final-rows->final-defs [final-rows fields-defs]
  (reduce (fn [{output :output next-rows :rest :as results} row]
            {:output (into output [(merge row {:defs (take (count (:widths row)) next-rows)})])
             :rest   (drop (count (:widths row)) next-rows)})
          {:output [] :rest fields-defs}
          final-rows))

(defn row-def-defs->fields [row-def]
  (let [names (mapv :name (:defs row-def))]
    (-> row-def (dissoc :defs) (assoc :fields names))))

(defn distribute-fields [fields-defs container-width]
  (let [distributed-rows      (distribute-widths (mapv :width fields-defs) (/ container-width field-width-multiplier))
        bootstrap-widths-rows (mapv (partial row-widths->grid-widths bootstrap-grid-cols) distributed-rows)
        final-rows            (mapv assoc-bootstrap-widths distributed-rows bootstrap-widths-rows)]
    (mapv row-def-defs->fields (:output (final-rows->final-defs final-rows fields-defs)))))

(def empty-by-type {:char    ""
                    :integer 0
                    :float   0.0})

(defn width->col-md-class [width]
  (str "col-md-" width))

(defn row-fields [row-def fields-defs]
  (let [field-by-name (fn [name] (first (filter #(= (:name %) name) fields-defs)))]
    (mapv field-by-name (:fields row-def))))

(defn str->date [s]
  (tf/parse date-formatter s))

(defn str->timestamp [s]
  (tf/parse date-time-formatter s))

(defn new-record [fields-defs]
  (reduce (fn [r field-def] (assoc r (-> field-def :name keyword) (or (:default field-def) "")))
          {}
          fields-defs))

(defn empty-record [fields-defs]
  (reduce (fn [r field-def] (assoc r (-> field-def :name keyword) ""))
          {}
          fields-defs))

(defn next-form-state [action current-state]
  (case [action current-state]
    [:append :view]  :edit
    [:edit :view]    :edit
    [:confirm :edit] :view
    [:discard :edit] :view
    [:delete :view]  :deleting
    current-state))

(defn get-form [db form-id]
  (get-in db [:complex-forms form-id]))

(defn current-form [db]
  (get-form db (:current-form db)))

(defn current-form-state [db]
  (:state (current-form db)))

(defn current-form-data [db]
  (:data (current-form db)))

(defn fields-defs [db]
  (-> (current-form db) :definition :fields-defs))

(def current-record-index #(-> % current-form-data :current-record))
(def current-records #(-> % current-form-data :records))
(def editing-data #(-> % current-form-data :editing-data))

(defn set-current-form-data [db new-form-data]
  (assoc-in db [:complex-forms (:current-form db) :data] (merge (current-form-data db) new-form-data)))

(defn set-current-record-index [db index]
  (assoc-in db [:complex-forms (:current-form db) :data :current-record] index))

(defn current-data-record [db]
  (some->>
   (current-record-index db)
   (get (current-records db))))

(defn current-record<-editing-data [db record-index]
  (assoc (current-records db) record-index (editing-data db)))

(defn new-record<-editing-data [db]
  (conj (current-records db) (editing-data db)))

(defn records<-editing-data [db]
  (if-let [record-index (current-record-index db)]
    (current-record<-editing-data db record-index)
    (new-record<-editing-data db)))

(defn delete-current-record [db]
  (into [] (cl/remove-nth (current-records db) (current-record-index db))))

(defn record-index-after-delete [db after-delete-records]
  (let [record-count  (count after-delete-records)
        current-index (current-record-index db)
        last-index    (when (> record-count 0) (dec record-count))]
    (if last-index
      (if (> current-index last-index)
        last-index
        current-index))))
