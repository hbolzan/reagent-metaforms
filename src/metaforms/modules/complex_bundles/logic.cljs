(ns metaforms.modules.complex-bundles.logic
  (:require [cljs-time.format :as tf]
            [clojure.string :as str]
            [metaforms.common.dictionary :refer [l]]
            [metaforms.common.logic :as cl]
            [metaforms.modules.complex-forms.constants :as cf.consts]
            [metaforms.modules.complex-forms.logic :as cf.logic]))

(defn get-bundle [db bundle-id]
  (get-in db [:complex-bundles bundle-id]))

(def bundle-data (comp :bundle-data get-bundle))

(defn set-bundle-data [db bundle-id data]
  (assoc-in db
            [:complex-bundles bundle-id :bundle-data]
            (merge (bundle-data db bundle-id) data)))

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

(defn parse-element [el]
  (if (string? el) (cljs.reader/read-string el) el))

(def not-vector? #(not (vector? %)))
(def restv (comp (partial into []) rest))

(defn parse-branch [data]
  (if (empty? data)
    data
    (let [f           (first data)
          first-data  (if (vector? f) (parse-branch f) f)
          middle-data (into [] (take-while not-vector? (restv data)))
          rest-data   (into [] (drop-while not-vector? (restv data)))]
      (into [(parse-element first-data)]
            (concat middle-data (parse-branch rest-data))))))

(defn parse-view-data [view-data]
  (parse-branch view-data))


(defn dynamic-view-modal-action [content]
  {:dispatch [:show-modal-window
              (l :common/results)
              (parse-view-data content)
              nil]})

;; what to expect from dynamic view
;; type: "modal" (for now)
;; content: the view content
;; actions: array of maps with buttons and corresponding actions
(defn dynamic-view-actions [db bundle-id response]
  (let [{:keys [type content]}
        (-> db :complex-bundles bundle-id :bundle-data :dynamic-view)]
    (case type
      "modal" (dynamic-view-modal-action content)
      nil)))
