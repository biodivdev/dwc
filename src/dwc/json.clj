(ns dwc.json
  (:use [clojure.data.json :only [read-str write-str]] ))

(defn read-json-stream
  "Read the Json as a stream, passing each occurrence to your function"
  [url fun] 
   (let [occurrences (read-str (slurp url) :key-fn keyword)]
     (doseq [occ occurrences]
       (fun occ))))

(defn read-json
  "Read the Json as a whole returning all occurrences as a vector"
  [url]
  (let [occs (atom [])]
    (read-json-stream url
      (fn [occ] 
        (swap! occs conj occ)))
    (deref occs)))

(defn write-json
  "Write a json from occurrences data"
  [occurrences] 
   (write-str occurrences))

