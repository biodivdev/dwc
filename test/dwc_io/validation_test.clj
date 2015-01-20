(ns dwc-io.validation-test
  (:use midje.sweet)
  (:use dwc-io.validation))

(fact "Must have an occurrenceID"
  (validate {})
    => (list {:error :required :path ["occurrenceID"] :ref "occurrenceID"} ))

(fact "It denies extra properties"
  (map :error
    (validate {:occurrenceID "123" :foo "bar"}))
    => (list :additional-properties-not-allowed))

