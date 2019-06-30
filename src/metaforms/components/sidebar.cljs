(ns metaforms.components.sidebar
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.modules.cadastros.db :as cadastros-db]))

(def has-children? not-empty)

(defn separator []
  [:div.nav-link {:style {:border-style "inset"
                          :border-width "1px"
                          :border-color "#4b5258"
                          :max-height   0
                          :padding      0}}])

(defn parent [caption]
  [:div.nav-link.text-uppercase.font-weight-bold.border-top caption])

(defn action-item [{:keys [caption icon route] enabled? :enabled :as item}]
  [:a.nav-link {:href route} [:i {:class (str "mr-3 fas fa-" (or icon "layer-group"))}] caption])

(defn menu-item
  [index {:keys [caption icon route children] enabled? :enabled separator? :separator :as item}]
  [:li.nav-item {:key (str "menu-item-" index)}
   (cond
     separator?               (separator)
     (has-children? children) (parent caption)
     :else                    (action-item item))])

(defn menu [items]
  (doall
   (map-indexed menu-item items)))

(defn main [{menu-items :menu-items}]
  [:div.sidebar
   [:nav.sidebar-nav.ps.ps--active-y
    [:ul.nav
     [:li.nav-item
      [:a.nav-link {:href "/#"}
       [:i.nav-icon.cui-home] " Início"]]
     (menu menu-items)]]])
