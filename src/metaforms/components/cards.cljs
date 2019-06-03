(ns metaforms.components.cards
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn card
  ([title toolbar content]
   (card title toolbar content "col-lg-12"))
  ([title toolbar content col-class]
   [:div.row
    [:div {:class col-class}
     [:div.card
      [:div.card-header.bg-primary {:style {:padding-top "2px" :padding-bottom "2px"}}
       [:strong title]]
      (when toolbar
        [:div.card-header
         toolbar])
      [:div.card-body
       content]]]]))
