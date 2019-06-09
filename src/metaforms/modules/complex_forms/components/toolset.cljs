(ns metaforms.modules.complex-forms.components.toolset
  (:require [metaforms.common.dictionary :refer [l]]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(def action-buttons {:insert  {:icon           "plus-circle"
                               :enabled-states [:view :empty :pending]
                               :form-event     :append}
                     :delete  {:icon           "trash-alt"
                               :enabled-states [:view :pending]
                               :form-event     :delete}
                     :edit    {:icon           "edit"
                               :enabled-states [:view :pending]
                               :form-event     :edit}
                     :confirm {:icon           "check-circle"
                               :enabled-states [:edit]
                               :form-event     :confirm}
                     :discard {:icon           "ban"
                               :enabled-states [:edit]
                               :form-event     :discard}
                     :search  {:icon           "search"
                               :enabled-states [:view :empty :pending]
                               :form-event     :search}
                     :refresh {:icon           "redo"
                               :enabled-states [:view :pending]
                               :form-event     :refresh}})

(def nav-buttons {:first {:icon           "fast-backward"
                          :enabled-states [:view :pending]
                          :form-event     :nav-first}
                  :prior {:icon           "step-backward"
                          :enabled-states [:view :pending]
                          :form-event     :nav-prior}
                  :next  {:icon           "step-forward"
                          :enabled-states [:view :pending]
                          :form-event     :nav-next}
                  :last  {:icon           "fast-forward"
                          :enabled-states [:view :pending]
                          :form-event     :nav-last}})

(def extra-buttons {:save {:icon           "save"
                           :label          (l :grid/save-pending)
                           :class          "btn-danger"
                           :enabled-states [:pending]
                           :form-event     :save}})

(defn disabled? [form-state enabled-states]
  (-> (.indexOf enabled-states form-state) (< 0)))

(defn button-click [form-id e]
  (let [events (if (= (type e) Keyword) [e] e)]
    (rf/dispatch-sync [:do-form-action (first events) form-id])
    (doseq [event (rest events)]
      (rf/dispatch-sync [event form-id]))))

(defn button-props [form-id form-state button-type buttons on-click]
  (merge
   {:type      "button"
    :className (str "btn btn-lg "
                    (if-let [custom-class (-> buttons button-type :class)] custom-class "btn-primary"))
    :key       (name button-type)
    :onClick #(on-click form-id (-> buttons button-type :form-event))}
   (cond (disabled? form-state (-> buttons button-type :enabled-states))
         {:disabled :disabled})))

(defn toolset-button
  [{:keys [form-id form-state button-type buttons on-click]}]
  (let [icon-class     (-> buttons button-type :icon)
        enabled-states (-> buttons button-type :enabled-states)]
    [:button (button-props form-id form-state button-type buttons on-click)
     [:i {:className (str "fas fa-" icon-class)}]
     (when-let [label (-> buttons button-type :label)] (str " " label))]))

(defn btn-group
  [key form-id form-state buttons on-click]
  [:div {:key key :className "btn-group mr-2" :role "group"}
   (map (fn [button-type] (toolset-button {:form-id     form-id
                                           :form-state  form-state
                                           :button-type button-type
                                           :buttons     buttons
                                           :on-click    on-click}))
        (keys buttons))])

(defn form-data+current-state->form-state
  [form-data current-state]
  (if (and (= current-state :view) (-> form-data :current-record nil?))
    :empty
    current-state))

(defn toolbar [{form-id        :form-id
                form-state     :form-state
                buttons-groups :buttons-groups
                on-click       :on-click}]
  [:div {:className "btn-toolbar" :role "toolbar"}
   (doall (map-indexed (fn [idx group]
                         (btn-group (str "btn-grp-" idx) form-id form-state group on-click))
                       buttons-groups))])

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
               :buttons-groups [action-btns nav-btns]
               :on-click       btn-click}))))
