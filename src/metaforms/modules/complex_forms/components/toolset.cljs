(ns metaforms.modules.complex-forms.components.toolset
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(def action-buttons {:insert {:icon           "plus-circle"
                              :enabled-states [:empty :view]
                              :form-event     :append}
                     :delete   {:icon           "trash-alt"
                                :enabled-states [:view]
                                :form-event     :delete}
                     :edit     {:icon           "edit"
                                :enabled-states [:view]
                                :form-event     :edit}
                     :confirm  {:icon           "check-circle"
                                :enabled-states [:edit]
                                :form-event     :confirm}
                     :discard  {:icon           "ban"
                                :enabled-states [:edit]
                                :form-event     :discard}
                     :search   {:icon           "search"
                                :enabled-states [:empty :view]
                                :form-event     :search}
                     :refresh  {:icon           "redo"
                                :enabled-states [:view]
                                :form-event     :refresh}})

(def nav-buttons {:first {:icon           "fast-backward"
                          :enabled-states [:empty :view :edit]
                          :form-event     :nav-first}
                  :prior {:icon           "step-backward"
                          :enabled-states [:empty :view :edit]
                          :form-event     :nav-prior}
                  :next  {:icon           "step-forward"
                          :enabled-states [:empty :view :edit]
                          :form-event     :nav-next}
                  :last  {:icon           "fast-forward"
                          :enabled-states [:empty :view :edit]
                          :form-event     :nav-last}}
  )

(defn disabled? [form-state enabled-states]
  (-> (.indexOf enabled-states form-state) (< 0)))

(defn on-click-event
  [events button-type button-types]
  (if-let [event ((-> button-types button-type :form-event) events)]
    {:onClick event}))

(defn button-props [form-state events enabled-states button-type button-types]
  (merge
   {:type      "button"
    :className "btn btn-primary btn-lg"
    :key       (name button-type)}
   (on-click-event events button-type button-types)
   (cond (disabled? form-state enabled-states)
         {:disabled :disabled})))

(defn toolset-button
  [form-state {:keys [events button-type button-types]}]
  (let [icon-class     (-> button-types button-type :icon)
        enabled-states (-> button-types button-type :enabled-states)]
    [:button (button-props form-state events enabled-states button-type button-types)
                [:i {:className (str "fas fa-" icon-class)}]]))

(defn btn-group
  [form-state events buttons]
  [:div
   {:className "btn-group mr-2" :role "group"}
   (map (fn [button-type] (toolset-button form-state
                                         {:events       events
                                          :button-type  button-type
                                          :button-types buttons}))
        (keys buttons))])

(defn toolset
  [events]
  (let [form-state @(rf/subscribe [:current-form-state])]
    [:div {:className "btn-toolbar" :role "toolbar"}
     (btn-group form-state events action-buttons)
     (btn-group form-state events nav-buttons)]))
