(ns secure-http-app.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [clojure.pprint]
            [secure-http-app.api :as api]
            [nrepl.server :refer [start-server]]
            [compojure.api.sweet :refer :all]))

(defn -main
  [& args]
  (println "Server started in PORT: 3000")
  (start-server :port 7070)
  (jetty/run-jetty (api {:swagger
                         {:ui   "/api-docs"
                          :spec "/swagger.json"
                          :data {:info     {:title "Secure HTTP APP APIs"}
                                 :tags     [{:name "User APIs", :description "Log-in & Sign-up"}]
                                 :consumes ["application/json"]
                                 :produces ["application/json"]}}}
                        api/app)
                   {:port  8080
                    :join? true}))