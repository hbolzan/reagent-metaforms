(ns metaforms.http.events
  (:require [ajax.core :as ajax]
            [re-frame.core :as rf]))

(def default-timeout 8000)

(defn basic-params [method uri on-success on-failure]
  {:method          method
   :uri             uri
   :format          (ajax/json-request-format)
   :response-format (ajax/json-response-format {:keywords? true})
   :timeout         default-timeout
   :on-success      on-success
   :on-failure      on-failure})

(defn with-payload [method uri payload on-success on-failure]
  (assoc (basic-params method uri on-success on-failure) :params payload))

(rf/reg-event-fx
 :http-get
 (fn [{db :db} [_ uri on-success on-failure]]
   {:http-xhrio (basic-params :get uri on-success on-failure)}))

(rf/reg-event-fx
 :http-post
 (fn [{db :db} [_ uri payload on-success on-failure]]
   {:http-xhrio (with-payload :post uri payload on-success on-failure)}))

(rf/reg-event-fx
 :http-put
 (fn [{db :db} [_ uri payload on-success on-failure]]
   {:http-xhrio (with-payload :put uri payload on-success on-failure)}))

(rf/reg-event-fx
 :http-delete
 (fn [{db :db} [_ uri on-success on-failure]]
   {:http-xhrio (basic-params :delete uri on-success on-failure)}))
