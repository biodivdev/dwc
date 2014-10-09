(ns dwc.calc-test
  (:use dwc.calc)
  (:use midje.sweet))

(fact "Calculate EOO buffers"
 (let [o0 {:decimalLatitude -15.48333 :decimalLongitude -55.68333}
       o1 {:decimalLatitude -15.402872 :decimalLongitude -55.881867}
       o2 {:decimalLatitude -15.402872 :decimalLongitude -55.881867}
       o3 {:decimalLatitude -15.402872 :decimalLongitude -55.881867}]
   (int (:area (eoo [o0]))) => (roughly 262)
   (int (:area (eoo [o0 o1 o2 o3]))) => (roughly 525)
   ))

(fact "Calculate EOO buffers others"
 (let [o0 {:decimalLatitude 10.10 :decimalLongitude 20.20}
       o1 {:decimalLatitude 14.10 :decimalLongitude 21.21}
       o2 {:decimalLatitude 14.10 :decimalLongitude 21.21}
       o3 {:decimalLatitude -15.15 :decimalLongitude -35.35}]
   (int (:area (eoo [ o3  ]) )) => (roughly 262)
   (:area (eoo [ o0 o1 ]) ) => (roughly 519)
   (eoo [ o0 o1 o2 ]) => (eoo [ o0 o1 ])
   ))

(fact "Calculate EOO convex-hull"
 (let [o0 {:decimalLatitude 10.10 :decimalLongitude 20.20}
       o1 {:decimalLatitude 14.10 :decimalLongitude 21.21}
       o2 {:decimalLatitude 14.12 :decimalLongitude 21.22}]
   (:area (eoo [ o0 o1 o2 ]) ) => (roughly 99)
   ))

(fact "Grids"
  (map #(vector (aget % 0) (aget % 1) (aget % 2) (aget % 3))
    (grid-of-points [[0 0] [4 4]] 2) )
    => (list [-2 0 -2 0] [0 2 -2 0] [2 4 -2 0] [4 6 -2 0] 
             [-2 0 0 2]  [0 2 0 2]  [2 4 0 2]  [4 6 0 2]
             [-2 0 2 4]  [0 2 2 4]  [2 4 2 4]  [4 6 2 4]
             [-2 0 4 6]  [0 2 4 6]  [2 4 4 6]  [4 6 4 6]))

(fact "Within?"
  (within? (int-array [0 2 0 2]) [0 0])
    => true
  (within? (int-array [0 2 0 2]) [1 1])
    => true
  (within? (int-array [0 2 0 2]) [2 2])
    => false
      )

(fact "Filter grid cells"
 (count 
   (filter-cells (grid-of-points [[0 0] [4 4]] 2) [[0 0] [4 4]]))
 => 2
 (count 
   (filter-cells (grid-of-points [[0 0] [4 4]] 10) [[0 0] [4 4]]))
 => 1
      )

(fact "AOO"
 (let [o0 {:decimalLatitude -10.10 :decimalLongitude -20.20}
       o1 {:decimalLatitude -24.12 :decimalLongitude -21.22}
       o2 {:decimalLatitude -24.1200001 :decimalLongitude -21.2200001}]
   (:area (aoo [o0 o1 o2]) ) => 8000
   (:area (aoo [o0]) ) => 4000
   ))

(fact "More AOO"
  (:area (aoo [
    {:decimalLatitude -12.966667  :decimalLongitude -41.333333}
    {:decimalLatitude -12.564514  :decimalLongitude -41.544109}
    {:decimalLatitude -13 :decimalLongitude -41.4}
    {:decimalLatitude -12.45  :decimalLongitude -41.466667}
    {:decimalLatitude -12.45  :decimalLongitude -41.466667}
    {:decimalLatitude -12.47  :decimalLongitude -41.43}
    {:decimalLatitude -12.46  :decimalLongitude -41.46}
    {:decimalLatitude -12 :decimalLongitude -41}
    {:decimalLatitude -12.45  :decimalLongitude -41.466667}
    {:decimalLatitude -12.35681   :decimalLongitude -41.312036}
    {:decimalLatitude -12.35681   :decimalLongitude -41.312036}
    {:decimalLatitude -13.337144  :decimalLongitude -41.43961}
    {:decimalLatitude -12.941367  :decimalLongitude -41.281258}
    {:decimalLatitude -12.941367  :decimalLongitude -41.281258}
    {:decimalLatitude -13.337144  :decimalLongitude -41.43961}
    {:decimalLatitude -12.45  :decimalLongitude -41.466667}
    {:decimalLatitude -12.941367  :decimalLongitude -41.281258}
    {:decimalLatitude -12.466667  :decimalLongitude -41.433333}
    {:decimalLatitude -12.551035  :decimalLongitude -41.572546}
    {:decimalLatitude -12 :decimalLongitude -41}
    {:decimalLatitude -12.35681   :decimalLongitude -41.312036}
    {:decimalLatitude -12 :decimalLongitude -41}
    {:decimalLatitude -13 :decimalLongitude -41}
    {:decimalLatitude -12.35681   :decimalLongitude -41.312036}
    {:decimalLatitude -12 :decimalLongitude -41}
    {:decimalLatitude -12.453889  :decimalLongitude -41.403611}
    {:decimalLatitude -13.337144  :decimalLongitude -41.43961}
    {:decimalLatitude -12.941367  :decimalLongitude -41.281258}
    {:decimalLatitude -12.466667  :decimalLongitude -41.433333}
    {:decimalLatitude -12.447222  :decimalLongitude -41.420556}
   ])) => 56000

      (:area (aoo [
        {:decimalLatitude -20.568944  :decimalLongitude -41.784721}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -20.568944  :decimalLongitude -41.784721}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.531347  :decimalLongitude -44.568832}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.935203  :decimalLongitude -43.471631}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.531347  :decimalLongitude -44.568832}
        {:decimalLatitude -22.531347  :decimalLongitude -44.568832}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -20.568944  :decimalLongitude -41.784721}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.398194  :decimalLongitude -44.634236}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
        {:decimalLatitude -22.531347  :decimalLongitude -44.568832}
        {:decimalLatitude -22.356565  :decimalLongitude -44.660074}
               ])) => 20000

  )

