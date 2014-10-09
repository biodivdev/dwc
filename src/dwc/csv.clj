(ns dwc.csv
  (:use dwc.validation)
  (:require [clojure.java.io :as io])
  (:require [clojure.data.csv :as csv]))

(defn read-csv-stream
  "Read the csv as a stream calling fun at each line"
  [url fun]
  (with-open [in-file (io/reader url)]
    (let [csv (csv/read-csv in-file :separator \; :quote \")
          head (first csv)
          content (rest csv)]
      (doseq [line content]
        (fun
          (reduce merge {}
            (for [i (range 0 (count head))]
              (hash-map (keyword (get head i)) (get line i)))))))))

(defn read-csv
  "Read the csv as a whole returning all occurrences as a vector of hash-maps"
  [url]
  (let [occs (atom [])]
    (read-csv-stream url
      (fn [occ] 
        (swap! occs conj occ)))
    (deref occs)))

(defn write-csv
  "Return CSV of occurrences"
  [occurrences] 
   (let [in-fields  (mapv name (mapv key (reduce merge occurrences)))
         fields  (filter (partial contains? (set in-fields)) all-fields)]
     (apply str 
      (concat
        (interpose ";" (map #(str "\"" % "\"") fields))
        (list "\n")
        (flatten
          (interpose "\n"
           (for [occ occurrences]
             (interpose ";"
                 (for [f fields]
                   (str "\"" (get occ (keyword f)) "\"" ))))))))))

