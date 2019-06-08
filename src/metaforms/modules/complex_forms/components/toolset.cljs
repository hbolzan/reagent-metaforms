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
                          :enabled-states [:view]
                          :form-event     :nav-first}
                  :prior {:icon           "step-backward"
                          :enabled-states [:view]
                          :form-event     :nav-prior}
                  :next  {:icon           "step-forward"
                          :enabled-states [:view]
                          :form-event     :nav-next}
                  :last  {:icon           "fast-forward"
                          :enabled-states [:view]
                          :form-event     :nav-last}}
  )

(defn disabled? [form-state enabled-states]
  (-> (.indexOf enabled-states form-state) (< 0)))

(defn button-click [form-id e]
  (let [events (if (= (type e) Keyword) [e] e)]
    (rf/dispatch-sync [:do-form-action (first events) form-id])
    (doseq [event (rest events)]
      (rf/dispatch-sync [event form-id]))))

(defn button-props [form-id form-state enabled-states button-type button-types on-click]
  (merge
   {:type      "button"
    :className "btn btn-primary btn-lg"
    :key       (name button-type)
    :onClick #(on-click form-id (-> button-types button-type :form-event))}
   (cond (disabled? form-state enabled-states)
         {:disabled :disabled})))

(defn toolset-button
  [{:keys [form-id form-state button-type button-types on-click]}]
  (let [icon-class     (-> button-types button-type :icon)
        enabled-states (-> button-types button-type :enabled-states)]
    [:button (button-props form-id form-state enabled-states button-type button-types on-click)
                [:i {:className (str "fas fa-" icon-class)}]]))

(defn btn-group
  [form-id form-state buttons on-click]
  [:div
   {:className "btn-group mr-2" :role "group"}
   (map (fn [button-type] (toolset-button {:form-id      form-id
                                           :form-state   form-state
                                           :button-type  button-type
                                           :button-types buttons
                                           :on-click     on-click}))
        (keys buttons))])

(defn form-data+current-state->form-state
  [form-data current-state]
  (if (and (= current-state :view) (-> form-data :current-record nil?))
    :empty
    current-state))

(defn toolbar [{form-id        :form-id
                form-state     :form-state
                action-buttons :action-buttons
                nav-buttons    :nav-buttons
                on-click       :on-click}]
  [:div {:className "btn-toolbar" :role "toolbar"}
   (btn-group form-id form-state action-buttons on-click)
   (btn-group form-id form-state nav-buttons on-click)])

(defn toolset
  ([form-id]
   (toolset form-id action-buttons nav-buttons))
  ([form-id action-btns nav-btns]
   (toolset form-id action-btns nav-btns button-click))
  ([form-id action-btns nav-btns btn-click]
   (let [form-data     @(rf/subscribe [:form-by-id-data form-id])
         current-state @(rf/subscribe [:form-by-id-state form-id])
         form-state    (form-data+current-state->form-state form-data current-state)]
     (toolbar {:form-id        form-id
               :form-state     form-state
               :action-buttons action-btns
               :nav-buttons    nav-btns
               :on-click       btn-click}))))
