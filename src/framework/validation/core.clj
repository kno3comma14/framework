(ns framework.validation.core
  (:require [malli.core :as m]
            [malli.error :as me]))

(defn- clean-request-map
  "Removes nil elements from the map to be evaluated"
  [request-map]
  (let [filtered-values (filter (fn [[k, v]] (not (nil? v))) request-map)]
    (reduce (fn [acc, x] (assoc acc (get x 0) (get x 1))) {} filtered-values)))

(defn validate
  "Validates a request-map with a given malli schema"
  [schema request-map]
  (let [cleaned-request-map (clean-request-map request-map)]
    (m/validate schema cleaned-request-map)))

(defn explain
  "Explains the reason of a failure in the validation process"
  [schema request-map & humanized]
  (let [cleaned-request-map (clean-request-map request-map)
        result-explanation (-> schema (m/explain cleaned-request-map))]
    (if humanized
      (me/humanize result-explanation)
      result-explanation)))
