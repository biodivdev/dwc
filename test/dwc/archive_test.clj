(ns dwc.archive-test
  (:use dwc.archive)
  (:use midje.sweet))

(def test-url "http://data.canadensys.net/ipt/archive.do?r=win-vascular-specimens")
(def zip (java.util.zip.ZipFile. (download test-url)))

(fact "Can find core tag, config of csv and fields."
  (let [core   (get-core zip)
        fields (get-fields core)
        file   (get-file core)
        config (get-config core)]
    (:tag core) => :core
    file => "occurrence.txt"
    config => {:separator \tab :ignoreFirst true :quote nil}
    (first fields) => {:index 1  :name :type}
    (last  fields) => {:index 47 :name :nomenclaturalCode}))

(fact "Can read DwC-A"
 (let [occurrences (read-archive test-url)]
   (:scientificName (first occurrences)) => "Polypodium sibiricum Sipl."
   (:recordedBy (last occurrences)) => "D. Punter"))

