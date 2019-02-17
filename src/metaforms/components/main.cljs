(ns metaforms.components.main
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.components.sidebar :as sidebar]))

(defn header-logo [path]
  [:img.navbar-brand-full {:src   path
                           :width "90px"}])

(defn header-sider-toggler-button []
  [:button.navbar-toggler.sidebar-toggler.d-md-down-none
   {:type         "button"
    :data-togggle "side-bar-lg-show"
    :on-click     #(rf/dispatch [:toggle-sidebar])}
   [:span.navbar-toggler-icon]])

(defn main-header [content]
  [:header.app-header.navbar
   [:span.navbar-brand
    [:a {:href "/#"}
     (header-logo "img/logo_horizontal_320x132.png")]
    (header-sider-toggler-button)]])

(defn main-sidebar [content]
  [sidebar/main])

(defn breadcrumb-item [index item]
  (let [link    (:link item)
        active? (:active? item)]
    [:li.breadcrumb-item {:key   (str "breadcrumb-" index)
                          :class (when active? "active")}
     (if (and link (not active?)) [:a {:href link} (:label item)] (:label item))]))

(defn breadcrumbs [items]
  [:ol.breadcrumb
   (map-indexed breadcrumb-item items)])

(defn sidebar-items->content [items]
  (some->> items (mapv identity)))

(defn main-body [sidebar-items breadcrumb-items main-content]
  [:div.app-body
   (main-sidebar (sidebar-items->content sidebar-items))
   [:main.main
    (breadcrumbs breadcrumb-items)
    [:div.container-fluid
     [:div.ui-view
      [:div
       [:div.animated.fadeIn
        main-content]]]]]])

(defn main-footer [content]
  [:footer.app-footer content
   [:div
    [:a {:href "https://www.minipcp.com.br" :target "_blank"} "MiniPCP"]
    [:span " © 2019 EFX Tecnologia"]]
   [:div.ml-auto
    [:span "Powered by "]
    [:a {:href "https://github.com/hbolzan/reagent-metaforms" :target "_blank"} "Reagent Metaforms"]]])
