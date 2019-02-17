(ns metaforms.common.dictionary
  (:require [re-frame.core :as rf]
            [metaforms.common.translations.en :as en]
            [metaforms.common.translations.pt-br :as pt-br]))

(def translations
  {:en    en/translations
   :pt-br pt-br/translations})

(defn l [k]
  (let [language (or (:current-language @re-frame.db/app-db) :pt-br)]
    (or (-> translations language k) (name k))))

(comment
  (l :teste)
  (l :modal/close))
