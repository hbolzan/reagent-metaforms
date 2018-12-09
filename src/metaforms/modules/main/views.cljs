(ns metaforms.modules.main.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.components.main :as main]))

(defn main [sidebar-items body-content]
  (let [sidebar-visible? @(rf/subscribe [:sidebar-visible?])
        breadcrumb-items @(rf/subscribe [:breadcrumb-items])]
    [:section.app.header-fixed.sidebar-fixed.aside-menu-fixed.pace-done
     (when sidebar-visible? {:class "sidebar-lg-show"})
     [main/main-header nil]
     [main/main-body
      sidebar-items
      breadcrumb-items
      body-content]
     [main/main-footer nil]]))
