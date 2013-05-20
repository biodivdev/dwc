(ns dwc.tapir-test
  (:use midje.sweet)
  (:use dwc.tapir))

(def test-url "http://tapirlink.jbrj.gov.br/tapir.php/RBdna")

  (fact "Can create proper XML for request"
      (let [xml0 (make-xml)
            xml1 (make-xml {:fields ["ScientificName"]})]
        (.contains xml0 
         "<node path=\"/records/record/GlobalUniqueIdentifier\">")
             => true
        (.contains xml0
         "<concept id=\"http://rs.tdwg.org/dwc/dwcore/GlobalUniqueIdentifier\"/>")
             => true
        (.contains xml0 
         "<node path=\"/records/record/IdentifiedBy\">")
             => true
        (.contains xml0
         "<concept id=\"http://rs.tdwg.org/dwc/curatorial/IdentifiedBy\"/>")
             => true
  
        (.contains xml0
          "<xs:element name=\"ScientificName\" type=\"xs:string\"/>")
             => true
  
        (.contains xml1 
         "<node path=\"/records/record/AssociatedMedia\">")
             => false 
        (.contains xml1
          "<xs:element name=\"ScientificName\" type=\"xs:string\"/>")
            => true
        ))
  
  
  (fact "Can create XML with filters"
      (let [xml (make-xml {:filters {"ScientificName" "Cobra"
                                     "DecimalLongitude" "123"}})
            xml0 (make-xml {})]
        (.contains xml
          "<concept id=\"http://rs.tdwg.org/dwc/dwcore/ScientificName\"/>") => true
        (.contains xml
          "<literal value=\"Cobra\"/>") => true
        (.contains xml
          "<concept id=\"http://rs.tdwg.org/dwc/geospatial/DecimalLongitude\"/>") => true
        (.contains xml
          "<literal value=\"123\"/>") => true
        (.contains xml0 "<and>") => false
      ))

  (fact "And does pagging"
      (let [xml (make-xml {:start 10 :limit 30})
            xml0 (make-xml {} )]
        (.contains xml
           "<search count=\"true\" start=\"10\" limit=\"30\"")
            => true
        (.contains xml0
           "<search count=\"true\" start=\"0\" limit=\"20\"")
            => true
        ))
  
  (fact "Can read a tapir source"
    (let [occurrences (read-tapir test-url {:fields ["ScientificName" "InstitutionCode"]})]
      (:ScientificName (first (:records occurrences)))
         => "BROMELIACEAE"
      (:InstitutionCode (first (:records occurrences)))
         => "JBRJ"
      (:end (:summary occurrences)) 
         => false
      ))
  
