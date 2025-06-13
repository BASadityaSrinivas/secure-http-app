(defproject secure-http-app "0.1.0-SNAPSHOT"
  :description "The most secure HTTP Application"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :aliases {"magik" ["run"]}
  :dependencies [[org.clojure/clojure "1.12.1"]
                 [ring/ring-core "1.14.1"]
                 [ring/ring-jetty-adapter "1.14.1"]
                 [compojure "1.6.2"]
                 [nrepl "1.3.1"]
                 [org.postgresql/postgresql "42.7.7"]
                 [com.layerware/hugsql "0.5.3"]
                 [org.slf4j/slf4j-simple "2.0.17"]
                 [metosin/compojure-api "2.0.0-alpha33"]
                 [metosin/ring-http-response "0.9.5"]
                 [buddy/buddy-sign "3.6.1-359"]
                 [buddy/buddy-core "1.12.0-430"]
                 [com.fasterxml.jackson.core/jackson-core "2.19.0"]
                 [buddy/buddy-hashers "2.0.167"]]
  :main ^:skip-aot secure-http-app.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot      :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
