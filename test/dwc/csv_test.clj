(ns dwc.csv-test
  (:use midje.sweet)
  (:use dwc.csv))

(def test-file "resources/dwc.csv")

(fact "Can read csv into hash-map"
  (read-csv test-file) => [{:scientificName "Aphelandra longiflora" :latitude "10.10" :longitude "20.20" :locality "riverrun"}
                           {:scientificName "Vicia faba" :latitude "30.3" :longitude "8.9" :locality ""}])

(fact "Can write csv"
  (write-csv [{:scientificName "Foo" :locality "Riverrun" } {:scientificName "Bar" :id 1}])
   => "\"id\";\"locality\";\"scientificName\"\n\"\";\"Riverrun\";\"Foo\"\n\"1\";\"\";\"Bar\"")
