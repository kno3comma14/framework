(ns framework.route.core-test
  (:require
   [xiana.core :as xiana]
   [clojure.test :refer :all]
   [framework.state.core :as state]
   [framework.route.core :as route]
   [framework.route.helpers :as helpers]))

(def sample-request
  {:uri "/" :request-method :get})

(def sample-not-found-request
  {:uri "/not-found" :request-method :get})

(def sample-routes
  "Sample routes structure."
  [["/" {:action :action}]])

(def sample-routes-with-handler
  "Sample routes structure."
  [["/" {:handler :handler}]])

(def sample-routes-without-action
  "Sample routes structure (without action or handler)."
  [["/" {}]])

;; test reset routes functionality
(deftest contains-sample-routes
  ;; set sample routes
  (route/reset sample-routes)
  ;; test if sample routes was registered correctly
  (is (= sample-routes @route/-routes)))

;; test route match update request-data (state) functionality
(deftest contains-updated-request-data
  ;; set sample routes
  (route/reset sample-routes)
  ;; get state from sample request micro/match flow
  (let [state (-> (state/make sample-request)
                  (route/match)
                  (xiana/extract))
        ;; expected request data
        expected  {:method :get :action :action}]
    ;; verify if updated request-data
    ;; is equal to the expected value
    (is (= (:request-data state) expected))))

;; test if the updated request-data (state) data handles the
(deftest contains-not-found-action
  ;; set sample routes
  (route/reset sample-routes)
  ;; get action from sample request micro/match flow
  (let [action (-> (state/make sample-not-found-request)
                   (route/match)
                   (xiana/extract)
                   (:request-data)
                   (:action))
        ;; expected action
        expected helpers/not-found]
    ;; verify if action has the expected value
    (is (= action expected))))

;; test if the updated request-data contains the right action
(deftest route-contains-default-action
  ;; (re)set routes
  (route/reset sample-routes-with-handler)
  ;; get action from the updated state/match (micro) flow computation
  (let [action (-> (state/make sample-request)
                   (route/match)
                   (xiana/extract)
                   (:request-data)
                   (:action))
        ;; expected action
        expected helpers/action]
    ;; verify if action has the expected value
    (is (= action expected))))

;; test if the route/match flow handles a route without a handler or action
(deftest handles-route-without-action-or-handler
  ;; (re)set routes
  (route/reset sample-routes-without-action)
  ;; get action from the updated state/match (micro) flow computation
  (let [action (-> (state/make sample-request)
                   (route/match)
                   (xiana/extract)
                   (:request-data)
                   (:action))
        ;; expected action? TODO: research
        expected helpers/not-found]
    ;; verify if action has the expected value
    (is (= action expected))))