(ns metaforms.modules.complex-forms.components.dropdown
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [metaforms.common.logic :as cl]
            [re-frame.core :as rf]))

(defn filtered-options [options filter-by filter-value]
  (if filter-by
    (when filter-value
      (filter #(= ((keyword filter-by) %) filter-value) options))
    options))

(defn render-dropdown-options [options lookup-key lookup-result]
  (map
   (fn [option] [:option {:value (lookup-key option)
                         :key   (or (lookup-key option) 0)} (lookup-result option)])
   options))

(defn review-lookup-result-name [lookup-key-name lookup-result-name]
  (if (= lookup-key-name lookup-result-name)
    (str lookup-result-name "-dscr")
    lookup-result-name))

(defn dropdown-options [options label lookup-key-name lookup-result-name]
  (let [lookup-key    (keyword lookup-key-name)
        lookup-result (keyword (review-lookup-result-name lookup-key-name lookup-result-name))]
    (render-dropdown-options (concat [{lookup-key nil lookup-result (str "-- " label " --")}]
                                     (if (= lookup-key-name lookup-result-name)
                                       (mapv #(assoc % lookup-result (lookup-key %)) options)
                                       options))
                             lookup-key
                             lookup-result)))

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
  [{:keys [field-id name label lookup-key lookup-result options filter-source-value] :as defs}
   common-props
   local-state]
  (let [lookup-filter       (-> defs :lookup-filter cl/safe-trim)
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
     (dropdown-options (filtered-options options
                                         (last filter-args)
                                         (filter-value local-state filter-source-value))
                       label
                       lookup-key
                       lookup-result)]))
