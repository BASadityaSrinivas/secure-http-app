(ns secure-http-app.db.users
  (:require [hugsql.core :as hugsql]
            [buddy.hashers :as hashers]))

(hugsql/def-db-fns "secure_http_app/db/sql/users.sql")

(def db
  {:classname   "org.postgresql.Driver"
   :subprotocol "postgresql"
   :subname     "http_app"
   :user        "postgres"
   :password    "postgres"})

(defn insert-into-users
  [username password]
  (let [insert-operation (insert-user db {:username username
                                          :password (hashers/encrypt password)})]
    (if (not (zero? insert-operation))
      {:success true
       :msg     "Successfully added the user"}
      {:success false
       :msg     "User already exist"})))

(defn validate-pwd
  [username password]
  (if-let [fetch-user (get-user-by-username db {:username username})]
    (try
      (if (hashers/check password (:password fetch-user))
        {:success true
         :msg     "Success! User password matched"}
        {:success false
         :status  403
         :msg     "You're a FAILURE, can't even remember a password"})
      (catch Exception e
        {:success false
         :status  403
         :msg     "Hashing issue"}))
    {:success false
     :status  404
     :msg     "User NOT FOUND"}))

;(comment
;  (get-user-by-username db {:username "bas"})
;  (insert-user db {:username "ram"
;                   :password "NoNoNo"})
;  (insert-into-users "bassssss" "whoisthis")
;  (validate-pwd "bas" "itsSecret"))


