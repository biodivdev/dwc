# dwc-io

This is a simple clojura library for writing, accessing and reading darwincore occurrences.

## Usage

Include in your project.clj

[![Clojars Project](http://clojars.org/dwc-io/latest-version.svg)](http://clojars.org/dwc-io)

## Reading and Writing

### dwc-A

    (use 'dwc-io.archive)
    (let [records (read-archive "url for dwc-a zip")]
      (comment "records is a vector of maps, with dwc-io fields as keywords keys"))

    (read-archive-stream "url for dwc-a zip" 
     (fn [record]
      (comment "reads the archive as a stream")))

### CSV

    (use 'dwc-io.csv) 
    (let [records (read-csv "path-to.csv")]
      (comment "records is a vector of maps, with dwc fields as keywords keys")))

    (read-csv-stream "path-to.csv"
      (function [record]
        (comment "reads the csv as a stream")))

    (spit "dwc-io.csv"
        (write-csv [{:scientificName "Aphelandra longiflora" } {:scientificName "Aphelandra espirito-santensis"}]))

### JSON & GeoJSON

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

### XLSX

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

### GBIF Search API

    (use 'dwc-io.gbif)
    (let [opts {:filters {"Family" "BROMELIACEAE"} :limit 30} ; omit limit to loop until the end
          result (read-gbif opts)]
     (comment "all options are optional, any combination is valid")
     (comment "comes back as {:results [{recordhere}] :endOfRecords true :count 30 :offset 0 :limit 30}"))

### Tapir

    (use 'dwc-io.tapir)
    (let [opts {:fields ["ScientificName"] :filters {"Family" "BROMELIACEAE"} :start 10 :limit 30}
          records (read-tapir "url" opts)]
     (comment "all options are optional, any combination is valid")
     (comment "comes back as {:records [{recordhere}] :summary {:start 10 :next 30 :total 1000 :end false}}"))

### Digir

    (use 'dwc-io.digir)
    (let [opts {:filters {"Family" "BROMELIACEAE"} :start 10 :limit 30}
          records (read-digir "url" opts)]
     (comment "all options are optional, any combination is valid")
     (comment "comes back as {:records [{recordhere}] :summary {:total 1000 :start 10 :limit 30 :end false}}"))

## Applying common fixes

Fixes some common problems with occurrence records.

Current fixes:

- normalize keys: ScientificName -> scientificName
- decimal data into double values
- empty and nul fields get removed
- all non decimal fields as strings
- generade occurrenceID from: id, globalUniqueIdentifier, instutition+catalog+number or generate an UUID
- transform latitude into decimalLatitude (and longitude), if applicable, converting coordinates (radians2decimal)
- transform verbatimLatitude into decimalLatitude (and longitude), if applicable, converting coordinates (radians2decimal)
- remove non standart fields
- trim spaces
- normalize the use of the taxonomic fields (like scientificName = scientificNameWithoutAuthorship + scientificNameAuthorship, and the other way)


    (use 'dwc-io.fixes)

    (fix-id record)
    (comment "tries to generate an recordID, if not present, based on institution+collection+number")

    (fix-coords record)
    (Comment "trie to convert degree latitude longitude fields into decimalLatitude and decimalLongitude")

    (-fix-> record)
    (comment "apply all fixes, in single or vector of records")

## Validation

This feature validate against the darwincore schema, which is mostly the presence of known only fields, but not their contents.

    (use 'dwc-io.validation)

    (validate record)
    (comment "tries to validate record based on information of http://rs.tdwg.org/dwc/terms/")

## License

MIT

