(ns dwc-io.csv-test
  (:use midje.sweet)
  (:use dwc-io.csv))

(def test-file "resources/dwc.csv")
(def test-file2 "resources/dwc2.csv")
(def test-file3 "resources/dwc3.csv")

(fact "Can read csv into hash-map"
  (read-csv test-file) => [{:scientificName "Aphelandra longiflora" :latitude "10.10" :longitude "20.20" :locality "riverrun"}
                           {:scientificName "Vicia faba" :latitude "30.3" :longitude "8.9" :locality ""}]
  (read-csv test-file2) => [{:scientificName "Aphelandra longiflora" :latitude "10.10" :longitude "20.20" :locality "riverrun"}
                           {:scientificName "Vicia faba" :latitude "30.3" :longitude "8.9" :locality ""}]
  (:globalUniqueIdentifier (last (read-csv test-file3) )) = "urn:lsid:biocol.org:col:15528")

(fact "Can write csv"
  (write-csv [{:scientificName "Foo" :locality "Riverrun" }
              {:scientificName "Bar" :id 1 :habitat "test"  :decimalLongitude 20.20 :decimalLatitude 10.10}
              {:scientificName "\"err\"" :foo "bar" }
              ])
      => (str "\"scientificName\",\"locality\",\"id\",\"habitat\",\"decimalLongitude\",\"decimalLatitude\",\"foo\"\n"
              "\"Foo\",\"Riverrun\",\"\",\"\",\"\",\"\",\"\"\n"
              "\"Bar\",\"\",\"1\",\"test\",\"20.2\",\"10.1\",\"\"\n\"'err'\",\"\",\"\",\"\",\"\",\"\",\"bar\""))
