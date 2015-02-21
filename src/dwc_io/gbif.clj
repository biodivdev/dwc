(ns dwc-io.gbif
  (:use [clojure.data.json :only [read-str write-str]])
  (:require [clj-http.client :as http]))

(defn read-gbif-0
  ([filters offset limit]
    (let [filters-url (reduce #(str "&" %1 (key %2) "=" (.replace (val %2) " " "+" )) "" filters)
          limit-url  (str "limit=" (if (> limit 300) 300 limit) "&offset=" offset)
          url (str "http://api.gbif.org/v1/occurrence/search?" filters-url "&" limit-url)
          result (read-str (:body (http/get url)) :key-fn keyword)]
      result
      )))

(defn read-gbif
  "Read the GBIF as a whole returning all occurrences as a vector"
  ([opts] 
   (let [filters (dissoc opts :limit :offset)
         limit   (or (:limit opts) 200000)
         offset  (or (:offset opts) 0)]
     (let [r0 (read-gbif-0 filters offset limit)
           relimit  (min limit (:count r0))
           numreq   (int (Math/ceil (/ relimit 300)))
           reoffset (+ offset 300)]
       (if (and (not (:endOfRecords r0)) (< (count (:results r0)) limit))
          (assoc r0 :results 
              (reduce concat (:results r0)
                (pmap
                  (fn [offset] (:results (read-gbif-0 filters offset 300)))
                  (map first (partition-all 300 (range reoffset relimit))))))
         r0
         ))
     ))
  ([filters offset limit acc]
    (let [filters-url (reduce #(str "&" %1 (key %2) "=" (.replace (val %2) " " "+" )) "" filters)
          limit-url  (str "limit=" (if (> limit 300) 300 limit) "&offset=" offset)
          url (str "http://api.gbif.org/v1/occurrence/search?" filters-url "&" limit-url)
          result (read-str (:body (http/get url)) :key-fn keyword)]
      (if (and (not (:endOfRecords result)) (> limit (+ (count acc) (count (:results result)))))
        (recur filters (+ offset 300) limit (concat acc (:results result) ))
        (assoc result :results (concat acc (:results result)))
        )
      )))

