(ns metaforms.common.logic)

(def sum (partial reduce +))

(defn index-by-fn [a fn]
  (first (apply fn second (map-indexed vector a))))

(def min-index #(index-by-fn % min-key))

(def max-index #(index-by-fn % max-key))

(defn dec-nth [v i]
  (assoc v i (dec (get v i))))

(defn inc-nth [v i]
  (assoc v i (inc (get v i))))
