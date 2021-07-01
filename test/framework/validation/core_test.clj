(ns framework.validation.core-test
  (:require [framework.validation.core :as v]
            [clojure.test :refer :all]))

(def RequestValidationSchema
  [:and
   [:map
    [:x int?]
    [:y int?]]])

(deftest validate-test
  (testing "Valid schema"
    (is
     (let [valid-input-map {:x 19 :y 0}
           expected-value true
           actual-value (v/validate RequestValidationSchema valid-input-map)]
       (= expected-value actual-value))))
  (testing "Not valid schema"
    (is
     (let [not-valid-input-map {:x 0}
           expected-value false
           actual-value (v/validate RequestValidationSchema not-valid-input-map)]
       (= expected-value actual-value)))))

(deftest explain-test
  (testing "Explanation from not valid schema"
    (is
     (let [not-valid-input-map {:x 0}
           expected-type clojure.lang.PersistentArrayMap
           actual-type (type (v/explain RequestValidationSchema not-valid-input-map))]
       (prn actual-type)
       (isa? actual-type expected-type))))
  (testing "Humanized explanation input"
    (is
     (let [not-valid-input-map {:x 1}
           expected-value {:y ["missing required key"]}
           humanized true
           actual-value (v/explain RequestValidationSchema not-valid-input-map humanized)]
       (= expected-value actual-value)))))
    

