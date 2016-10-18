(defproject dwc-io "0.0.59"
  :description "Simple (and rather limited) reader/consumer for DwC-A, DIGIR, TAPIR, CSV and xlsx resources, local or online."
  :url "http://github.com/diogok/dwc-io"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.csv "0.1.3"]
                 [org.clojure/data.json "0.2.6"]
                 [dk.ative/docjure "1.9.0"]
                 [bigml/closchema "0.6.1"]
                 [clj-http "2.2.0"]]
  :repositories [["clojars" {:sign-releases false}]]
  :global-vars {*warn-on-reflection* true}
  :profiles {:uberjar {:aot :all}
             :jar {:aot :all}
             :dev {
                   :dependencies [[midje "1.8.2"]]
                   :plugins [[lein-midje "3.2"]]}})
