(ns ventas.plugins.stripe.api
  (:require
   [ventas.server.api :as api]
   [ventas.server.api.admin :as api.admin]
   [ventas.database.entity :as entity]))

(api.admin/register-admin-endpoint!
  ::admin.config
  (fn [_ _]
    (entity/query-one :stripe-plugin)))

(api/register-endpoint!
 ::public-key.get
 (fn [_ _]
   (:stripe-plugin/public-key (entity/query-one :stripe-plugin))))

(defn set-config! [config]
  (when-let [existing-config (:db/id (entity/query-one :stripe-plugin))]
    (entity/delete existing-config))
  (entity/create* (merge config {:schema/type :schema.type/stripe-plugin})))

(api.admin/register-admin-endpoint!
  ::config.save
  (fn [{config :params} _]
    (set-config! config)
    nil))