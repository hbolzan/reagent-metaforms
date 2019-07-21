(ns metaforms.common.translations.en)

(def translations
  {:common/ok      "OK"
   :common/yes     "Yes"
   :common/no      "No"
   :common/warning "WARNING"
   :common/error   "ERROR"
   :common/search  "Search"

   :modal/close   "Close"
   :modal/dismiss "Dismiss"
   :modal/confirm "Confirm"
   :modal/select  "Select"

   :dialog/confirmation "Confirmation"
   :dialog/verifying    "Verifying"

   :form/confirm-delete?         "Are you sure you want to delete the current record?"
   :form/confirm-edit?           "Confirm changes on current record?"
   :form/confirm-append?         "Confirm new record?"
   :form/search-failure          "An error ocurred while trying to open the search window"
   :form/confirm-failure         "An error ocurred while trying to append/update the current record"
   :form/delete-failure          "An error ocurred while trying to delete current record"
   :form/load-definition-failure "An error ocurred while trying do read the '{form-id}' complex table definition"
   :form/load-data-failure       "An error ocurred while trying to load '{form-id}' table data"

   :grid/save-pending "SAVE PENDING"
   :grid/save-failure "An error ocurred while trying to save '{form-id}' table data"

   :bundle/load-definition-failure "An error ocurred while trying to load '{bundle-id}' bundle definition"

   :error/unknown "Unknown error"
   })
