(ns metaforms.common.logic
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

(defn numeric? [s]
  (not (js/isNaN s)))

(defn sql-quoted [s]
  (str "'" s "'"))

(defn sql-quoted-if [s pred]
  (if (pred s)
    (sql-quoted s)
    s))

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

(defn action->dispatch-action
  "If action is not a vector, wraps it into a vector so it can be used as a dispatch action"
  [action]
  (if (= (type action) cljs.core/PersistentVector)
    action
    [(keyword action)]))

(defn action->action-fn
  "If action is not a function, returns a dispatcher function"
  [action]
  (cond
    (nil? action) nil
    (fn? action)  action
    :else #(rf/dispatch (action->dispatch-action action))))

(defn replace-tag [src tag value]
  (str/replace src (str "{" tag "}") value))

(defn set-spinner [db visible?]
  (assoc db :spinner {:visible? visible?}))

(defn clear-separators [s]
  (str/replace s #"[.\-/_]" ""))

(defn js-map->clj-map [m]
  (reduce-kv (fn [acc k v] (assoc acc (keyword k) v)) {} (js->clj m)))

(defn safe-trim [s]
  (if (nil? s) "" (str/trim s)))

(defn filter-index [pred coll]
  (keep-indexed #(when (pred %2) %1) coll))

(defn log [x]
  (js/console.log x)
  x)

(defn str-keys->keywords [m]
  (into {} (map (fn [e] [(keyword (first e)) (last e)]) m)))
