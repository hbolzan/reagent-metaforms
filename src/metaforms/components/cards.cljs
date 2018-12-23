(ns metaforms.components.cards
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn card
  ([title content]
   (card title content "col-lg-12"))
  ([title content col-class]
   [:div.row
    [:div {:class col-class}
     [:div.card
      [:div.card-header
       [:i.fa.fa-align-justify]
       title]
      [:div.card-body
       content]]]]))
