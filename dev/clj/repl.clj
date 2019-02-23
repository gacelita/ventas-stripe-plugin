(ns repl
  (:require
   [clojure.tools.namespace.repl :as tn]
   [ventas.system :as system]
   [mount.core :as mount]
   [shadow.cljs.devtools.server :as shadow.server]
   [shadow.cljs.devtools.api :as shadow.api]))

(defn refresh [& {:keys [after]}]
  (let [result (tn/refresh :after after)]
    (when (instance? Throwable result)
      (throw result))))

(defn refresh-all [& {:keys [after]}]
  (let [result (tn/refresh-all :after after)]
    (when (instance? Throwable result)
      (throw result))))

(alter-var-root #'*warn-on-reflection* (constantly true))
(tn/set-refresh-dirs "dev/clj" "src/clj" "src/cljc")

(defn start [& [states]]
  (-> (mount/only (or states system/default-states))
      mount/start)
  :done)

(defn r [& subsystems]
  (let [states (system/get-states subsystems)]
    (when (seq states)
      (mount/stop states))
    (refresh :after 'repl/start)))

(defn init-next []
  (start))

(defn init []
  (require 'ventas.core)
  (refresh-all :after 'repl/init-next))

(defn watch-cljs [build-id]
  (shadow.server/start!)
  (shadow.api/watch build-id))

(defn release-cljs [build-id]
  (shadow.server/start!)
  (shadow.api/release build-id))
