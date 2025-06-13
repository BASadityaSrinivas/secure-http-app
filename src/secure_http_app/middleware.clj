(ns secure-http-app.middleware
  (:require [buddy.sign.jwt :as jwt]
            [buddy.core.mac :as mac]
            [ring.util.http-response :refer :all]
            [buddy.core.codecs :as codecs]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clj-time.format :as f]))

(def ^:const JWT_SECRET "jwt-secret")
(def ^:const HMAC_SECRET "hmac-secret")

(defn generate-jwt
  [msg]
  (jwt/sign msg JWT_SECRET {:alg :hs256
                            :typ :JWT}))

(defn verify-jwt
  [user token]
  (try
    (let [{:keys [username timestamp] :as msg} (jwt/unsign token JWT_SECRET)
          old-time-ms (c/from-long timestamp)
          now (t/now)]
      (if (= user username)
        (if (< (t/in-minutes (t/interval old-time-ms now)) 5)
          {:success true
           :status  200
           :msg     "JWT is valid"}
          {:success false
           :status  403
           :msg     "JWT is invalid. Please log-in again"})
        {:success false
         :status  404
         :msg     "Invalid user. Please use your own JWT again"}))
    (catch Exception e
      {:success false})))

(defn generate-hmac
  [msg]
  (-> (mac/hash msg {:key HMAC_SECRET
                     :alg :hmac+sha256})
      (codecs/bytes->hex)))

(defn validate-hmac
  [msg token]
  (try
    {:success (mac/verify msg (codecs/hex->bytes token) {:key HMAC_SECRET
                                                         :alg :hmac+sha256})}
    (catch Exception e
      {:success false})))

(defn wrap-hmac-validation
  [handler]
  (fn
    ([{:keys [headers params] :as request}]
     (let [filename (:filename params)
           hmac (get headers "hmac")
           hmac-validation (validate-hmac filename hmac)]
       (if (:success hmac-validation)
         (handler request)
         (forbidden {:msg "Invalid HMAC. Please generate again"}))))))

(defn wrap-jwt-validation
  [handler]
  (fn
    ([{:keys [headers] :as request}]
     (let [x-jwt (get headers "x-jwt")
           user (get headers "user-cookie")
           jwt-validation (verify-jwt user x-jwt)]
       (if (:success jwt-validation)
         (handler request)
         (if (= 403 (:status jwt-validation))
           (forbidden {:msg "Invalid session. Please log-in again"})
           (forbidden {:msg "Use your own JWT please. No forgery"})))))))

;(c/to-long (t/now))                                         ; To epoch milliseconds
;(c/from-long 1749792315213)                                 ; epoch to date-time object
;(f/unparse (f/formatters :date-time) (c/from-long 1749792315213)) ; epoch to string
;(t/in-minutes (t/interval (c/from-long 1749792315213) (t/now))) ; difference b/w time in minutes
