(ns migrations.20210216103041-question
  (:require [framework.db.sql :as sql]))

(defn up [config]
      (-> (sql/create-table :questions)
          (sql/with-columns [[:question_text (sql/call :varchar 200) (sql/call :primary-key)]
                             [:pub_date :date]])
          (sql/execute! config)))

(defn down [config]
      (-> (sql/drop-table :questions)
          (sql/execute! config)))