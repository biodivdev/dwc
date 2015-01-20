(ns dwc-io.geojson-test
  (:use midje.sweet)
  (:use [clojure.data.json :only [read-str write-str]])
  (:use dwc-io.geojson))

(def test-file "resources/dwc.geojson")

(fact "Can read geojson into hash-map"
  (read-geojson test-file) => [{:scientificName "Aphelandra longiflora" :decimalLatitude 10.10 :decimalLongitude 20.20 :locality "riverrun"}
                               {:scientificName "Vicia faba" :decimalLatitude 30.3 :decimalLongitude 8.9 :locality ""}])

(fact "Can write geojson"
  (let [occs (read-geojson test-file)
        geojson (write-geojson occs)]
    (read-str geojson) => (read-str (slurp test-file))))

