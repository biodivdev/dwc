(ns dwc.csv-test
  (:use midje.sweet)
  (:use dwc.csv))

(def test-file "resources/dwc.csv")

(fact "Can read csv into hash-map"
  (read-csv test-file) => [{:scientificName "Aphelandra longiflora" :latitude "10.10" :longitude "20.20" :locality "riverrun"}
                           {:scientificName "Vicia faba" :latitude "30.3" :longitude "8.9" :locality ""}])

(fact "Can write csv"
  (write-csv [{:scientificName "Foo" :locality "Riverrun" }
              {:scientificName "Bar" :id 1 :habitat "test"  :decimalLongitude 20.20 :decimalLatitude 10.10}])
   => (str "\"id\";\"habitat\";\"locality\";\"decimalLatitude\";\"decimalLongitude\";\"scientificName\"\n"
           "\"\";\"\";\"Riverrun\";\"\";\"\";\"Foo\"\n"
           "\"1\";\"test\";\"\";\"10.1\";\"20.2\";\"Bar\"")
      )

(fact "Write CSV 2"
  (let [csv (write-csv (dwc.json/read-json "http://cncflora.jbrj.gov.br/floradata/api/v1/occurrences?scientificName=Aphelandra+acrensis"))]
    csv => (slurp "resources/dwc2.csv")
    ))

(fact "Write CSV 3"
  (let [csv (write-csv (dwc.json/read-json "http://cncflora.jbrj.gov.br/floradata/api/v1/occurrences?scientificName=Anisacanthus+trilobus"))]
    csv => (slurp "resources/dwc3.csv")
    ))

