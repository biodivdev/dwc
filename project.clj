(defproject dwc "0.0.1"
  :description "Simple (and rather limited) reader/consumer for DwC-A, DIGIR and TAPIR resources."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.clojure/data.xml "0.0.7"]
                 [org.clojure/data.csv "0.1.2"]
                 [org.clojure/data.json "0.2.2"]
                 [clj-http "0.7.2"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}})
