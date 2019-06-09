(ns metaforms.common.helpers
  (:require [metaforms.common.consts :as consts]
            [re-frame.core :as rf]))

(defn is-key? [key-str key-key]
  (= key-str (get consts/keyboard key-key)))

(defn assoc-if [m k v]
  (-> m
      (cond-> v (assoc k v))))

(defn dispatch-n [effects]
  (doseq [fx effects] (rf/dispatch fx)))
