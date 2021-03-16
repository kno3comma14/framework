(ns framework.acl.core-functions-test
  (:require
    [clojure.test :refer :all]
    [framework.acl.core-functions :refer [has-access]]))

(def custom-roles
  {:customer         [{:resource    "items"
                       :actions     [:read]
                       :restriction :all}
                      {:resource    "users"
                       :actions     [:read :update :delete]
                       :restriction :own}
                      {:resource    "addresses"
                       :actions     [:create :read :update :delete]
                       :restriction :own}
                      {:resource    "carts"
                       :actions     [:create :read :update :delete]
                       :restriction :own}]
   :warehouse-worker [{:resource    "items"
                       :actions     [:read :update]
                       :restriction :all}]
   :postal-worker    [{:resource    "carts"
                       :actions     [:read :update]
                       :restriction :all}
                      {:resource    "addresses"
                       :actions     [:read]
                       :restriction :all}]
   :shop-worker      [{:resource    "items"
                       :actions     [:all]
                       :restriction :all}]
   :administrator    [{:resource    :all
                       :actions     [:all]
                       :restriction :all}]})

(deftest custom-roles-test
  (is (= :all (has-access custom-roles {:role :customer :resource "items" :privilege :read})))
  (is (false? (has-access custom-roles {:role :customer :resource "items" :privilege :delete})))
  (is (= :own (has-access custom-roles {:role :customer :resource "users" :privilege :read})))
  (is (= :own (has-access custom-roles {:role :customer :resource "users" :privilege :delete})))
  (is (= :all (has-access custom-roles {:role :administrator :resource "users" :privilege :read}))))

(def default-roles
  {:guest     [{:resource    "items"
                :actions     [:read]
                :restriction :all}]
   :member    [{:resource    "items"
                :actions     [:read]
                :restriction :all}
               {:resource    "users"
                :actions     [:read :update :delete]
                :restriction :own}
               {:resource    "addresses"
                :actions     [:create :read :update :delete]
                :restriction :own}
               {:resource    "carts"
                :actions     [:create :read :update :delete]
                :restriction :own}]
   :staff     [{:resource    "items"
                :actions     [:read :update]
                :restriction :all}]
   :superuser [{:resource    :all
                :actions     [:all]
                :restriction :all}]})

(def guest {})
(def member {:is_active true})
(def staff {:is_active true :is_staff true})
(def admin {:is_active true :is_superuser true})
(def suspended-admin {:is_active false :is_superuser true})

(deftest default-role-tests
  (is (= :all (has-access default-roles guest {:resource "items" :privilege :read})))
  (is (false? (has-access default-roles guest {:resource "users" :privilege :read})))
  (is (= :own (has-access default-roles member {:resource "users" :privilege :read})))
  (is (= :own (has-access default-roles member {:resource "addresses" :privilege :read})))
  (is (false? (has-access default-roles staff {:resource "users" :privilege :read})))
  (is (= :all (has-access default-roles staff {:resource "items" :privilege :update})))
  (is (= :all (has-access default-roles admin {:resource "items" :privilege :create})))
  (is (= :all (has-access default-roles admin {:resource "items" :privilege :create})))
  (is (= :all (has-access default-roles suspended-admin {:resource "items" :privilege :read})))
  (is (false? (has-access default-roles suspended-admin {:resource "items" :privilege :create})))
  (is (false? (has-access default-roles suspended-admin {:resource "users" :privilege :delete}))))

(def complex-roles
  {:guest     [{:resource    "posts"
                :actions     [:read]
                :restriction :all}]
   :member    [{:resource    "posts"
                :actions     [:read]
                :restriction :all}
               {:resource    "posts"
                :actions     [:create :update :delete]
                :restriction :own}
               {:resource    "comments"
                :actions     [:create :update :delete]
                :restriction :own}
               {:resource    "comments"
                :actions     [:read]
                :restriction :all}
               {:resource    "users"
                :actions     [:create :update :delete]
                :restriction :own}
               {:resource    "users"
                :actions     [:read]
                :restriction :all}]
   :staff     [{:resource    "posts"
                :actions     [:read :update :delete]
                :restriction :all}
               {:resource    "comments"
                :actions     [:read :update :delete]
                :restriction :all}
               {:resource    "users"
                :actions     [:read]
                :restriction :all}]
   :superuser [{:resource    :all
                :actions     [:all]
                :restriction :all}]})

(deftest complex-roles-test
  (is (= :all (has-access complex-roles {:role :guest :resource "posts" :privilege :read})))
  (is (false? (has-access complex-roles {:role :guest :resource "posts" :privilege :create})))
  (is (= :all (has-access complex-roles {:role :member :resource "posts" :privilege :read})))
  (is (= :own (has-access complex-roles {:role :member :resource "posts" :privilege :create})))
  (is (= :all (has-access complex-roles {:role :member :resource "comments" :privilege :read})))
  (is (= :own (has-access complex-roles {:role :member :resource "comments" :privilege :update}))))

