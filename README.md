# dwc-io

This is a simple clojure/java library for writing, accessing and reading occurrences.

Current features:
- Reading and streaming from: dwc-A, XLSX, CSV, JSON, GEOJSON, TAPIR, DIGIR, GBIF
- Writing to: JSON, GEOJSON, XLSX, CSV
- Search, Filters and pagging on Tapir and Digir
- Validate records
- Apply common fixes
-- verbatimCoordinates vs decimalLatitude/decimalLongitude
-- fields and keys case (DecimalLatitude vs decimalLatitude)
-- empty and null values
-- naming fields (scientifcName, scientificNameWithoutAuthorship, scientificNameAuthorship)
-- occurrenceID generation, if not exists, as one of:
--- id field
--- globalUniqueIdentifier field
--- institutionCode:collectionCode:catalogNumber
--- randomUUID

Missing features:
- Java Interface
- Finish validation
- Writing on dwc-A and XLSX 

## Usage

### From Clojure

Include in your project.clj

    [dwc-io "0.0.39"]

### dwc-A

    (use 'dwc-io.archive)
    (let [records (read-archive "url for dwc-a zip")]
      (comment "records is a vector of maps, with dwc-io fields as keywords keys"))

    (read-archive-stream "url for dwc-a zip" 
     (fn [record]
      (comment "reads the archive as a stream")))

#### CSV, using ';'(column) as  separator and '"'(double-quotes) as quote

    (use 'dwc-io.csv) 
    (let [records (read-csv "path-to.csv")]
      (comment "records is a vector of maps, with dwc fields as keywords keys")))

    (read-csv-stream "path-to.csv"
      (function [record]
        (comment "reads the csv as a stream")))

    (spit "dwc-io.csv"
        (write-csv [{:scientificName "Aphelandra longiflora" } {:scientificName "Aphelandra espirito-santensis"}]))

#### JSON & GeoJSON

    (use 'dwc-io.json) 
    (let [records (read-json "path-to.json")]
      (comment "records is a vector of maps, with dwc fields as keywords keys")))

    (read-json-stream "path-to.json"
      (function [record]
        (comment "reads the json as a stream")))

    (use 'dwc-io.geojson) 
    (let [records (read-geojson "path-to.xlsx")]
      (comment "records is a vector of maps, with dwc fields as keywords keys")))

    (read-geojson-stream "path-to.xlsx"
      (function [record]
        (comment "reads the geojson as a stream")))

    (comment "Support writing")
    (spit "occs.gjson" (write-geojson [ {:decimalLatitude 10 :decimalLongitude 20 :scientificName "Vicia faba"} ]))
    (spit "occs.json" (write-json [ {:decimalLatitude 10 :decimalLongitude 20 :scientificName "Vicia faba"} ]))

#### XLSX

    (use 'dwc-io.xlsx) 
    (let [records (read-xlsx "path-to.xlsx")]
      (comment "records is a vector of maps, with dwc fields as keywords keys")))

    (read-xlsx-stream "path-to.xlsx"
      (function [record]
        (comment "reads the xlsx as a stream")))

    (use 'clojure.java.io)
    (copy
        (as-file (write-csv [{:scientificName "Aphelandra longiflora" } {:scientificName "Aphelandra espirito-santensis"}]))
        (as-file "./dwc.xlsx"))

#### GBIF

    (use 'dwc-io.gbif)
    (let [opts {:filters {"Family" "BROMELIACEAE"} :limit 30} ; omit limit to loop until the end
          result (read-gbif opts)]
     (comment "all options are optional, any combination is valid")
     (comment "comes back as {:results [{recordhere}] :endOfRecords true :count 30 :offset 0 :limit 30}"))

#### Tapir

    (use 'dwc-io.tapir)
    (let [opts {:fields ["ScientificName"] :filters {"Family" "BROMELIACEAE"} :start 10 :limit 30}
          records (read-tapir "url" opts)]
     (comment "all options are optional, any combination is valid")
     (comment "comes back as {:records [{recordhere}] :summary {:start 10 :next 30 :total 1000 :end false}}"))

#### Digir

    (use 'dwc-io.digir)
    (let [opts {:filters {"Family" "BROMELIACEAE"} :start 10 :limit 30}
          records (read-digir "url" opts)]
     (comment "all options are optional, any combination is valid")
     (comment "comes back as {:records [{recordhere}] :summary {:total 1000 :start 10 :limit 30 :end false}}"))

#### Applying fixes

    (use 'dwc-io.fixes)

    (fix-id record)
    (comment "tries to generate an recordID, if not present, based on institution+collection+number")

    (fix-coords record)
    (Comment "trie to convert degree latitude longitude fields into decimalLatitude and decimalLongitude")

    (-fix-> record)
    (comment "apply all fixes, in single or vector of records")

Current fixes:
- normalize keys: ScientificName -> scientificName
- decimal data into double values
- empty fields get removed
- generade occurrenceID from: id, globalUniqueIdentifier, instutition+catalog+number or generate a unique uud
- transform latitude into decimalLatitude (and longitude), if applicable, converting coordinates (radians2decimal)
- transform verbatimLatitude into decimalLatitude (and longitude), if applicable, converting coordinates (radians2decimal)
- remove non std fields

#### Validation

    (use 'dwc-io.validation)

    (validate record)
    (comment "tries to validate record based on information of http://rs.tdwg.org/dwc/terms/")

### From Java

Soon

## License

MIT

