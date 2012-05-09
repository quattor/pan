(ns org.quattor.pan.cmd-option-test
  (:use clojure.test
        [clojure.string :only [join]]
        org.quattor.pan.cmd-option)
  (:import (org.quattor.pan.output TxtFormatter 
                                   JsonFormatter 
                                   DotFormatter 
                                   PanFormatter 
                                   XmlDBFormatter)
           (java.util.regex Pattern)))

(deftest test-to-integer
  []
  (is (= 0 (to-integer "0")))
  (is (= 1001 (to-integer "1001")))
  (is (= -1 (to-integer "-1")))
  (is (nil? (to-integer "a"))))

(deftest test-positive-integer
  []
  (is (= 1 (:name (positive-integer :name "1"))))
  (is (= 1000 (:name (positive-integer :name "1000"))))
  (is (thrown? Exception (positive-integer :name "-1")))
  (is (thrown? Exception (positive-integer :name "a"))))

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

(deftest test-compress
  []
  (is (:gzip-output (process :compress true)))
  (is (not (:gzip-output (process :compress false)))))

(deftest test-debug-exclude-patterns
  []
  (let [patterns [".*OK.*" ".*OK2.*"]
        patterns-as-string (join " " patterns)
        result (process :debug-exclude-patterns patterns-as-string)
        regex (:debug-exclude-patterns result)]
    (is (= 1 (count result)))
    (is (= 2 (count regex)))
    (is (every? (partial instance? Pattern) regex))
    (let [[regex1 regex2] regex]
      (is (re-matches regex1 "...OK..."))
      (is (re-matches regex1 "xxxOKxxx"))
      (is (not (re-matches regex1 "...BAD...")))
      (is (re-matches regex2 "...OK2..."))
      (is (re-matches regex2 "xxxOK2xxx"))
      (is (not (re-matches regex2 "...BAD2..."))))))

(deftest test-debug-include-patterns
  []
  (let [patterns [".*OK.*" ".*OK2.*"]
        patterns-as-string (join " " patterns)
        result (process :debug-include-patterns patterns-as-string)
        regex (:debug-include-patterns result)]
    (is (= 1 (count result)))
    (is (= 2 (count regex)))
    (is (every? (partial instance? Pattern) regex))
    (let [[regex1 regex2] regex]
      (is (re-matches regex1 "...OK..."))
      (is (re-matches regex1 "xxxOKxxx"))
      (is (not (re-matches regex1 "...BAD...")))
      (is (re-matches regex2 "...OK2..."))
      (is (re-matches regex2 "xxxOK2xxx"))
      (is (not (re-matches regex2 "...BAD2..."))))))

(deftest test-iteration-limit
  []
  (is (= 1 (:iteration-limit (process :iteration-limit "1"))))
  (is (= 1000 (:iteration-limit (process :iteration-limit "1000"))))
  (is (thrown? Exception (process :iteration-limit "-1")))
  (is (thrown? Exception (process :iteration-limit "a"))))
  
(deftest test-call-depth-limit
  []
  (is (= 1 (:call-depth-limit (process :call-depth-limit "1"))))
  (is (= 1000 (:call-depth-limit (process :call-depth-limit "1000"))))
  (is (thrown? Exception (process :call-depth-limit "-1")))
  (is (thrown? Exception (process :call-depth-limit "a"))))
  
  