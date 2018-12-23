(ns metaforms.modules.main.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.components.main :as main]
            [metaforms.modules.cadastros.views :as cadastros]))

(defmulti route (fn [view content] view))

(defmethod route :cadastros [_ _]
  (cadastros/index))

(defn not-found []
  [:div.container
   [:div.row.justify-content-center
    [:div.col-md-6
     [:div.clearfix
      [:h1.float-left.display-3.mr-4 "404"]
      [:h4.pt-3 "Oops! Função não disponível."]
      [:p.text-muted "Escolha uma das opções disponíveis no menu."]]]]])

(defmethod route :default [_ content]
  content)

(defn main [sidebar-items body-content]
  (let [sidebar-visible? @(rf/subscribe [:sidebar-visible?])
        breadcrumb-items @(rf/subscribe [:breadcrumb-items])
        view             @(rf/subscribe [:current-view])]
    [:section.app.header-fixed.sidebar-fixed.aside-menu-fixed.pace-done
     (when sidebar-visible? {:class "sidebar-lg-show"})
     [main/main-header nil]
     [main/main-body
      sidebar-items
      breadcrumb-items
      (route view (not-found))]
     [main/main-footer nil]]))
