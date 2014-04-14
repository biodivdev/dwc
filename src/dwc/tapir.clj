(ns dwc.tapir
  (:use clojure.java.io)
  (:use clojure.data.xml)
  (:require [clj-http.client :as http]))

(def template (slurp (resource "tapir.xml")))
(def aterm    (slurp (resource "tapir_term.xml")))
(def anode    (slurp (resource "tapir_node.xml")))
(def afilter  (slurp (resource "tapir_filter.xml")))

(defn make-concepts
  "Make maps for the concepts from the xmls, interal."
  [url]
  (reduce merge 
    (map #(hash-map (first (:content (first (:content %))))
                    (:id (:attrs %)))
         (:content (first
             (filter #(= (:tag %) :concepts) 
                (:content (first (:content 
                  (parse (java.io.StringReader. (slurp url))))))))))))

(def concepts 
  (reduce merge 
    [(make-concepts (resource "tapir_terms1.xml"))
     (make-concepts (resource "tapir_terms2.xml"))
     (make-concepts (resource "tapir_terms3.xml"))
     (make-concepts (resource "tapir_terms4.xml"))]))

(defn turn-fields
  "Turn simple fields into concepts mapping"
  [fields]
  (if (or (nil? fields) (empty? fields)) concepts
    (reduce merge
      (map #(hash-map % (concepts %)) 
        fields))))

(defn turn-filters
  "Turn simple filters into concepts filters"
  [filters]
  (if (or (nil? filters) (empty? filters)) {}
    (reduce merge
      (map #(hash-map (concepts (key %)) (val %)) 
        filters))))

(defn make-xml 
  "Creates the request XML with proper params"
  ([] (make-xml {}))
  ([opts]
    (let [fields  (turn-fields (:fields opts))
          filters (turn-filters (:filters opts))
          start   (if (nil? (:start opts)) 0 (:start opts))
          limit   (if (nil? (:limit opts)) 20 (:limit opts))]
      (-> template
        (.replace "{{start}}" (String/valueOf start))
        (.replace "{{limit}}" (String/valueOf limit))
        (.replace "{{terms}}" 
          (reduce str 
            (for [field fields]
              (-> aterm
                (.replace "{{alias}}" (key field))
                  ))))
        (.replace "{{nodes}}" 
          (reduce str 
            (for [field fields]
              (-> anode
                (.replace "{{concept}}" (val field))
                (.replace "{{alias}}" (key field))
              ))))
        (.replace "{{filters}}" 
          (if (empty? filters) ""
            (str (if (> (count filters) 1) "<and>")
              (reduce str 
                (for [kv filters]
                  (-> afilter
                      (.replace "{{concept}}" (key kv))
                    (.replace "{{value}}" (val kv)))))
               (if (> (count filters) 1) "</and>")
               )
            ))
          ))))

(defn get-search
  "Extract the search element of the response xml"
  [xml]
  (:content
    (first
      (filter #(= :search (:tag %))
        (:content xml)))))

(defn get-summary
  "Get the summary (page...) from the response xml"
  [xml]
  (let [summary (:attrs
                  (first 
                    (filter #(= :summary (:tag %))
                      (get-search xml))))]
    (-> summary 
        (assoc :total (Integer/valueOf (or (:totalMatched summary) 0)))
        (assoc :start (Integer/valueOf (or (:start summary) 0)))
        (assoc :next (Integer/valueOf (or (:next summary) 0)))
        (dissoc :totalMatched)
        (dissoc :totalReturned)
        (assoc :end (not (> (Integer/valueOf (or (:next summary) 0)) 0)))
        )
    ))

(defn get-record
  "Get, parse and map a single record"
  [el]
   (let [pairs (map #(hash-map (:tag %) 
                              (first (:content %))) el)]
     (apply merge pairs)))

(defn get-records
  "Extract all records found on response xml"
  [xml]
  (map  get-record
    (map :content
      (filter #(= :record (:tag %))
       (:content
         (first
           (filter #(= :records (:tag %))
            (get-search xml))))))))

(defn read-tapir
  "Read the summary and records from a URL tapir"
  ([url] (read-tapir url {}))
  ([url opts]
   (let [res (http/post url {:body (make-xml opts) :headers {"Content-Type" "application/xml"}})
         xml (parse (java.io.StringReader. (:body res)))]
     (if (not (empty? (filter #(= :error (:tag %)) (:content xml ))))
       {:errors (map #(:content (first (:content %))) (filter #(= :diagnostics (:tag %)) (:content xml)))}
       {:summary (get-summary xml) :records (get-records xml)})
     )))

