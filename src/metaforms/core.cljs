(ns metaforms.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [metaforms.http.events]
            [metaforms.routes :as routes]
            [metaforms.modules.main.events]
            [metaforms.modules.complex-forms.events]
            [metaforms.modules.complex-bundles.events]
            [metaforms.modules.main.subs]
            [metaforms.modules.complex-forms.subs]
            [metaforms.components.main :as main]
            [metaforms.modules.main.views :as main-views]))

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
