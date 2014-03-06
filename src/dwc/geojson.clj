(ns dwc.geojson
  (:use [clojure.data.json :only [read-str write-str]]))

(defn read-geojson-stream
  "Read the GEOJson as a stream, passing each occurrence to your function"
  [url fun] nil)

(defn read-geojson
  "Read the GEOJson as a whole returning all occurrences as a vector"
  [url]
  (let [occs (atom [])]
    (read-geojson-stream url
      (fn [occ] 
        (swap! occs conj occ)))
    (deref occs)))

(defn write-geojson
  ""
  [occurrences] nil)

