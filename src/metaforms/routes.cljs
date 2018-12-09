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

(defn app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/cadastros" []
    (rf/dispatch [:set-breadcrumbs (path->breadcrumbs "/cadastros")])
    (rf/dispatch [:set-view :cadastros]))

  (defroute "*" {:as params}
    (rf/dispatch [:set-breadcrumbs (path->breadcrumbs (:* params))])
    (rf/dispatch [:set-view :not-found]))

  (hook-browser-navigation!))
