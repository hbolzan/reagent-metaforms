(ns metaforms.modules.complex-bundles.logic
  (:require [cljs-time.format :as tf]
            [clojure.string :as str]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.logic :as cf.logic]
            [metaforms.modules.complex-forms.constants :as cf.consts]))

(defn get-bundle [db bundle-id]
  (get-in db [:complex-bundles bundle-id]))

(defn dissoc-definitions [bundled-tables]
  (mapv #(dissoc (assoc % :definition-id (-> % :definition first :id)) :definition) bundled-tables))

(defn load-bundle-definition-success [db bundle-id bundle]
  (assoc-in db
            [:complex-bundles bundle-id]
            (assoc bundle
                   :bundled-tables
                   (dissoc-definitions (:bundled-tables bundle)))))

(defn load-bundled-forms [db bundle-id bundled-tables load-definition-fn]
  "add bundled forms to complex forms map - first form is the parent and the rest are it's children"
  (let [bundled-forms-ids (mapv #(keyword bundle-id (-> % :definition first :id)) bundled-tables)
        first-definition  (assoc (-> bundled-tables first :definition first) :children (rest bundled-forms-ids))
        tables            (into [] (cons (assoc (first bundled-tables) :definition [first-definition])
                                         (rest bundled-tables)))]
    (reduce-kv (fn [new-db index bundled-table]
                 (load-definition-fn new-db
                                     (get bundled-forms-ids index)
                                     (assoc (-> bundled-table :definition first)
                                            :bundle-id bundle-id
                                            :master-fields (:master-fields bundled-table)
                                            :bundle-actions (:bundle-actions bundled-table)
                                            :related-fields (:related-fields bundled-table))))
               db
               tables)))

(defn parent-data-changed? [old-data new-data master-fields]
  (reduce (fn [changed? field]
            (or changed? (not= (get old-data field) (get new-data field))))
          false
          (mapv keyword master-fields)))

(defn parent-related-where [parent-data master-fields related-fields]
  (str/join " and "
            (mapv (fn [master-field related-field]
                    (str related-field "=" (-> master-field
                                               keyword
                                               parent-data
                                               (cl/sql-quoted-if (comp not cl/numeric?)))))
                  master-fields
                  related-fields)))

(defn child-url [db complex-table-id parent-data bundled-table]
  (let [order-by-fields (->> complex-table-id (cf.logic/form-by-id-definition db) :order-by-fields)]
    (str (cf.logic/form-by-id-data-url db complex-table-id cf.consts/persistent-get-base-uri)
         "?where="
         (parent-related-where parent-data
                               (:master-fields bundled-table)
                               (:related-fields bundled-table))
         (when order-by-fields
           (str "&order=" (str/join "~" order-by-fields))))))

(defn empty-parent-data? [parent-data master-fields]
  (or (empty? parent-data)
      (reduce (fn [is-empty? field]
                (let [field-value (-> field keyword parent-data)]
                  (or is-empty? (str/blank? field-value) (= field-value 0))))
              false
              master-fields)))
