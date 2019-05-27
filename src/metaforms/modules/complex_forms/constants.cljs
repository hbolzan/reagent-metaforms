(ns metaforms.modules.complex-forms.constants)

;; release build must be served through the same domain and port
(def api-host (if goog.DEBUG  "http://localhost:8000/api/" "/api/"))

(def persistent-path "query/persistent/")
(def base-uri (str api-host persistent-path "complex-tables/?id={id}&middleware=complex_forms&depth=1"))
(def persistent-get-base-uri (str api-host persistent-path ":complex-id/"))
(def persistent-post-base-uri (str api-host persistent-path ":complex-id/"))
(def persistent-put-base-uri (str persistent-post-base-uri ":id/"))
(def persistent-delete-base-uri (str api-host persistent-path "delete/:complex-id/:id/"))

(def complex-bundles-base-uri (str api-host persistent-path "complex-bundles/?id=:bundle-id&depth=1"))

(def validations-path "service/get/{service}/{method}/")
(def validation-base-url (str api-host validations-path))