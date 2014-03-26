(ns dwc.archive-test
  (:use dwc.archive)
  (:use midje.sweet))

(def test-url (clojure.java.io/resource "dwca-occurrences.zip" ))
(comment (def test-url2 (clojure.java.io/resource "dwca-taxons.zip")))

(fact "Can find core tag, config of csv and fields."
  (let [zip    (java.util.zip.ZipFile. (download test-url))
        core   (get-core zip)
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

(comment
  (fact "Can read DwC-A taxons"
    (let [taxons (read-archive test-url2)]
      (println taxons)
      (:scientificName (first taxons)) => "Agaricales")))

