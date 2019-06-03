(ns metaforms.modules.complex-forms.components.toolset
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(def action-buttons {:insert  {:icon           "plus-circle"
                               :enabled-states [:view :empty]
                               :form-event     :append}
                     :delete  {:icon           "trash-alt"
                               :enabled-states [:view]
                               :form-event     :delete}
                     :edit    {:icon           "edit"
                               :enabled-states [:view]
                               :form-event     :edit}
                     :confirm {:icon           "check-circle"
                               :enabled-states [:edit]
                               :form-event     :confirm}
                     :discard {:icon           "ban"
                               :enabled-states [:edit]
                               :form-event     :discard}
                     :search  {:icon           "search"
                               :enabled-states [:view :empty]
                               :form-event     :search}
                     :refresh {:icon           "redo"
                               :enabled-states [:view]
                               :form-event     :refresh}})

(def nav-buttons {:first {:icon           "fast-backward"
                          :enabled-states [:view :edit]
                          :form-event     :nav-first}
                  :prior {:icon           "step-backward"
                          :enabled-states [:view :edit]
                          :form-event     :nav-prior}
                  :next  {:icon           "step-forward"
                          :enabled-states [:view :edit]
                          :form-event     :nav-next}
                  :last  {:icon           "fast-forward"
                          :enabled-states [:view :edit]
                          :form-event     :nav-last}}
  )

(defn disabled? [form-state enabled-states]
  (-> (.indexOf enabled-states form-state) (< 0)))

(defn button-click [e]
  (let [events (if (= (type e) Keyword) [e] e)]
    (rf/dispatch-sync [:do-form-action (first events)])
    (doseq [event (rest events)]
      (rf/dispatch-sync [event]))))

(defn button-props [form-state enabled-states button-type button-types]
  (merge
   {:type      "button"
    :className "btn btn-primary btn-lg"
    :key       (name button-type)
    :onClick #(button-click (-> button-types button-type :form-event))}
   (cond (disabled? form-state enabled-states)
         {:disabled :disabled})))

(defn toolset-button
  [form-state {:keys [button-type button-types]}]
  (let [icon-class     (-> button-types button-type :icon)
        enabled-states (-> button-types button-type :enabled-states)]
    [:button (button-props form-state enabled-states button-type button-types)
                [:i {:className (str "fas fa-" icon-class)}]]))

(defn btn-group
  [form-state buttons]
  [:div
   {:className "btn-group mr-2" :role "group"}
   (map (fn [button-type] (toolset-button form-state
                                         {:button-type  button-type
                                          :button-types buttons}))
        (keys buttons))])

(defn form-data+current-state->form-state
  [form-data current-state]
  (if (and (= current-state :view) (-> form-data :current-record nil?))
    :empty
    current-state))

(defn toolset
  [form-id]
  (let [form-data     @(rf/subscribe [:form-by-id-data form-id])
        current-state @(rf/subscribe [:form-by-id-state form-id])
        form-state    (form-data+current-state->form-state form-data current-state)]
    [:div {:className "btn-toolbar" :role "toolbar"}
     (btn-group form-state action-buttons)
     (btn-group form-state nav-buttons)]))
