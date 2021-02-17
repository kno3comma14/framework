(ns status-test
  (:require [app]
            [router]
            [framework.config.core :as config]
            [com.stuartsierra.component :as component]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [clojure.test :refer :all]))

(deftest status-test
  (let [config  (config/edn)
        app-cfg (:framework.app/ring config)
        handler (->
                  (component/system-map
                    :config config
                    :router (router/make-router)
                    :app (app/make-app app-cfg))
                  (component/system-using
                    {:router []
                     :app    [:router]})
                  (component/start-system)
                  (get-in [:app :handler]))]
    (-> (session handler)
        (visit "/re-frame")
        (has (status? 200))
        (has (some-regex? "^.*JavaScript.*$")))))
