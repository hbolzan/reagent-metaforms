(ns metaforms.routes
  (:require [goog.events :as events]
            [goog.history.EventType :as EventType]
            [re-frame.core :as rf]
            [secretary.core :as secretary]

            ;; provisório - somente para testes
            [metaforms.modules.samples.db :as samples.db])
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.history.Html5History))

(defn hook-browser-navigation! []
  (doto (Html5History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn assoc-if [cond m k v]
  (if cond (assoc m k v) m))

(defn crumbs->routes [crumbs]
  (reduce (fn [routes crumb] (conj routes (str (last routes) "/" crumb))) [] crumbs))

(defn path->breadcrumbs [route]
  (let [crumbs      (rest (clojure.string/split route #"/"))
        all-routes  (crumbs->routes crumbs)
        last-crumb  (-> crumbs count dec)
        last-crumb? #(= % last-crumb)]
    (into [{:label "Início" :link "/#/"}]
          (map-indexed (fn [index path-part]
                         (assoc-if (last-crumb? index)
                                   {:label (clojure.string/capitalize path-part)
                                    :link  (str "/#" (nth all-routes index))}
                                   :active? true))
                       crumbs))))

;; routing methods in metaforms.modules.main.views
(defn handle-route [path view-id]
  (rf/dispatch [:set-breadcrumbs (path->breadcrumbs path)])
  (rf/dispatch [:set-view view-id]))

;; primeiro teste - pega form-definition de samples
(defn handle-complex-form-route [form-id]
  (let [form-definition samples.db/form-definition]
    (rf/dispatch [:set-breadcrumbs (path->breadcrumbs (str "/forms/" (:title form-definition)))])
    (rf/dispatch [:set-form-definition :sample])))

(defn handle-remote-complex-form-route [complex-form-id]
  (rf/dispatch [:set-breadcrumbs (path->breadcrumbs (str "/forms/" complex-form-id))])
  (rf/dispatch [:set-form-definition complex-form-id]))

(defn app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/cadastros" []
    (handle-route "/cadastros" :cadastros))

  (defroute "/sample" []
    (handle-route "/sample" :sample))

  (defroute "/forms/sample" []
    (handle-complex-form-route :sample))

  (defroute "/forms/complex" []
    (handle-remote-complex-form-route "CAD_CLIENTES"))

  (defroute "*" {:as params}
    (handle-route (:* params) :not-found))

  (hook-browser-navigation!))
