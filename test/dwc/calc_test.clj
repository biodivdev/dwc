(ns dwc.calc-test
  (:use dwc.calc)
  (:use midje.sweet))

(fact "Calculate EOO buffers"
 (let [o0 {:decimalLatitude 10.10 :decimalLongitude 20.20}
       o1 {:decimalLatitude 14.10 :decimalLongitude 21.21}
       o2 {:decimalLatitude 14.10 :decimalLongitude 21.21}
       o3 {:decimalLatitude -15.15 :decimalLongitude -35.35}]
   (int (eoo [ o3  ])) => (roughly 299832)
   (eoo [ o0 o1 ]) => (roughly 624280)
   (eoo [ o0 o1 o2 ]) => (eoo [ o0 o1 ])
   ))

(fact "Calculate EOO convex-hull"
 (let [o0 {:decimalLatitude 10.10 :decimalLongitude 20.20}
       o1 {:decimalLatitude 14.10 :decimalLongitude 21.21}
       o2 {:decimalLatitude 14.12 :decimalLongitude 21.22}]
   (eoo [ o0 o1 o2 ]) => (roughly 6.7432E5)
   ))

(fact "AOO"
 (let [o0 {:decimalLatitude -10.10 :decimalLongitude -20.20}
       o1 {:decimalLatitude -24.12 :decimalLongitude -21.22}
       o2 {:decimalLatitude -24.1200001 :decimalLongitude -21.2200001}]
   (aoo [o0 o1 o2]) => 8000
   (aoo [o0]) => 4000
   ))
