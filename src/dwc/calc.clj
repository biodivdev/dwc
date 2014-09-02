(ns dwc.calc
  (:use [clojure.data.json :only [read-str write-str]])
  (:require [cljts.transform :as transform])
  (:require [cljts.analysis :as analysis])
  (:require [cljts.relation :as relation])
  (:require [cljts.geom :as geom])
  (:require [cljts.io :as io])
  (:import [com.vividsolutions.jts.geom
            GeometryFactory
            PrecisionModel
            PrecisionModel$Type
            Coordinate
            LinearRing
            Point
            Polygon
            Geometry]))

(def point geom/point)
(def c geom/c)

(defn area-in-meters
  ""
  ([polygon] (area-in-meters polygon "EPSG:4326"))
  ([polygon crs] 
   (geom/area
     (transform/reproject
       polygon crs "EPSG:23032"))))

(defn convex-hull
  ""
  [points] 
   (analysis/convex-hull 
     (reduce analysis/union points)))

(defn buffer-in-meters
  ""
  ([point meters] (buffer-in-meters point meters "EPSG:4326"))
  ([point meters crs]
    (transform/reproject 
      (analysis/buffer
        (transform/reproject point crs "EPSG:23032")
        meters) "EPSG:23032" crs)))

(defn union
  ""
  [ features ]
   (reduce analysis/union features))

(defn eoo
  ""
  [ occs ]
   (let [occs (distinct occs)
         points (map #(point (c (:decimalLatitude %) (:decimalLongitude %))) occs) ]
     (if (> (count points) 3 )
       (area-in-meters (convex-hull points))
       (area-in-meters (union (map #(buffer-in-meters % 10000) points)))
       )))

(def step 2)

(def grid
 (doall
  (for [lng (range -7800 -1800 step) lat (range -3400 500 step)]
    [lat (+ lat step) lng (+ lng step)]
  )))

(defn within?
  [cell point]
  (let [lat (first point)
        lng (last point)]
    (and 
      (>= lat (get cell 0))
      (< lat (get cell 1))
      (>= lng (get cell 2))
      (< lng (get cell 3))
      )))

(defn aoo
  ""
  [ occs ]
   (let [occs (distinct occs)
         points (map #(vector (int (* (:decimalLatitude %) 100))  (int (* (:decimalLongitude %) 100))) occs)
         cells (transient [])]
     (dorun
       (for [cell grid point points]
         (if (within? cell point)
           (conj! cells cell))))
     (* 4 (count (distinct (persistent! cells))))))

