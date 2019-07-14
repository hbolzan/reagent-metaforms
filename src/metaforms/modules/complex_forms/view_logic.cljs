(ns metaforms.modules.complex-forms.view-logic
  (:require [cljs-time.format :as tf]
            [metaforms.common.logic :as cl]))

(def empty-row {:width 0 :widths []})
(def field-width-multiplier 7)
(def bootstrap-grid-cols 12)
(def bootstrap-md-width 720)

(defn set-last [coll x]
  (if (< (count coll) 1)
    [x]
    (conj (pop coll) x)))

(defn add-width-to-row [row width]
  (merge row {:width  (+ (:width row) width)
              :widths (conj (:widths row) width)}))

(defn add-to-row? [row {:keys [width line-break?] :as field-def} container-width]
  (boolean (and (not line-break?)
                (< (count (:widths row)) bootstrap-grid-cols)
                (<= (+ (:width row) width) container-width))))

(defn row-reducer [container-width rows {width :width :as field-def}]
  (let [current-row (last rows)]
    (if (add-to-row? current-row field-def container-width)
      (set-last rows (add-width-to-row current-row width))
      (conj rows {:width width :widths [width]}))))

(defn distribute-widths [fields-defs container-width]
  (reduce (partial row-reducer container-width) [empty-row] fields-defs))

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
  (let [distributed-rows      (distribute-widths fields-defs (/ container-width field-width-multiplier))
        bootstrap-widths-rows (mapv (partial row-widths->grid-widths bootstrap-grid-cols) distributed-rows)
        final-rows            (mapv assoc-bootstrap-widths distributed-rows bootstrap-widths-rows)]
    (mapv row-def-defs->fields (:output (final-rows->final-defs final-rows fields-defs)))))

(defn distribute-fields-by-page [fields-defs container-width]
  (let [fields-groups (group-by #(get-in % [:page :index]) fields-defs)]
    (reduce
     (fn [pages group-index]
       (assoc pages (or group-index 0) (distribute-fields (get fields-groups group-index) container-width)))
     {}
     (keys fields-groups))))

(def empty-by-type {:char    ""
                    :integer 0
                    :float   0.0})

(defn width->col-md-class [width]
  (str "col-md-" width))

(defn row-fields [row-def fields-defs]
  (let [field-by-name (fn [name] (first (filter #(= (:name %) name) fields-defs)))]
    (mapv field-by-name (:fields row-def))))

(comment
  (let [defs [{:name "A" :page {:index 0 :title "page-a"}}
              {:name "B" :page {:index 0 :title "page-a"}}
              {:name "C" :page {:index 0 :title "page-a"}}
              {:name "D" :page {:index 1 :title "page-b"}}
              {:name "E" :page {:index 1 :title "page-b"}}]
        groups (group-by #(get-in % [:page :index]) defs)]
    (mapv #(get groups %) (keys groups )))

  (let [defs [{:name "A" }
              {:name "B" }
              {:name "C" }
              {:name "D" }
              {:name "E" }]
        groups
        (group-by #(get-in % [:page :index]) defs)]
    (mapv #(get groups %) (keys groups ))
    ))
