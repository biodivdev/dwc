(ns dwc-io.gbif
  (:use [clojure.data.json :only [read-str write-str]])
  (:require [clj-http.client :as http]))

(defn read-gbif
  "Read the GBIF as a whole returning all occurrences as a vector"
  ([filters] (read-gbif (dissoc filters :limit :offset) (or (:offset filters) 0) (or (:limit filters) 200000) []))
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

