(ns framework.webserver.core
  (:require
   [xiana.core :as xiana]
   [ring.adapter.jetty :as jetty]
   [framework.route.core :as route]
   [framework.state.core :as state]
   [framework.config.core :as config]
   [framework.interceptor.queue :as interceptor.queue])
  (:import
   (org.eclipse.jetty.server Server)))

;; web server reference
(defonce -webserver (atom {}))

(defn handler-fn
  "Return jetty server handler function."
  [interceptors]
  (fn [http-request]
    (let [state (state/make http-request)
          queue (list #(route/match %)
                      #(interceptor.queue/execute % interceptors))]
      (-> (xiana/apply-flow-> state queue)
          ;; extract
          (xiana/extract)
          ;; get the response
          (get :response)))))

(defn- make
  "Web server instance."
  [options interceptors]
  {:options options
   :server  (jetty/run-jetty (handler-fn interceptors) options)})

(defn stop
  "Stop web server."
  []
  ;; stop the server if necessary
  (when (not (empty? @-webserver))
    (.stop (get @-webserver :server))))

(defn start
  "Start web server."
  [interceptors]
  ;; stop the server
  (stop)
  ;; get server options
  (when-let [options (config/get-spec :webserver)]
    ;; tries to initialize the web-server if we have the
    ;; server specification (its options)
    (swap! -webserver
           (fn [m]
             (merge m (make options interceptors))))))