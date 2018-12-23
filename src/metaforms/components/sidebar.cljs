(ns metaforms.components.sidebar
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.modules.cadastros.db :as cadastros-db]))

(defn main []
  [:div.sidebar
   [:nav.sidebar-nav.ps.ps--active-y
    [:ul.nav
     [:li.nav-item
      [:a.nav-link {:href "/#"}
       [:i.nav-icon.cui-home] " Início"]]
     [:li.nav-item
      [:a.nav-link {:href "/#/cadastros"} [:i.nav-icon.cui-layers] " Cadastros"]]]]])
