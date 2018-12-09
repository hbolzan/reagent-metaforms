(ns metaforms.routes
  (:require [goog.events :as events]
            [goog.history.EventType :as EventType]
            [re-frame.core :as rf]
            [secretary.core :as secretary])
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.history.Html5History))

(defn hook-browser-navigation! []
  (doto (Html5History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/teste/:path" [path]
    ;; :set-breadcrumbs
    (rf/dispatch [:set-breadcrumbs [{:label "Início"}
                                    {:label "Teste" :link "/#/teste"}
                                    {:label path}]])
    (js/console.log "teste: " path))

  (defroute "*" {:as params}
    (js/console.log "route: " (:* params)))
  (hook-browser-navigation!))
