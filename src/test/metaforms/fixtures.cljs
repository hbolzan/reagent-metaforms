(ns test.metaforms.fixtures
  (:require [cljs-time.core :as tc]))

              ;; "C": "char",
              ;; "D": "date",
              ;; "E": "time",
              ;; "F": "float",
              ;; "I": "integer",
              ;; "M": "memo",
              ;; "T": "timestamp",

(def a-date (tc/local-date-time 2019 01 18))
(def a-timestamp (tc/local-date-time 2019 01 18 12 34 15))

(def fields-defs
  [{:data-type "integer"
    :default   19
    :name      "id"}

   {:data-type "char"
    :default   "Some name"
    :name      "name"}

   {:data-type "char"
    :default   nil
    :name      "address"}

   {:data-type "date"
    :default   "2019-01-18"
    :name      "initial_date"}

   {:data-type "timestamp"
    :default   "2019-01-18T12:34:15Z"
    :name      "date_and_time"}])
