(ns metaforms.modules.complex-forms.components.dropdown
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :as rf]))

(defn filtered-options [options filter-by filter-value]
  (if filter-by
    (when filter-value
      (filter #(= ((keyword filter-by) %) filter-value) options))
    options))

(defn dropdown-options [options lookup-key lookup-result]
  (map
   (fn [option] [:option {:value (lookup-key option)
                         :key   (lookup-key option)} (lookup-result option)])
   options))

(defn update-filter-source [local-state filter-source-field]
  (assoc @local-state :filter-source-field filter-source-field))

(defn- view? [local-state]
  (= (:state @local-state) :view))

(defn filter-value [local-state filter-source-value]
  (let [local-state* @local-state]
    (if (= (:state local-state*) :view)
      filter-source-value
      (:filter-value local-state*))))

(defn dropdown
  [{:keys [field-id name label options filter-source-value] :as defs}
   common-props
   local-state]
  (let [lookup-key          (-> defs :lookup-key keyword)
        lookup-result       (-> defs :lookup-result keyword)
        lookup-filter       (-> defs :lookup-filter str/trim)
        filter-args         (if (not (empty? lookup-filter)) (str/split lookup-filter ";") [])
        filter-source-field (first filter-args)
        last-modified-field (:last-modified-field @local-state)
        value               (:value @local-state)]

    ;; if last-modified-field interests me, i'll keep it's value in my own state
    (if (and (not= (:value last-modified-field) (:filter-value @local-state))
             (= (:name last-modified-field) (first filter-args)))
      (reset! local-state (assoc @local-state :filter-value (:value last-modified-field))))

    [:select (merge {:class "form-control"
                     :id    field-id
                     :value value}
                    common-props)
     (dropdown-options (concat [{lookup-key "" lookup-result (str "-- " label " --")}]
                               (filtered-options options
                                                 (last filter-args)
                                                 (filter-value local-state filter-source-value)))
                       lookup-key
                       lookup-result)]))
