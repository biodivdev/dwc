(ns dwc.json-test
  (:use midje.sweet)
  (:use [clojure.data.json :only [read-str write-str]])
  (:use dwc.json))

(def test-file "resources/dwc.json")

(fact "Can read json into hash-map"
  (read-json test-file) => [{:scientificName "Aphelandra longiflora" :latitude 10.10 :longitude 20.20 :locality "riverrun"}
                            {:scientificName "Vicia faba" :latitude 30.3 :longitude 8.9 :locality ""}])

(fact "Can write json"
  (let [occs (read-json test-file)
        json (write-json occs)]
    (read-str json) => (read-str (slurp test-file))))

