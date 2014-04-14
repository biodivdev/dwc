(ns dwc.tapir-test
  (:use midje.sweet)
  (:use dwc.tapir))

(def test-url "http://tapirlink.jbrj.gov.br/tapir.php/RBw")

  (fact "Can create proper XML for request"
      (let [xml0 (make-xml test-url)
            xml1 (make-xml test-url {:fields ["ScientificName"]})]
        (.contains xml0 
         "<node path=\"/records/record/GlobalUniqueIdentifier\">")
             => true
        (.contains xml0
         "<concept id=\"http://rs.tdwg.org/dwc/dwcore/GlobalUniqueIdentifier\"/>")
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
      (let [xml (make-xml test-url {:filters {"ScientificName" "Cobra"
                                              "DecimalLongitude" "123"}})
            xml0 (make-xml test-url {})]
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
      (let [xml (make-xml test-url {:start 10 :limit 30})
            xml0 (make-xml test-url {} )]
        (.contains xml
           "<search count=\"true\" start=\"10\" limit=\"30\"")
            => true
        (.contains xml0
           "<search count=\"true\" start=\"0\" limit=\"20\"")
            => true
        ))

  (fact "Capable of capabilities"
     (let [caps (map key (:fields (get-capabilities test-url)) )]
       (first caps) => "StateProvince"
       (last caps) => "InfraspecificEpithet"))
  
  (fact "Can read a tapir source"
    (let [occurrences (read-tapir test-url {:fields ["ScientificName" "InstitutionCode"]})]
      (:scientificName (first (:records occurrences)))
         => "CANELLACEAE"
      (:institutionCode (first (:records occurrences)))
         => "JBRJ"
      (:end (:summary occurrences)) 
         => false)
    (let [occurrences (read-tapir test-url {:fields ["ScientificName"] :filters {"Family" "ACANTHACEAE"}})]
      (:scientificName (first (:records occurrences)))
         => "Mendoncia velloziana Mart.")
    (let [occurrences (read-tapir test-url {:filters {"Family" "ACANTHACEAE"} :start 0 :limit 10})]
      (:scientificName (first (:records occurrences)))
         => "Trichanthera gigantea Kunth")
    (let [occurrences (read-tapir test-url {:filters {"Family" "ACANTHACEAE"} :start 0 :limit 10 :fields []})]
      (:scientificName (first (:records occurrences)))
         => "Trichanthera gigantea Kunth")
        )

