(ns metaforms.modules.main.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [metaforms.common.views :as common.views]
            [metaforms.components.main :as main]
            [metaforms.components.modal :as modal]
            [metaforms.modules.cadastros.views :as cadastros]
            [metaforms.modules.complex-forms.views :as cf.views]
            [metaforms.modules.samples.views :as samples]))

(defmulti route (fn [view content] view))

(defmethod route :cadastros [_ _]
  (cadastros/index))

(defmethod route :sample [_ _]
  (samples/sample-view))

(defmethod route :complex-form [_ _]
  (cf.views/generic-view))

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
      (route view (common.views/not-found))]
     [main/main-footer nil]
     (modal/dialog)
     (modal/spinner)]))
