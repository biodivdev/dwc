(ns dwc.csv-test
  (:use midje.sweet)
  (:use dwc.csv))

(def test-file "resources/dwc.csv")

(fact "Can read csv into hash-map"
  (read-csv test-file) => [{:scientificName "Aphelandra longiflora" :latitude "10.10" :longitude "20.20"}
                           {:scientificName "Vicia faba" :latitude "30.3" :longitude "8.9"}])

