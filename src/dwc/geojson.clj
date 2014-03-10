(ns dwc.geojson
  (:use [clojure.data.json :only [read-str write-str]]))

(defn read-geojson-stream
  "Read the GEOJson as a stream, passing each occurrence to your function"
  [url fun] 
  (let [geojson (read-str (slurp url) :key-fn keyword)]
    (doseq [feature (:features geojson)]
      (fun (assoc (:properties feature) 
                   :decimalLongitude (first (:coordinates (:geometry feature) ))
                   :decimalLatitude (second (:coordinates (:geometry feature)))))) ))

(defn read-geojson
  "Read the GEOJson as a whole returning all occurrences as a vector"
  [url]
  (let [occs (atom [])]
    (read-geojson-stream url
      (fn [occ] 
        (swap! occs conj occ)))
    (deref occs)))

(defn occurrence2feature
  "Convert a occurrence hash-map to a geojson feature"
  [occ] {:properties (dissoc occ :decimalLatitude :decimalLongitude)
         :type "Feature"
         :geometry {
          :type "Point" 
          :coordinates [(:decimalLongitude occ) (:decimalLatitude occ)]
         }})

(defn write-geojson
  "Return GEOJSon representation of occurrences"
  [occurrences] 
   (write-str
      {:type "FeatureCollection"
       :features (map occurrence2feature occurrences)}))

