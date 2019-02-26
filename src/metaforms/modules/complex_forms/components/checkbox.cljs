(ns metaforms.modules.complex-forms.components.checkbox
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(def YES "S")
(def NO "N")

(defn toggle-yes-no [local-state]
  (if (= (:value local-state) YES) NO YES))

(defn yes-no-attrs
  [{:keys [field-id name label] :as defs} common-props local-state*]
  (merge {:id       field-id
          :type     "checkbox"
          :value    (:value @local-state*)
          :checked  (= (:value @local-state*) YES)
          :onChange (fn [e]
                      (let [value (toggle-yes-no @local-state*)]
                        (reset! local-state* (assoc @local-state* :value value))
                        (rf/dispatch [:field-value-changed name value])
                        (rf/dispatch [:input-blur name value])))}
         common-props))

(defn yes-no
  [{:keys [field-id name label] :as defs} common-props local-state*]
  [:div.form-check
   [:input.form-check-input (yes-no-attrs defs common-props local-state*)]
   [:label.form-check-label {:for field-id}
    label]])
