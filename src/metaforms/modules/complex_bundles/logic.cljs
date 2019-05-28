(ns metaforms.modules.complex-bundles.logic
  (:require [cljs-time.format :as tf]
            [clojure.string :as str]
            [metaforms.common.logic :as cl]))

(defn get-bundle [db bundle-id]
  (get-in db [:complex-bundles bundle-id]))

(defn dissoc-definitions [bundled-tables]
  (map #(dissoc (assoc % :definition-id (-> % :definition :id)) :definition) bundled-tables))

(defn load-bundle-definition-success [db bundle-id bundle]
  (assoc-in db
            [:complex-bundles bundle-id]
            (assoc bundle
                   :bundled-tables
                   (dissoc-definitions (:bundled-tables bundle)))))

(defn bundled-form-id [bundle-id table-definition]
  (keyword bundle-id (-> table-definition :definition :id)))

(defn bundle-forms-ids [bundle]
  (map #(keyword (:id bundle) (:definition-id %)) (:bundled-tables bundle)))

(defn children-bundled-tables-ids [bundle-id bundled-tables]
  (map (partial bundled-form-id bundle-id) (rest bundled-tables)))

(defn load-bundled-forms [db bundle-id bundled-tables load-definition-fn]
  (let [first-definition (-> bundled-tables first :definition first)]
    (reduce
     (fn [new-db bundled-table] (load-definition-fn new-db
                                                   (bundled-form-id bundle-id (:definition bundled-table))
                                                   (-> bundled-table :definition first)))
     (load-definition-fn db
                         (bundled-form-id bundle-id first-definition)
                         first-definition
                         (children-bundled-tables-ids bundle-id bundled-tables))
     (rest bundled-tables))))
