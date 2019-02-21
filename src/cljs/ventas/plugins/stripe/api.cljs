(ns ventas.plugins.stripe.api
  (:require [re-frame.core])
  (:require-macros
   [ventas.plugins.stripe.api]
   [ventas.server.api.core :refer [define-api-events-for-ns!]]))

(define-api-events-for-ns!)