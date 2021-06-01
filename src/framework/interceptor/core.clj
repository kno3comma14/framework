(ns framework.interceptor.core
  (:require
   [clojure.walk :refer [keywordize-keys]]
   [xiana.core :as xiana]
   [honeysql.core :as sql]
   [ring.middleware.params :as middleware.params]
   [framework.acl.core :as acl]
   [framework.db.sql :as db.sql]
   [framework.session.core :as session]
   [framework.interceptor.wrap :as wrap]
   [framework.interceptor.muuntaja :as muuntaja])
  (:import
   (java.util UUID)))

(def log
  "Log interceptor.
  Enter: Print 'Enter:' followed by the complete state map.
  Leave: Print 'Leave:' followed by the complete state map."
  {:enter (fn [state] (println "Enter: " state) (xiana/ok state))
   :leave (fn [state] (println "Leave: " state) (xiana/ok state))})

(def side-effect
  "Side-effect interceptor.
  Enter: nil.
  Leave: Fetch and execute the state registered
  side-effect procedure, if none was found execute: `xiana/ok`."
  {:leave
   (fn [{side-effect :side-effect :as state}]
     (let [f (or side-effect xiana/ok)]
       (f state)))})

(def view
  "View interceptor.
  Enter: nil.
  Leave: Fetch and execute the state view registered
  procedure, if none was found execute: `xiana/ok`."
  {:leave
   (fn [{view :view :as state}]
     (let [f (or view xiana/ok)]
       (f state)))})

;; Question: should be used in the early chain of interceptors?
(def params
  "Update the request map with parsed url-encoded parameters.
  Adds the following keys to the request map:

  :query-params - a map of parameters from the query string
  :form-params  - a map of parameters from the body
  :params       - a merged map of all types of parameter

  Enter: TODO.
  Leave: nil."
  {:enter (fn [state]
            (let [f #(keywordize-keys
                      ((middleware.params/wrap-params identity) %))]
              (xiana/ok
               (update state :request f))))})

(def db-access
  "Database access interceptor.
  Enter: nil.
  Leave: Fetch and execute a given query using the chosen database
  driver, if succeeds associate its results into state response data.
  Remember the entry query must be a sql-map, e.g:
  {:select [:*] :from [:users]}."
  {:leave
   (fn [{query :query :as state}]
     (xiana/ok
      (if query
        (assoc-in state
                  [:response-data :db-data]
                  ;; returns the result of the database-query
                  ;; execution or empty ({})
                  (db.sql/execute query))
        state)))})

(defn message
  "This interceptor creates a function that prints predefined message.
  Enter: Print an arbitrary message.
  Leave: Print an arbitrary message."
  [msg]
  {:enter (fn [state] (println msg) (xiana/ok state))
   :leave (fn [state] (println msg) (xiana/ok state))})

(defn session-user-id
  "This interceptor handles the session user id management.
  Enter: Get the session id from the request header, if
  that operation doesn't succeeds a new session is created an associated to the
  current state, otherwise the cached session data is used.
  Leave: Verify if the state has a session id, if so add it to
  the session instance and remove the new session property of the current state.
  The final step is the association of the session id to the response header."
  ([] (session-user-id (session/init-in-memory)))
  ([session-instance]
   {:enter
    (fn [{request :request :as state}]

      (let [session-id (try (UUID/fromString
                             (get-in request [:headers :session-id]))
                            (catch Exception _ nil))
            session-data (when session-id
                           (session/fetch session-instance
                                          session-id))]
        (xiana/ok
         (if session-data
           ;; associate session data into state
           (assoc state :session-data session-data)
           ;; else, associate a new session
           (-> (assoc-in state [:session-data :session-id] (UUID/randomUUID))
               (assoc-in [:session-data :new-session] true))))))
    :leave
    (fn [state]
      (let [session-id (get-in state [:session-data :session-id])]
        ;; add the session id to the session instance and
        ;; dissociate the new-session from the current state
        (session/add! session-instance
                      session-id
                      (dissoc (:session-data state) :new-session))
        ;; associate the session id
        (xiana/ok
         (assoc-in state
                   [:response :headers :session-id]
                   (str session-id)))))}))

(defn -user-role
  "Update the user role."
  [state role]
  (assoc-in state [:session-data :user] {:role role}))

(defn session-user-role
  "This interceptor updates session data user role:authorization
  from the given request header.
  Enter: Fetch the authorization from its request/state
  if succeeds update the current state with that information,
  also update the user role with a custom value or the default :guest.
  Leave: nil."
  ([]
   (session-user-role -user-role :guest))
  ([f role]
   {:enter
    (fn [{request :request :as state}]
      (let [auth (get-in request [:headers :authorization])]
        (xiana/ok
         (->
          ;; f: function to update/associate the user role
          (f state role)
          ;; associate authorization into session-data
          (assoc-in [:session-data :authorization] auth)))))}))

(defn muuntaja
  "Muuntaja encoder/decoder interceptor."
  ([] (muuntaja muuntaja/interceptor))
  ([interceptor] (wrap/interceptor interceptor)))