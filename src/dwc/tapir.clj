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

(def concepts-0
  (make-concepts (resource "tapir_terms1.xml")))

(def concepts 
  (reduce merge 
    [(make-concepts (resource "tapir_terms1.xml"))
     (make-concepts (resource "tapir_terms2.xml"))
     (make-concepts (resource "tapir_terms3.xml"))
     (make-concepts (resource "tapir_terms4.xml"))]))

(defn get-capabilities
  "Read the capabilities(specially the fields)"
  [url] 
   (let [res (http/get (str url "?op=c&xslt=http://tapirlink.jbrj.gov.br/skins/darwin/capabilities.xsl"))
          xml (parse (java.io.StringReader. (:body res)))]
     {:fields
       (reduce merge
         (map (fn [concept] 
                (let [concept-name (.substring concept (+ (.lastIndexOf concept "/") 1))]
                 (hash-map concept-name concept)))
           (map :id (map :attrs
             (filter #(= :mappedConcept (:tag %))
               (:content (first
                 (filter #(= :schema (:tag %))
                   (:content (first
                     (filter #(= :concepts (:tag %))
                       (:content (first
                         (filter #(= :capabilities (:tag %))
                            (:content xml))))))))))))))) 
      }))


(def default-fields
     (vec (filter #(and (not (nil? %)) (not (empty? %)) (not (.startsWith % "#")) )
       (clojure.string/split (slurp (clojure.java.io/resource "tapir_terms_default.txt")) #"\n"))))

(defn turn-fields
  "Turn simple fields into concepts mapping"
  [url fields]
  (if (or (nil? fields) (empty? fields)) 
   (turn-fields url default-fields)
    (reduce merge
      (map #(hash-map % (concepts %)) 
        fields))))

(defn turn-filters
  "Turn simple filters into concepts filters"
  [url filters]
  (if (or (nil? filters) (empty? filters)) {}
    (reduce merge
      (map #(hash-map (concepts (key %)) (val %)) 
        filters))))

(defn make-xml 
  "Creates the request XML with proper params"
  ([url] (make-xml url {}))
  ([url opts]
    (let [fields  (turn-fields url (:fields opts))
          filters (turn-filters url (:filters opts))
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

(defn lower-first
  [tag]
   (keyword
   (clojure.string/replace
     (name tag)
     #"^([A-Z])(.*)$"
    (fn [args] 
      (str (.toLowerCase (second args)) (last args))))))

(defn get-record
  "Get, parse and map a single record"
  [el]
   (let [pairs (map #(hash-map (lower-first (:tag %))
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
   (let [req (make-xml url opts)
         res (http/post url {:body req :headers {"Content-Type" "application/xml"}})
         xml (parse (java.io.StringReader. (:body res)))]
     (if (not (empty? (filter #(= :error (:tag %)) (:content xml))))
       {:errors (map #(:content (first (:content %))) (filter #(= :diagnostics (:tag %)) (:content xml)))}
       {:summary (get-summary xml) :records (get-records xml)})
     )))

