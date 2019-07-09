(ns metaforms.components.sidebar
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.modules.cadastros.db :as cadastros-db]))

(defn separator []
  [:div.nav-link {:style {:border-style "inset"
                          :border-width "1px"
                          :border-color "#4b5258"
                          :max-height   0
                          :padding      0}}])

(defn menu-group [key caption children menu-item]
  (let [group-visible? @(rf/subscribe [:menu-group-visible? key])]
    [:<>
     [:div.nav-link.text-uppercase.font-weight-bold.border-top
      {:style {:cursor "pointer"} :onClick #(rf/dispatch [:set-menu-group-visible key])}
      caption]
     (when group-visible?
       [:ul.nav
        (doall (map-indexed menu-item children))])]))

(defn action-item [{:keys [caption icon route] enabled? :enabled :as item}]
  [:a.nav-link {:href route} [:i {:class (str "mr-3 fas fa-" (or icon "layer-group"))}] caption])

(defn menu-item
  [index {:keys [caption icon route children] enabled? :enabled separator? :separator :as item}]
  (let [key (str "menu-item-" index)]
    [:li.nav-item {:key key}
     (cond
       separator?           (separator)
       (not-empty children) (menu-group (keyword key) caption children menu-item)
       :else                (action-item item))]))

(defn menu [items]
  (doall
   (map-indexed menu-item items)))

(defn main [{menu-items :menu-items}]
  [:div.sidebar
   [:nav.sidebar-nav.ps.ps--active-y
    [:ul.nav
     [:li.nav-item
      [:a.nav-link {:href "/#"}
       [:i {:class (str "mr-3 fas fa-home")}] " Início"]]
     (menu menu-items)]]])
