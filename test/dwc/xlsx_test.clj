(ns dwc.xlsx-test
  (:use midje.sweet)
  (:use dwc.xlsx))

(def test-file "resources/dwc.xlsx")

(fact "Can read xlsx into hash-map"
  (read-xlsx test-file) => [{:scientificName "Aphelandra longiflora" :latitude "10.10" :longitude "20.20" :locality "riverrun" :taxonRank "species"}
                            {:scientificName "Vicia faba" :latitude "30.3" :longitude "8.9" :locality nil :taxonRank "species"}])

(fact "Can write to xlsx"
   (let [data [{:locality "riverrun" :decimalLatitude "30.10" :decimalLongitude "10.20" :scientificName "Aphelandra longiflora" :taxonRank "species"}
               {:locality nil :decimalLatitude "20.2" :decimalLongitude "9.8" :scientificName "Vicia faba" :taxonRank "species"}]]
  (read-xlsx (write-xlsx data)) => data))

