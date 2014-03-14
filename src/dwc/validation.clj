(ns dwc.validation
  (:require [closchema.core :as schema])
  (:use [clojure.data.json :only [read-str write-str]] ))

(comment "http://rs.tdwg.org/dwc/terms/")

(def dwc-schema
  (read-str (slurp "resources/schema.json") :key-fn keyword))

(defn validate
  "Validate an occurrence entry"
  [occ] 
   (if-not (schema/validate dwc-schema occ)
     (schema/report-errors (schema/validate dwc occ)) 
      true))

