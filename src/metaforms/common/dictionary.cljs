(ns metaforms.common.dictionary
  (:require [metaforms.common.logic :as cl]
            [re-frame.core :as rf]
            [metaforms.common.translations.en :as en]
            [metaforms.common.translations.pt-br :as pt-br]))

(def translations
  {:en    en/translations
   :pt-br pt-br/translations})

(defn current-language []
  (or (:current-language @re-frame.db/app-db) :pt-br))

(defn current-translations [language]
  (-> translations language))

(defn replace-message-tags [message tags-data]
  (reduce-kv (fn [m tag value] (cl/replace-tag m (name tag) value)) message tags-data))

(defn l
  ([k]
   (l k nil))
  ([k tags-data]
   (let [language (or (:current-language @re-frame.db/app-db) :pt-br)]
     (or (-> (current-language) current-translations k (replace-message-tags tags-data)) (name k)))))

(defn error-result->error-message [result default-message]
  (or (get (-> result :response :data :messages) (current-language))
      default-message))
