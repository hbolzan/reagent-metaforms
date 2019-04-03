(ns metaforms.components.modal
  (:require [re-frame.core :as rf]
            [metaforms.common.entities :as entities]
            [metaforms.common.dictionary :refer [l]]))

(defn modal-close []
  (rf/dispatch [:modal-close]))

(defn modal-confirm [confirm-fn]
  (fn [e]
    (when confirm-fn (confirm-fn))
    (modal-close)))

(defn modal-header [title on-close]
  [:div.modal-header
   [:h5.modal-title title]
   [:button.close {:type         "button"
                   :className    "close"
                   :data-dismiss "close"
                   :aria-label   (l :modal/close)
                   :onClick      on-close}
    [:span {:aria-hidden true} entities/times]]])

(defn modal-footer [on-confirm on-close confirm-label close-label dismiss-button? confirm-button?]
  [:div.modal-footer
   (when dismiss-button?
     [:button.btn.btn-secondary {:type         "button"
                                 :data-dismiss "modal"
                                 :onClick      on-close}
      close-label])
   (when confirm-button?
     [:button.btn.btn-primary {:type    "button"
                               :onClick (modal-confirm on-confirm)}
      confirm-label])])

(defn modal-overlay [visible?]
  [:div {:class (if visible? "show modal-backdrop fade" "modal-backdrop fade")
         :style (when-not visible? {:zIndex -9999})}])

(defn dialog []
  (let [{visible?             :visible?
         title                :title
         content              :content
         ok-button-label      :ok-button-label
         dismiss-button-label :dismiss-button-label
         on-confirm           :on-confirm
         dismiss-button?      :dismiss-button?
         confirm-button?      :confirm-button?
         :or                  {dismiss-button-label (l :modal/dismiss)
                               ok-button-label      (l :common/ok)
                               dismiss-button?      true
                               confirm-button?      true}} @(rf/subscribe [:modal-params])]
    [:<>
     [:div {
            :class    (if visible? "show modal" "modal fade")
            :tabIndex -1
            :role     "dialog"
            :style    (merge {:display      "block"
                              :paddingRight "14px"}
                             (when-not visible? {:zIndex -9999}))}
      [:div.modal-dialog {:role "document"}
       [:div.modal-content
        (modal-header title modal-close)
        [:div.modal-body [:p content]]
        (modal-footer on-confirm modal-close ok-button-label dismiss-button-label dismiss-button? confirm-button?)]]]
     (modal-overlay visible?)]))

(defn spinner []
  (let [{visible? :visible?} @(rf/subscribe [:spinner-params])]
    (when visible?
      [:<>
       [:div {:style {:position "fixed"
                      :left "0px"
                      :top "0px"
                      :width "100%"
                      :height "100%"
                      :zIndex 9999
                      :background "url('img/ajax-loader.gif') 50% 50% no-repeat"}}]
       #_(modal-overlay visible?)])))
