(ns metaforms.common.helpers
  (:require [metaforms.common.consts :as consts]))

(defn is-key? [key-str key-key]
  (= key-str (get consts/keyboard key-key)))

(defn assoc-if [m k v]
  (-> m
      (cond-> v (assoc k v))))
