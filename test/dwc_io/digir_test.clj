(ns dwc-io.digir-test
  (:use midje.sweet)
  (:use dwc-io.digir))

(def test-url "http://www.kew.org/digir/www/DiGIR.php")

#_(fact "Can create a nice req xml"
  (let [xml0 (make-xml test-url )
        xml1 (make-xml test-url {:start 10 :limit 20 :filters {"ScientificName" "ACANTHACEAE"}})]
    (.contains xml1
    "<dwc:ScientificName>ACANTHACEAE</dwc:ScientificName>")
      => true
    (.contains xml0 "<and>") 
      => false
    (.contains xml0 test-url)
      => true
    ))

#_(fact "Can read digir resources"
  (let [res (read-digir test-url {:filters {"ScientificName" "Quercus alba L. var. pinnatifida Michx."}})]
    (:total (:summary res)) => 1
    (:end (:summary res)) => true
    (:start (:summary res)) => 0
    (:limit (:summary res)) => 20
    (:InstitutionCode (first (:records res))) => "K"
  ))

