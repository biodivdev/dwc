(ns dwc-io.xlsx
  (:use dk.ative.docjure.spreadsheet))

(defn col
 [c] 
  (org.apache.poi.ss.util.CellReference/convertNumToColString c))

(defn read-xlsx-stream
  "Read the xlsx as a stream calling fun at each line"
  [url fun]
  (let [workbook ^org.apache.poi.xssf.usermodel.XSSFWorkbook (load-workbook url)
        sheet    (.getSheetAt workbook 0)
        head     (vec (map read-cell
                   (->> sheet (.iterator) (iterator-seq) (first) (.cellIterator) (iterator-seq))))
        query    (reduce merge
                   (map #(hash-map (keyword (col (first %))) (keyword (last %) ))
                     (for [i (range 0 (count head))] [i (get head i)])))
        default  (reduce merge (map #(hash-map (keyword %) nil) head))]
    (doseq [row (rest (->> sheet (select-columns query)) )] 
      (fun (merge default row)))))

(defn read-xlsx
  "Read the xlsx as a whole returning all occurrences as a vector of hash-maps"
  [url]
  (let [occs (atom [])]
    (read-xlsx-stream url
      (fn [occ] 
        (swap! occs conj occ)))
    (deref occs)))

(defn write-xlsx
  [occurrences]
  (let [path (str (System/getProperty "java.io.tmpdir") "/dwc" (hash occurrences) ".xlsx")
        fields (mapv name (mapv key (reduce merge occurrences)))]
    (save-workbook! path 
     (create-workbook "occurrences"
      (vec
        (concat [fields]
       (vec
        (for [occ occurrences]
         (vec
          (for [f fields]
           (get occ (keyword f))))))))))
    path
    ))

