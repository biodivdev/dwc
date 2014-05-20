(ns dwc.gbif-test
(:use midje.sweet)
(:use dwc.gbif))

(fact "Can read from gbif"
  (let [occurrences (read-gbif {"scientificName" "Aphelandra longiflora"})]
    (:scientificName (first (:results occurrences)))
       => "Aphelandra longiflora (Lindl.) Profice"))

