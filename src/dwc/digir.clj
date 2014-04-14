(ns dwc.digir
  (:use clojure.java.io)
  (:use clojure.data.xml)
  (:require [clj-http.client :as http]))

(def template (slurp (resource "digir.xml")))
(def afilter  (slurp (resource "digir_filter.xml")))

(defn make-xml 
  "Creates the request XML with proper params"
  ([url] (make-xml url {}))
  ([url opts]
    (let [filters (if (nil? (:filters opts)) {} (:filters opts))
          start   (if (nil? (:start opts)) 0 (:start opts))
          limit   (if (nil? (:limit opts)) 20 (:limit opts))]
      (-> template
        (.replace "{{start}}" (String/valueOf start))
        (.replace "{{limit}}" (String/valueOf limit))
        (.replace "{{url}}" url)
        (.replace "{{filters}}" 
          (if (empty? filters) ""
            (str "<filter>"
              (reduce str 
                (for [kv filters]
                  (-> afilter
                      (.replace "{{field}}" (key kv))
                      (.replace "{{value}}" (val kv)))))
                   "</filter>")
            ))
          ))))

(defn get-content
  ""
  [xml] 
  (first
    (filter  #(= :content (:tag %))
      (:content xml))))

(defn get-diags
  ""
  [xml] 
  (first 
    (filter #(= :diagnostics (:tag %))
      (:content xml))))

(defn get-record
  "Get, parse and map a single record"
  [rec]
  (reduce merge
    (map 
      #(hash-map (:tag %) (first (:content %))) 
        (:content rec))))

(defn get-records 
  ""
  [xml] 
  (map get-record (:content (get-content xml))))

(defn get-summary 
  ""
  [xml req] 
  (let [diags (:content (get-diags xml))]
    {:total (Integer/valueOf (first (:content (first (filter #(= "RECORD_COUNT" (get-in % [:attrs :code])) diags)))))
     :end   (= "true" (first (:content (first (filter #(= "END_OF_RECORDS" (get-in % [:attrs :code])) diags)))))
     :start (Integer/valueOf (second (re-find #"start=\"(\d+)\"" req) ))
     :limit (Integer/valueOf (second (re-find #"limit=\"(\d+)\"" req) ))}))

(defn read-digir 
  ""
  ([url] (read-digir url {}))
  ([url opts]
   (let [req (make-xml url opts)
         res (http/get url {:query-params {"request" req} })
         xml (parse (java.io.StringReader. (:body res )))]
     {:summary (get-summary xml req) :records (get-records xml)}
     )))

