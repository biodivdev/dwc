(ns dwc.xlsx-test
  (:use midje.sweet)
  (:use dwc.xlsx))

(def test-file "resources/dwc.xlsx")

(fact "Can read xlsx into hash-map"
  (read-xlsx test-file) => [{:scientificName "Aphelandra longiflora" :latitude "10.10" :longitude "20.20" :locality "riverrun" :taxonRank "species"}
                            {:scientificName "Vicia faba" :latitude "30.3" :longitude "8.9" :locality nil :taxonRank "species"}])


