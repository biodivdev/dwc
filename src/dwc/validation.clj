(ns dwc.validation
  (:require [closchema.core :as schema])
  (:use [clojure.data.json :only [read-str write-str]] ))

(comment "http://rs.tdwg.org/dwc/terms/")

(def dwc-schema
  (read-str (slurp (clojure.java.io/resource "schema.json") ) :key-fn keyword))

(def all-fields 
  (->
    (map second (re-seq #"\"([a-zA-Z]+)\"\s?:" (slurp (clojure.java.io/resource "schema.json"))))
      rest rest rest rest))

(defn validate
  "Validate an occurrence entry"
  [occ] 
   (if-not (schema/validate dwc-schema occ)
     (schema/report-errors (schema/validate dwc-schema occ)) 
      true))

