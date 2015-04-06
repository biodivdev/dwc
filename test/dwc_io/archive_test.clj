(ns dwc-io.archive-test
  (:use dwc-io.archive)
  (:use midje.sweet))

(def test-url  (clojure.java.io/resource "dwca-redlist_2013_occs.zip"))
(def test-url2 "http://ipt.jbrj.gov.br/ipt/archive.do?r=redlist_2013_taxons")

(fact "Can find core tag, config of csv and fields."
  (let [zip    (java.util.zip.ZipFile. (download test-url))
        core   (get-core zip)
        fields (get-fields core)
        file   (get-file core)
        config (get-config core)]
    (:tag core) => :core
    file => "occurrence.txt"
    config => {:separator \tab :ignoreFirst true :quote \u0000}
    (first fields) => {:index 1  :name :language}
    (last  fields) => {:index 23 :name :taxonomicStatus}))

(fact "Can read DwC-A"
 (let [occurrences (read-archive test-url)]
   (:scientificName (first occurrences)) => "Justicia scheidweileri"
   (:recordedBy (last occurrences)) => "Miranda, A.M."))

(fact "Can read DwC-A taxons"
  (let [taxons (read-archive test-url2)]
    (:scientificName (first taxons)) => "Micropholis caudata"))

