(ns dwc-io.gbif
  (:use [clojure.data.json :only [read-str write-str]])
  (:require [clj-http.client :as http]))

(defn read-gbif
  "Read the GBIF as a whole returning all occurrences as a vector"
  [filters]
  (let [url (str "http://api.gbif.org/v0.9/occurrence/search?"
             (reduce #(str "&" %1 (key %2) "=" (.replace (val %2) " " "+" )) "" filters))]
   (read-str 
     (:body (http/get url))
     :key-fn keyword)))

