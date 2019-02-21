(ns ventas.plugins.stripe.core
  "Adds a payment method for Stripe"
  (:require
   [clj-stripe.charges :as stripe.charges]
   [clj-stripe.common :as stripe]
   [ventas.payment-method :as payment-method]
   [ventas.plugins.stripe.api]
   [ventas.entities.order :as entities.order]
   [ventas.html :as html]
   [ventas.database.entity :as entity]))

(defn- pay! [order {:keys [token]}]
  (let [{:amount/keys [currency value]} (entities.order/get-amount! order)
        {:currency/keys [keyword] :as amount} (entity/find currency)
        {:stripe-plugin/keys [private-key]} (entity/query-one :stripe-plugin)
        {:keys [id error]} (stripe/with-token
                            private-key
                            (stripe/execute
                             (stripe.charges/create-charge
                              (stripe/money-quantity (* 100 value) (name keyword))
                              (stripe/card token))))]
    (if error
      (throw (Exception. (:message error)))
      (do
        (entity/update* (assoc order :order/payment-reference id
                                     :order/payment-amount amount
                                     :order/status :order.status/paid))
        true))))

(entity/register-type!
 :stripe-plugin
 {:migrations
  [[:base [{:db/ident :stripe-plugin/private-key
            :db/valueType :db.type/string
            :db/cardinality :db.cardinality/one}
           {:db/ident :stripe-plugin/public-key
            :db/valueType :db.type/string
            :db/cardinality :db.cardinality/one}]]]})

(payment-method/register!
 :stripe
 {:name "Stripe"
  :pay-fn #'pay!})

(defn middleware [handler]
  (fn [req]
    (handler (html/enqueue-css req ::stripe "/files/css/ventas/plugins/stripe/core.css"))))