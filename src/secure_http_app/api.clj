(ns secure-http-app.api
  (:require [clojure.java.io :as io]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [ring.util.response :as response]
            [secure-http-app.db.users :as users]
            [secure-http-app.middleware :as middle]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn get-file
  [filename]
  (let [file (io/file (str "resources/" filename))]
    (-> (response/response (slurp file))
        (response/header "Content-Disposition"
                         (str "attachment; filename=" filename)))))

(def app
  (api
    (GET "/hello" []
      :tags ["Hellooooooooo!"]
      :description "Just to Greet the user!"
      :query-params [name :- String]
      (ok {:message (str "Hello, " name)}))

    (POST "/signup" []
      :tags ["User APIs"]
      :description "To register the user"
      :body-params [username :- String
                    password :- String]
      (let [create-user (users/insert-into-users username password)
            now (c/to-long (t/now))]
        (if (:success create-user)
          (created nil {:msg   (str "Hello " username ", You are added to the clan!")
                        :x-jwt (middle/generate-jwt {:username  username
                                                     :timestamp now})})
          (ok {:msg "You're already in the clan bruh! Go, log-in"}))))

    (POST "/login" []
      :tags ["User APIs"]
      :description "To log-in the user"
      :body-params [username :- String
                    password :- String]
      (let [validate-pwd (users/validate-pwd username password)
            now (c/to-long (t/now))]
        (if (:success validate-pwd)
          (ok {:msg   (str "Hello " username ", Welcome to safest place")
               :x-jwt (middle/generate-jwt {:username  username
                                            :timestamp now})})
          (if (= 403 (:status validate-pwd))
            (unauthorized {:msg "You're a FAILURE, can't even remember a password"})
            (unauthorized {:msg "Wait a minute, WHO ARE YOU? Could you please go and sign up first?"})))))

    (context "/file" []
      :tags ["File"]

      (GET "/" []
        :description "To see all available files"
        :header-params [x-jwt :- String
                        user-cookie :- String]
        :middleware [middle/wrap-jwt-validation]
        (ok (str "Here are the available files: \n" (seq (.list (clojure.java.io/file "./resources"))))))

      (GET "/:filename" []
        :description "To download the file"
        :path-params [filename :- String]
        :header-params [x-jwt :- String
                        user-cookie :- String
                        hmac :- String]
        :middleware [middle/wrap-jwt-validation
                     middle/wrap-hmac-validation]
        (try
          (get-file filename)
          (catch Exception e
            (bad-request {:msg "File does not exist"}))))

      (GET "/signed/:filename" []
        :description "Download the file using expiry link"
        :path-params [filename :- String]
        :query-params [expiry :- Long
                       hmac :- String]
        (let [payload (str filename ":" expiry)
              now (c/to-long (t/now)) ]
          (if (> now expiry)
            (forbidden {:msg "Link expired"})
            (if (:success (middle/validate-hmac payload hmac))
              (try
                (get-file filename)
                (catch Exception e
                  (bad-request {:msg "File does not exist"})))
              (forbidden {:msg "Invalid signed link"})))))

      (GET "/signed-url/:filename" []
        :description "Get a expiry URL for file download - 10 seconds"
        :path-params [filename :- String]
        :header-params [x-jwt :- String
                        user-cookie :- String
                        hmac :- String]
        :middleware [middle/wrap-jwt-validation
                     middle/wrap-hmac-validation]
        (let [expiry (+ (c/to-long (t/now)) (* 10 1000))
              payload (str filename ":" expiry)
              hmac (middle/generate-hmac payload)
              url (str "/file/signed/"
                       filename
                       "?expiry=" expiry
                       "&hmac=" hmac)]
          (ok {:signed-url url
               :expires-in "10 seconds"}))))

    (POST "/sign-payload" []
      :description "To generate HMAC for a filename"
      :tags ["Auth APIs"]
      :body-params [filename :- String]
      (ok {:filename filename
           :hmac     (middle/generate-hmac filename)}))

    (POST "/verify-signed" []
      :description "To validate hmac of a file name"
      :tags ["Auth APIs"]
      :body-params [filename :- String]
      :header-params [hmac :- String]
      (if (:success (middle/validate-hmac filename hmac))
        (ok {:msg "Valid HMAC"})
        (forbidden {:msg "Invalid HMAC. Please generate again"})))))