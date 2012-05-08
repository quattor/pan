(ns org.quattor.pan.cmd-option-test
  (:use clojure.test
        org.quattor.pan.cmd-option)
  (:import (org.quattor.pan.output TxtFormatter 
                                   JsonFormatter 
                                   DotFormatter 
                                   PanFormatter 
                                   XmlDBFormatter)))

(deftest test-default
  []
  (let [unknown (process :unknown "ok")]
    (is (= "ok" (:unknown unknown)))
    (is (= 1 (count unknown)))))
    
(deftest test-warnings
  []
  (let [off (process :warnings "off")
        on (process :warnings "on")
        fail (process :warnings "fail")]
    (is (= -1 (:deprecationLevel off)))
    (is (not (:failOnWarn off)))
    (is (= 1 (:deprecationLevel on)))
    (is (not (:failOnWarn on)))
    (is (= 1 (:deprecationLevel fail)))
    (is (:failOnWarn fail)))
  (is (thrown? Exception (process :warnings "unknown"))))
    
(deftest test-formats
  []
  (let [pandep (process :formats "pan,dep")
        text (process :formats "text")
        json (process :formats "json")
        dot (process :formats "dot")
        xmldb (process :formats "xmldb")]
    (is (instance? PanFormatter (:formatter pandep)))
    (is (:depWriteEnabled pandep))
    (is (instance? TxtFormatter (:formatter text)))
    (is (instance? JsonFormatter (:formatter json)))
    (is (instance? DotFormatter (:formatter dot)))
    (is (instance? XmlDBFormatter (:formatter xmldb))))
  (is (thrown? Exception (process :formats "unknown"))))

