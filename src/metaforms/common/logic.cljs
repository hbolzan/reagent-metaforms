(ns metaforms.common.logic)

(def sum (partial reduce +))

(defn index-by-fn [a fn]
  (first (apply fn second (map-indexed vector a))))

(def min-index #(index-by-fn % min-key))

(def max-index #(index-by-fn % max-key))

(defn dec-nth [v n]
  (assoc v n (dec (get v n))))

(defn inc-nth [v i]
  (assoc v i (inc (get v i))))

(defn merge-in [m path n]
  (assoc-in m path (merge (get-in m path) n)))

(defn remove-nth
  "Removes nth item from vector"
  [v n]
  (concat (subvec v 0 n) (subvec v (inc n))))

(defn log [x]
  (js/console.log x)
  x)
