(ns dwc.fixes)

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

(defn fix-id
  ""
  [occ] 
   (if-not (nil? (:occurrenceID occ)) occ
     (if-not (nil? (:id occ)) (assoc occ :occurrenceID (:id occ))
       (if (and (not (nil? (:institutionCode occ)))
                (not (nil? (:collectionCode occ)))
                (not (nil? (:catalogNumber occ))) )
          (assoc occ :occurrenceID (str "urn:occurrence:" 
                                        (:institutionCode occ) ":" 
                                        (:collectionCode occ) ":"
                                        (:catalogNumber occ)))
         (assoc occ :occurrenceID (str "urn:occurrence:" (java.util.UUID/randomUUID)))))))

(defn -fix->
  ""
  [data] 
   (if (vector? data) 
      (map -fix-> data)
      (-> data
          fix-id
          fix-coords)))

