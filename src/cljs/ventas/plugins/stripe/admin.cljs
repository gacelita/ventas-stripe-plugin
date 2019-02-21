(ns ventas.plugins.stripe.admin
  (:require
   [re-frame.core :as rf]
   [ventas.components.base :as base]
   [ventas.components.form :as form]
   [ventas.components.notificator :as notificator]
   [ventas.server.api :as backend]
   [ventas.i18n :refer [i18n]]
   [ventas.themes.admin.skeleton :as admin.skeleton]
   [ventas.routes :as routes]
   [ventas.utils.ui :as utils.ui]
   [ventas.plugins.stripe.api :as api]
   [ventas.i18n :as i18n])
  (:require-macros
   [ventas.utils :refer [ns-kw]]))

(def state-key ::state)

(i18n/register-translations!
 {:en_US
  {::page "Stripe"
   ::public-key "Public key"
   ::private-key "Private key"
   ::submit "Submit"}})

(admin.skeleton/add-menu-item!
 {:route :admin.payment-methods.stripe
  :label ::page
  :parent :admin.payment-methods})

(rf/reg-event-fx
 ::submit
 (fn [{:keys [db]} _]
   {:dispatch [::api/config.save
               {:params (get-in db [state-key :form])
                :success [::notificator/notify-saved]}]}))

(defn- field [{:keys [key] :as args}]
  [form/field (merge args
                     {:db-path [state-key]
                      :label (i18n (ns-kw key))})])

(defn- content []
  [form/form [state-key]
   [base/segment {:color "orange"
                  :title (i18n ::page)}
    [base/form {:on-submit (utils.ui/with-handler #(rf/dispatch [::submit]))}

     [field {:key :stripe-plugin/public-key}]
     [field {:key :stripe-plugin/private-key}]

     [base/divider {:hidden true}]

     [base/form-button
      {:type "submit"}
      (i18n ::submit)]]]])

(defn page []
  [admin.skeleton/skeleton
   [:div.admin__default-content.admin-payment-methods-stripe__page
    [content]]])

(rf/reg-event-fx
 ::init
 (fn [_ _]
   {:dispatch [::api/admin.config
               {:success [::form/populate [state-key]]}]}))

(routes/define-route!
 :admin.payment-methods.stripe
 {:name ::page
  :url "stripe"
  :component page
  :init-fx [::init]})
