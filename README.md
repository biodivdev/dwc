# dwc

This is a simple clojure/java library for accessing and reading DwC-A, TAPIR and DIGIR data sources, for occurrences and checklists.

Current features:
- Reading, streaming and writing from: DwC-A, XLSX, CSV, JSON, GEOJSON, TAPIR, DIGIR
- Filters and pagging on Tapir
- Filters and pagging on Digir

Missing features:
- Java Interface
- Writing on DwC-A, XLSX, 

## Usage

### From Clojure

### DwC-A

    (use 'dwc.archive)
    (let [occurrences (read-archive "url for dwc-a zip")]
      (comment "occurrences is a vector of maps, with dwc fields as keywords keys"))

    (read-archive-stream "url for dwc-a zip" 
     (fn [occurrence]
      (comment "reads the archive as a stream")))

#### CSV, using ','(comma) as  separator and '"'(double-quotes) as quote

    (use 'dwc.csv) 
    (let [occurrences (read-csv "path-to.csv")]
      (comment "occurrences is a vector of maps, with dwc fields as keywords keys")))

    (read-csv-stream "path-to.csv"
      (function [occurrence]
        (comment "reads the csv as a stream")))

#### JSON & GeoJSON

    (use 'dwc.json) 
    (let [occurrences (read-json "path-to.json")]
      (comment "occurrences is a vector of maps, with dwc fields as keywords keys")))

    (read-json-stream "path-to.json"
      (function [occurrence]
        (comment "reads the json as a stream")))

    (use 'dwc.geojson) 
    (let [occurrences (read-geojson "path-to.xlsx")]
      (comment "occurrences is a vector of maps, with dwc fields as keywords keys")))

    (read-geojson-stream "path-to.xlsx"
      (function [occurrence]
        (comment "reads the geojson as a stream")))

    (comment "Support writing")
    (spit "occs.gjson" (write-geojson {:decimalLatitude 10 :decimalLongitude 20 :scientificName "Vicia faba"}))
    (spit "occs.json" (write-json {:decimalLatitude 10 :decimalLongitude 20 :scientificName "Vicia faba"}))

#### XLSX

    (use 'dwc.xlsx) 
    (let [occurrences (read-xlsx "path-to.xlsx")]
      (comment "occurrences is a vector of maps, with dwc fields as keywords keys")))

    (read-xlsx-stream "path-to.xlsx"
      (function [occurrence]
        (comment "reads the xlsx as a stream")))

#### Tapir

    (use 'dwc.tapir)
    (let [opts {:fields ["ScientificName"] :filters {"Family" "BROMELIACEAE"} :start 10 :limit 30}
          records (read-tapir "url" opts)]
     (comment "all options are optional, any combination is valid")
     (comment "comes back as {:records [{recordhere}] :summary {:start 10 :next 30 :total 1000 :end false}}"))

#### Digir

    (use 'dwc.digir)
    (let [opts {:filters {"Family" "BROMELIACEAE"} :start 10 :limit 30}
          records (read-digir "url" opts)]
     (comment "all options are optional, any combination is valid")
     (comment "comes back as {:records [{recordhere}] :summary {:total 1000 :start 10 :limit 30 :end false}}"))

### From Java

Soon

## License

Copyright © 2013 Centro Nacional de Conservação da Flora, Instituto de Pesquisa Jardim Botânico do Rio de Janeiro.

Distributed under the Eclipse Public License.

