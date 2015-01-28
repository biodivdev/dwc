(ns dwc-io.fixes
  (:use [clojure.data.json :only [read-str write-str]] ))

(def dwc-fix-schema
  (read-str (slurp (clojure.java.io/resource "schema.json") ) :key-fn keyword))

(defn coord2decimal
  ""
  [coord] 
   (let [coord (rest (re-find #"^([\d]+)[^\d]+([\d]+)[^\d]+([\d]+)[^\d]*([\w]{1})" coord))]
     (*
       (float
       (+ (Integer/valueOf (nth coord 0))
          (/ (Integer/valueOf (nth coord 1)) 60)
          (/ (Integer/valueOf (nth coord 2)) 3600)
       ))
       (if (or 
             (= "w" (nth coord 3))
             (= "W" (nth coord 3))
             (= "s" (nth coord 3))
             (= "S" (nth coord 3)))
         -1 1))))

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
         occ)
       )
   ))

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
         occ)
       )
   ))

(defn fix-decimal-long
  ""
  [occ] 
   (if (and (not (nil? (:decimalLongitude occ)))
            (string? (:decimalLongitude occ))
            (> (count (:decimalLongitude occ)) 0))
     (assoc occ :decimalLongitude (Double/valueOf (:decimalLongitude occ)))
     occ))

(defn fix-decimal-lat
  ""
  [occ] 
   (if (and (not (nil? (:decimalLatitude occ)))
            (string? (:decimalLatitude occ))
            (> (count (:decimalLatitude occ)) 0))
     (assoc occ :decimalLatitude (Double/valueOf (:decimalLatitude occ)))
     occ))

(defn fix-strings
  ""
  [occ]
   (reduce merge {}
    (for [kv occ]
      (if (not (nil? (val kv)))
        (hash-map (key kv) (.toString (val kv)))))))

(defn fix-empties
  ""
  [occ]
   (reduce merge {}
    (for [kv occ]
      (if (and (not (empty? (val kv))) (not (nil? (val kv))))
        (hash-map (key kv) (val kv))))))

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
  (if-let [fixed
    (keyword
      (first
        (filter (fn [k2] (= (.toLowerCase (name k2)) (.toLowerCase (name k)))) 
            (map key
                 (:properties dwc-fix-schema))))
    )]
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
   (let [ok-fields (map key (:properties dwc-fix-schema))]
     (fix-empties
       (reduce merge 
           (map (fn [k] (hash-map k (k occ))) ok-fields)))))

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
            fix-coords)))
      )

