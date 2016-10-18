(ns dwc-io.fixes
  (:use [clojure.data.json :only [read-str write-str]]))

(def dwc-fix-schema
  (read-str 
    (slurp
      (clojure.java.io/resource "schema.json"))
    :key-fn keyword))

(def translations
  (read-str 
    (slurp
      (clojure.java.io/resource "translations.json"))
    :key-fn keyword))

(def fields (set (map key (:properties dwc-fix-schema))))

(def fields-low
 (apply merge
   (map 
    (fn [field]
      {(.toLowerCase (name field)) field})
    fields)))

(def fields-low
 (apply merge
   (map 
    (fn [prop]
      {(.toLowerCase (name (key prop))) (key prop)})
    (:properties dwc-fix-schema))))

(defn fix-spaces
  [occ]
  (let [fields [:scientificName :scientificNameAuthorship :scientificNameWithoutAuthorship :acceptedNameUsage]]
    (loop [occ occ fields fields]
      (let [f (first fields)]
      (if (nil? f) occ
        (if-not (nil? (occ f))
          (recur
            (assoc occ f (.trim (.replaceAll ^String (occ f) "[\\s]+" " ")))
            (rest fields))
          (recur occ (rest fields))))))))

(defn fix-naming-0
  ""
  [occ] 
  (let [{^String genus :genus ^String specificEpithet :specificEpithet 
         ^String scientificName :scientificName ^String scientificNameAuthorship :scientificNameAuthorship
         ^String scientificNameWithoutAuthorship :scientificNameWithoutAuthorship} occ]
    (cond 
      (and (not (nil? scientificNameAuthorship))
           (.contains scientificNameAuthorship " "))
           (recur (assoc occ :scientificNameAuthorship (.replaceAll scientificNameAuthorship "\\s" "")))
      (and (not (nil? genus))
           (not (nil? specificEpithet))
           (nil? scientificNameWithoutAuthorship))
        (recur (assoc occ :scientificNameWithoutAuthorship (str genus " " specificEpithet)))
      (and (nil? scientificNameWithoutAuthorship)
           (not (nil? scientificName))
           (not (nil? scientificNameAuthorship)))
         (recur (assoc occ :scientificNameWithoutAuthorship (.trim (.replace scientificName scientificNameAuthorship ""))))
      (and (nil? scientificName)
           (not (nil? scientificNameWithoutAuthorship))
           (not (nil? scientificNameAuthorship)))
         (recur (assoc occ :scientificName (.trim (str scientificNameWithoutAuthorship " " scientificNameAuthorship))))
      (and (not (nil? scientificName))
           (not (nil? scientificNameAuthorship))
           (not (.contains scientificName scientificNameAuthorship)))
         (recur (assoc occ :scientificName (str scientificName " " scientificNameAuthorship)))
      (and (not (nil? scientificName))
           (not (nil? scientificNameWithoutAuthorship))
           (nil? scientificNameAuthorship))
         (recur (assoc occ :scientificNameAuthorship (.trim (.replace scientificName scientificNameWithoutAuthorship ""))))
      (and (not (nil? scientificNameWithoutAuthorship))
           (nil? genus)
           (nil? specificEpithet))
         (recur (assoc occ :genus (first (.split scientificNameWithoutAuthorship  " "))
                           :specificEpithet (second (.split scientificNameWithoutAuthorship " "))))
      :else occ)))

(defn fix-naming
  [occ] (fix-spaces (fix-naming-0 occ)))

(defn translate
  [field taxa]
  (assoc taxa field 
    (or ((translations field) (keyword (field taxa)) ) (field taxa))))

(defn fix-translations
  [taxa] 
  (->> taxa
    (translate :taxonRank)
    (translate :taxonomicStatus)
      ))

(defn coord2decimal
""
[coord] 
 (let [ncoord (rest (re-find #"^([\d]+)[^\d]+([\d]+)[^\d]+([\d]+)[^\d]*([\w]{1})" coord))]
   (if (and (not (nil? ncoord)) (not (empty? ncoord)) (= 4 (count ncoord)) )
     (*
       (float
         (+ (Integer/valueOf ^String (nth ncoord 0))
            (/ (Integer/valueOf ^String (nth ncoord 1)) 60)
            (/ (Integer/valueOf ^String (nth ncoord 2)) 3600)))
       (if (or 
             (= "w" (nth ncoord 3))
             (= "W" (nth ncoord 3))
             (= "s" (nth ncoord 3))
             (= "S" (nth ncoord 3)))
         -1 1))
     coord)))

(defn fix-verbatim-coords
""
[occ] 
 (let [lat (:verbatimLatitude occ)
       lng (:verbatimLongitude occ)
       decLat (:decimalLatitude occ)
       decLng (:decimalLongitude occ)]
   (if (and (not (nil? decLat)) (not (nil? decLng)))
     occ
     (if (and (not (nil? lat)) (not (nil? lng)))
       (if (not (nil? (re-matches #"[0-9]+\.[0-9]+" lat)))
         (assoc occ :decimalLatitude lat :decimalLongitude lng)
         (assoc occ :latitude lat :longitude lng))
       occ))))

(defn fix-coords
""
[occ] 
 (let [lat (:latitude occ)
       lng (:longitude occ)
       decLat (:decimalLatitude occ)
       decLng (:decimalLongitude occ)]
   (if (and (not (nil? decLat)) (not (nil? decLng)))
     occ
     (if (and (not (nil? lat)) (not (nil? lng)))
       (assoc occ :decimalLatitude (coord2decimal lat) :decimalLongitude (coord2decimal lng))
       occ))))


(defn fix-decimal-long
[occ] 
(if (not (nil? (:decimalLongitude occ)))
  (if (string? (:decimalLongitude occ))
    (if (not (nil? (re-matches #"-?[0-9]+\.?[0-9]+" (:decimalLongitude occ))))
      (assoc occ :decimalLongitude (Double/valueOf ^String (:decimalLongitude occ)))
      (dissoc occ :decimalLongitude))
    (if (number? (:decimalLongitude occ))
      occ
      (dissoc occ :decimalLongitude)))
  (dissoc occ :decimalLongitude)))

(defn fix-decimal-lat
[occ] 
(if (not (nil? (:decimalLatitude occ)))
  (if (string? (:decimalLatitude occ))
    (if (not (nil? (re-matches #"-?[0-9]+\.?[0-9]+" (:decimalLatitude occ))))
      (assoc occ :decimalLatitude (Double/valueOf ^String (:decimalLatitude occ)))
      (dissoc occ :decimalLatitude))
    (if (number? (:decimalLatitude occ))
      occ
      (dissoc occ :decimalLatitude)))
  (dissoc occ :decimalLatitude)))

(defn fix-decimal-coords
""
[occ]
(-> occ
    (fix-decimal-lat)
    (fix-decimal-long)))

(defn fix-nils
  [occ]
  (into {} 
   (filter  
     #(and (not (nil? (val %)))
        (if (string? (val %))
          (not (empty? (val %))) true))
           occ)))

(defn string-0
  [s] 
  (if (string? s)
    s
    (String/valueOf s)))

(defn maybe-string
  [kv] 
 (let [v (val kv)]
   {(key kv) (string-0 v)}
   ))

(defn fix-strings
""
[occ] 
 (into {}
  (map 
    maybe-string
    (fix-nils occ))))

(defn fix-id-taxon
""
[taxon] 
 (if-not (nil? (:taxonID taxon)) taxon
   (if-not (nil? (:id taxon)) (assoc taxon :taxonID (:id taxon))
     (if-not (nil? (:globalUniqueIdentifier taxon)) (assoc taxon :taxonID (:globalUniqueIdentifier taxon))
         (assoc taxon :taxonID (str "urn:taxon:" (java.util.UUID/randomUUID)))))))

(defn fix-id-occ
""
[occ] 
 (if-not (nil? (:occurrenceID occ)) occ
   (if-not (nil? (:id occ)) (assoc occ :occurrenceID (:id occ))
     (if-not (nil? (:globalUniqueIdentifier occ)) (assoc occ :occurrenceID (:globalUniqueIdentifier occ))
       (if (and (not (nil? (:institutionCode occ)))
                (not (nil? (:collectionCode occ)))
                (not (nil? (:catalogNumber occ))) )
          (assoc occ :occurrenceID (str "urn:occurrence:" 
                                        (:institutionCode occ) ":" 
                                        (:collectionCode occ) ":"
                                        (:catalogNumber occ)))
         (assoc occ :occurrenceID (str "urn:occurrence:" (java.util.UUID/randomUUID))))))))

(def fix-id fix-id-occ)

(defn proper-field
""
[k]
  (fields-low (.toLowerCase (name k))))

(defn fix-keys
  "" 
  [occ] 
   (apply merge {}
     (map
      (fn [kv] {(proper-field (key kv)) (val kv)})
      occ)))

(defn fix-fields
  ""
  [occ] 
  (reduce merge {}
    (filter 
      (fn [kv] (fields (key kv)))
      occ)))

(defn rm-dot-zero
  ""
  [kv]
  {(key kv) (clojure.string/replace (val kv) #"\.0$" "") })

(defn fix-dot-zero
  ""
  [occ]
   (reduce merge
    (map rm-dot-zero occ)))

(defn fix-taxon
  ""
  [data]
   (if (vector? data) 
      (filter #(not (nil? %)) (map fix-taxon data))
      (if (or (nil? data) (empty? data)) nil
        (-> data
            fix-keys
            fix-strings
            fix-fields
            fix-id-taxon
            fix-naming
            fix-translations
            ))))


(defn fix-occ
  ""
  [data]
   (if (vector? data) 
      (filter #(not (nil? %)) (map fix-occ data))
      (if (or (nil? data) (empty? data)) nil
        (-> data
            fix-keys
            fix-strings
            fix-dot-zero
            fix-fields
            fix-id-occ
            fix-naming
            fix-decimal-lat
            fix-decimal-long
            fix-verbatim-coords
            fix-coords
            ))))

(defn -fix->
  ""
  [data] 
   (if (vector? data) 
      (filter #(not (nil? %)) (map -fix-> data))
      (if (or (nil? data) (empty? data)) nil
        (-> data
            fix-keys
            fix-strings
            fix-dot-zero
            fix-fields
            fix-id
            fix-naming
            fix-decimal-lat
            fix-decimal-long
            fix-verbatim-coords
            fix-coords
            ))))

