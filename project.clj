(defproject dwc-io "0.0.51"
  :description "Simple (and rather limited) reader/consumer for DwC-A, DIGIR, TAPIR, CSV and xlsx resources, local or online."
  :url "http://github.com/diogok/dwc-io"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.xml "0.0.7"]
                 [org.clojure/data.csv "0.1.2"]
                 [org.clojure/data.json "0.2.5"]
                 [dk.ative/docjure "1.6.0"]
                 [bigml/closchema "0.5"]
                 [clj-http "0.7.6"]]
  :repositories [["clojars" {:sign-releases false}]]
  :profiles {:uberjar {:aot :all}
             :jar {:aot :all}
             :dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]]}})
