(ns dwc-io.fixes
  (:use [clojure.data.json :only [read-str write-str]]))

(def dwc-fix-schema
  (read-str 
    (slurp
      (clojure.java.io/resource "schema.json"))
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

(defn coord2decimal
""
[coord] 
 (let [ncoord (rest (re-find #"^([\d]+)[^\d]+([\d]+)[^\d]+([\d]+)[^\d]*([\w]{1})" coord))]
   (if (and (not (nil? ncoord)) (not (empty? ncoord)) (= 4 (count ncoord)) )
     (*
       (float
         (+ (Integer/valueOf (nth ncoord 0))
            (/ (Integer/valueOf (nth ncoord 1)) 60)
            (/ (Integer/valueOf (nth ncoord 2)) 3600)))
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
         (assoc occ :latitude lat :longitude lng)
         )
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
      (assoc occ :decimalLongitude (Double/valueOf (:decimalLongitude occ)))
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
      (assoc occ :decimalLatitude (Double/valueOf (:decimalLatitude occ)))
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

(defn fix-empties
""
[occ]
 (reduce merge {}
  (for [kv occ]
    (if (and (not (empty? (val kv))) (not (nil? (val kv))))
      (hash-map (key kv) (val kv))))))

(defn fix-strings
""
[occ]
 (fix-empties
   (apply merge
    (for [kv occ]
      {(key kv)
        (if (string? (val kv))
          (.trim (val kv))
          (.trim (String/valueOf (or (val kv) ""))))}))))

(defn fix-id
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

(defn proper-field
""
[k]
(if-let [fixed (fields-low (.toLowerCase (name k) ))]
    fixed
    k))

(defn fix-keys
  "" 
  [occ] 
   (reduce merge
     (map
      (fn [kv] (hash-map (proper-field (key kv)) (val kv)))
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
            fix-decimal-lat
            fix-decimal-long
            fix-verbatim-coords
            fix-coords
            ))))

