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
(def area geom/area)
(def utm transform/utmzone)

(defn area-in-meters
  ""
  ([polygon] (area-in-meters polygon "EPSG:4326"))
  ([polygon crs] 
   (area
     (transform/reproject
       polygon crs (utm polygon)))))

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
        (transform/reproject point crs (utm point))
        meters) (utm point) crs)))

(defn union
  ""
  [ features ]
   (reduce analysis/union features))

(defn eoo
  ""
  [ occs ]
   (let [occs (distinct occs)
         points (map #(point (c (:decimalLongitude %) (:decimalLatitude %))) occs) 
         poli (if (>= (count points) 3) 
                (convex-hull points)
                (union (map #(buffer-in-meters % 10000) points)))]
     {:polygon (read-str (io/write-geojson poli))
      :area    (* (area poli) 10000)}
       ))

(defn make-grid
  ""
  [min-lat max-lat min-lng max-lng]
  (let [step 2]
   (doall
    (for [lng (range (- min-lng step) (+ max-lng step) step) 
          lat (range (- min-lat step) (+ max-lat step) step)]
      (int-array
        [lat (+ lat step) lng (+ lng step)])))))

(defn within?
  [cell point]
  (let [lat (first point)
        lng (last point)]
    (and 
      (>= lat (aget cell 0))
      (< lat (aget cell 1))
      (>= lng (aget cell 2))
      (< lng (aget cell 3))
      )))

(defn aoo
  ""
  [ occs ]
   (let [occs   (distinct occs)
         points (map #(vector (int (* (:decimalLatitude %) 100))  (int (* (:decimalLongitude %) 100))) occs)
         grid   (make-grid (apply min (map first points)) (apply max (map first points)) (apply min (map last points)) (apply max (map last points)))
         cells  (transient [])]
     (dorun
       (for [cell grid point points]
         (if (within? cell point)
           (conj! cells cell))))
     (let [result (distinct (persistent! cells))]
       {:area (* 1000 (* 4 (count result)))
        :polygon 
          (read-str
            (io/write-geojson
              (union 
                (mapv
                  #(geom/polygon
                     (geom/linear-ring
                       [ (c (/ (aget % 0) 100) (/ (aget % 2) 100))
                         (c (/ (aget % 0) 100) (/ (aget % 3) 100)) 
                         (c (/ (aget % 1) 100) (/ (aget % 3) 100)) 
                         (c (/ (aget % 1) 100) (/ (aget % 2) 100)) 
                         (c (/ (aget % 0) 100) (/ (aget % 2) 100)) ]
                       ) nil) 
                  result))))
        }
       )))

