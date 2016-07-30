(ns dwc-io.gbif-test
(:use midje.sweet)
(:use dwc-io.gbif))

(fact "Can read from gbif"
  (let [occurrences (read-gbif {"scientificName" "Aphelandra longiflora"})]
    (:scientificName (first (:results occurrences)))
       => "Aphelandra longiflora (Lindl.) Profice"))

(fact "Can read from gbif recur"
  (let [occurrences (read-gbif {"scientificName" "Vicia faba" :limit 900})]
    (count (:results occurrences)) => 900)
  (let [occurrences (read-gbif {"scientificName" "Vicia faba" :limit 1200})]
    (count (:results occurrences)) => 1200))
