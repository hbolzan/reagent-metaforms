(ns metaforms.modules.samples.views
  (:require [metaforms.modules.complex-forms.views :as cf-views]
            [metaforms.modules.samples.db :as db]))

(defn sample-view []
  (cf-views/index db/form-definition))
