(ns metaforms.modules.complex-bundles.logic
  (:require [cljs-time.format :as tf]
            [clojure.string :as str]
            [metaforms.common.logic :as cl]))

(defn get-bundle [db bundle-id]
  (get-in db [:complex-bundles bundle-id]))
