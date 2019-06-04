(ns metaforms.modules.complex-forms.logic
  (:require [cljs-time.format :as tf]
            [clojure.string :as str]
            [metaforms.common.helpers :as helpers]
            [metaforms.common.logic :as cl]))


(def date-formatter (tf/formatter "yyyy-MM-dd"))
(def date-time-formatter (tf/formatter "yyyy-MM-dd'T'HH':'mm':'ssZ"))

(defn form-pk->form-id [form-pk]
  (-> (str form-pk) str/lower-case (str/replace #"_" "-") keyword))

(defn str->date [s]
  (tf/parse date-formatter s))

(defn str->timestamp [s]
  (tf/parse date-time-formatter s))

(defn new-record [fields-defs]
  (reduce (fn [r field-def] (assoc r (-> field-def :name keyword) (or (:default field-def) "")))
          {}
          fields-defs))

(defn empty-record [fields-defs]
  (reduce (fn [r field-def] (assoc r (-> field-def :name keyword) ""))
          {}
          fields-defs))

(defn empty-number? [value data-type]
  (and (some #{data-type} [:integer :float]) (js/isNaN value)))

(defn date-or-time? [data-type]
  (some #{data-type} [:date :time :timestamp]))

(defn typecast [value data-type]
  (let [result (cond
                 (date-or-time? data-type) (if (empty? value) nil value)
                 (= data-type :integer)    (js/parseInt value)
                 (= data-type :float)      (js/parseFloat value)
                 :else                     value)]
    (if (empty-number? result data-type)
      nil
      result)))

(defn field-typecast [data-record field-def]
  (let [field-name (-> field-def :name)]
    {field-name (typecast (get data-record (keyword field-name))
                          (-> field-def :data-type keyword))}))

(defn data-record->typed-data [data-record fields-defs]
  (reduce (fn [result field-def] (merge result (field-typecast data-record field-def)))
          {}
          fields-defs))

(defn load-form-definition [db form-id form-definition]
  (assoc-in db
            [:complex-forms form-id]
            {:definition form-definition
             :state      :view
             :data       {:records        []
                          :current-record nil
                          :editing-data   nil
                          :new-record?    false}}))

(defn load-form-definition-success [db form-id response]
  (load-form-definition db form-id (-> response :data first)))

(defn next-form-state [action current-state]
  (case [action current-state]
    [:append :view]    :edit
    [:edit :view]      :edit
    [:confirm :edit]   :view
    [:discard :edit]   :view
    [:delete :view]    :deleting
    [:nav-prior :view] :prior
    [:nav-next :view]  :next
    [:nav-first :view] :first
    [:nav-last :view]  :last
    [:search :view]    :search
    current-state))

(defn get-form [db form-id]
  (get-in db [:complex-forms form-id]))

(defn current-form [db]
  (get-form db (:current-form db)))

(defn current-form-some-prop [db prop] (-> db current-form prop))
(defn form-by-id-some-prop [db form-id prop] (-> db (get-form form-id) prop))

(defn current-form-state [db] (current-form-some-prop db :state))
(defn form-by-id-state [db form-id] (form-by-id-some-prop db form-id :state))

(defn current-form-data [db] (current-form-some-prop db :data))
(defn form-by-id-data [db form-id] (form-by-id-some-prop db form-id :data))

(defn current-form-field-value [db field-name]
  (-> db current-form-data :editing-data (get (keyword field-name))))

(defn form-by-id-field-value [db form-id field-name]
  (-> db (form-by-id-data form-id) :editing-data (get (keyword field-name))))

(defn current-form-definition [db] (current-form-some-prop db :definition))
(defn form-by-id-definition [db form-id] (form-by-id-some-prop db form-id :definition))

(defn current-form-dataset-name [db] (-> db current-form-definition :dataset-name))
(defn form-by-id-dataset-name [db form-id] (-> db (form-by-id-definition form-id) :dataset-name))

(defn replace-url-tag [url tag value]
  (str/replace url (str ":" tag) value))

(defn replace-complex-id [url dataset-name]
  (replace-url-tag url "complex-id" dataset-name))

(defn current-form-data-url [db url] (replace-complex-id url (current-form-dataset-name db)))
(defn form-by-id-data-url [db form-id url] (replace-complex-id url (form-by-id-dataset-name db form-id)))

(defn fields-defs [db]
  (-> db current-form-definition :fields-defs))

(defn form-by-id-fields-defs [db form-id]
  (-> db (form-by-id-definition form-id) :fields-defs))

(defn one-field-def [db field-name]
  (filter (fn [field-def] (= (:name field-def) field-name))
          (fields-defs db)))

(def current-record-index #(-> % current-form-data :current-record))
(def form-by-id-current-record-index #(-> %1 (form-by-id-data %2) :current-record))
(def current-records #(-> % current-form-data :records))
(def form-by-id-current-records #(-> %1 (form-by-id-data %2) :records))
(def current-form-editing-data #(-> % current-form-data :editing-data))
(def form-by-id-editing-data #(-> %1 (form-by-id-data %2) :editing-data))
(def new-record? #(-> % current-form-data :new-record?))
(def form-by-id-new-record? #(-> %1 (form-by-id-data %2) :new-record?))

(defn form-by-id-set-some-prop [db form-id prop value]
  (assoc-in db [:complex-forms form-id prop] value))

(defn form-by-id-set-data [db form-id data]
  (assoc-in db [:complex-forms form-id :data] (merge (form-by-id-data db form-id) data)))

(defn current-form-set-data [db new-form-data]
  (form-by-id-set-data db (:current-form db) new-form-data))

(defn set-current-form-editing-data [db fields]
  "merges fields into current editing data"
  (merge (current-form-editing-data db) fields))

(defn set-form-by-id-editing-data [db form-id fields]
  "merges fields into form editing data"
  (merge (form-by-id-editing-data db form-id) fields))

(defn form-by-id-set-record-index [db form-id index]
  (assoc-in db [:complex-forms form-id :data :current-record] index))

(defn set-current-record-index [db index]
  (form-by-id-set-record-index db (:current-form db) index))

(defn data-record-by-index [db index]
  (get (current-records db) index))

(defn form-by-id-data-record-by-index [db form-id index]
  (get (form-by-id-current-records db form-id) index))

(defn current-data-record [db]
  (data-record-by-index db (current-record-index db)))

(defn form-by-id-current-data-record [db form-id]
  (form-by-id-data-record-by-index db form-id (form-by-id-current-record-index db form-id)))

(defn record-pk-values [data-record form-definition]
  (map (fn [pk-field] (-> pk-field keyword data-record)) (:pk-fields form-definition)))

(defn current-record-pk-values [db]
  (record-pk-values (current-data-record db) (current-form-definition db)))

(defn form-by-id-current-record-pk-values [db form-id]
  (record-pk-values (form-by-id-current-data-record db form-id) (form-by-id-definition db form-id)))

(defn form-by-id-replace-url-with-pk
  [db form-id base-url pk-tag-name]
  (replace-url-tag (form-by-id-data-url db form-id base-url)
                   pk-tag-name
                   (-> db (form-by-id-current-record-pk-values form-id) first)))

(defn current-form-replace-url-with-pk
  [db base-url pk-tag-name]
  (form-by-id-replace-url-with-pk db (:current-form db) base-url pk-tag-name))

(defn form-by-id-current-record<-editing-data [db form-id record-index]
  (assoc (form-by-id-current-records db form-id) record-index (form-by-id-editing-data db form-id)))

(defn form-by-id-new-record<-editing-data [db form-id]
  (conj (form-by-id-current-records db form-id) (form-by-id-editing-data db form-id)))

(defn form-by-id-records<-editing-data [db form-id]
  (if (form-by-id-new-record? db form-id)
    (form-by-id-new-record<-editing-data db form-id)
    (form-by-id-current-record<-editing-data db form-id (form-by-id-current-record-index db form-id))))

(defn records<-editing-data [db]
  (form-by-id-records<-editing-data db (:current-form db)))

(defn form-by-id-current-record<-new-data [db form-id record-index data]
  (assoc (form-by-id-current-records db form-id) record-index data))

(defn current-record<-new-data [db record-index data]
  (assoc (current-records db) record-index data))

(defn form-by-id-new-record<-new-data [db form-id data]
  (conj (form-by-id-current-records db form-id) data))

(defn new-record<-new-data [db data]
  (conj (current-records db) data))

(defn form-by-id-records<-new-data [db form-id data]
  (if (form-by-id-new-record? db form-id)
    (form-by-id-new-record<-new-data db form-id data)
    (form-by-id-current-record<-new-data db form-id (form-by-id-current-record-index db form-id) data)))

(defn records<-new-data [db data]
  (form-by-id-records<-new-data db (:current-form db) data))

(defn form-by-id-delete-current-record [db form-id]
  (into [] (cl/remove-nth
            (form-by-id-current-records db form-id)
            (form-by-id-current-record-index db form-id))))

(defn delete-current-record [db]
  (form-by-id-delete-current-record db (:current-form db)))

(defn form-by-id-record-index-after-delete [db form-id after-delete-records]
  (let [record-count  (count after-delete-records)
        current-index (form-by-id-current-record-index db form-id)
        last-index    (when (> record-count 0) (dec record-count))]
    (if last-index
      (if (> current-index last-index)
        last-index
        current-index))))

(defn record-index-after-delete [db after-delete-records]
  (form-by-id-record-index-after-delete db (:current-form db) after-delete-records))

(defn fields-defs->data-grid-cols [fields-defs read-only?]
  (mapv (fn [field-def] {:key      (:name field-def)
                         :name     (:label field-def)
                         :editable (not read-only?)}) fields-defs))
