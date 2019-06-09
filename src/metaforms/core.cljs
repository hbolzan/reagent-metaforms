(ns metaforms.core
  (:require [day8.re-frame.http-fx]
            [metaforms.components.main :as main]
            [metaforms.http.events]
            [metaforms.modules.complex-bundles.events]
            [metaforms.modules.complex-forms.events]
            [metaforms.modules.complex-forms.subs]
            [metaforms.modules.grid.events]
            [metaforms.modules.grid.subs]
            [metaforms.modules.main.events]
            [metaforms.modules.main.subs]
            [metaforms.modules.main.views :as main-views]
            [metaforms.routes :as routes]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(def breadcrumb-items [{:label "Início"}
                       {:label "Cadastro" :link "#"}
                       {:label "Clientes" :active? true}])

(def body-content [:div.col-md-12
                   "Olá Mundo!!!"])

(defn app []
  (main-views/main nil body-content))

(defn stop []
  (js/console.log "Stopping..."))

(defn start []
  (js/console.log "Starting...")
  (js/console.log (str "DEBUG: " goog.DEBUG))
  (rf/dispatch-sync [:initialize])
  (routes/app-routes)
  (r/render [app]
            (.getElementById js/document "metaforms")))

(defn ^:export init []
  (start))
