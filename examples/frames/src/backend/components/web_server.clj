(ns web-server
  (:require
    [com.stuartsierra.component :as component]
    [ring.adapter.jetty :as jetty]))

(defrecord WebServer
  [http-server app-component config]
  component/Lifecycle
  (start
    [this]
    (assoc this :http-server
           (jetty/run-jetty app-component config)))
  (stop
    [this]
    (.stop http-server)
    this))

(defn web-server
  "Returns a new instance of the web server component which
      creates its handler dynamically."
  [config]
  (component/using (map->WebServer {:config config})
                   [:app-component]))
