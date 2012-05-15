(ns org.quattor.pan.cmd-option-test
  (:use clojure.test
        [clojure.string :only (join)]
        [clojure.set :only (subset?)]
        org.quattor.pan.cmd-option)
  (:import (org.quattor.pan.output TxtFormatter 
                                   JsonFormatter 
                                   DotFormatter 
                                   PanFormatter 
                                   XmlDBFormatter)
           (java.util.regex Pattern)))

(deftest test-to-settings
  (is (subset? (set (seq {:verbose true :compress true}))
               (set (seq (to-settings {:verbose true :compress true}))))))

(deftest test-default
  (let [unknown (process [:unknown "ok"])]
    (is (= "ok" (:unknown unknown)))
    (is (= 1 (count unknown)))))
    
(deftest test-formats
  (let [pandep (process [:formats "pan,dep"])
        text (process [:formats "text"])
        json (process [:formats "json"])
        dot (process [:formats "dot"])
        xmldb (process [:formats "xmldb"])]
    (is (instance? PanFormatter (:formatter pandep)))
    (is (:depWriteEnabled pandep))
    (is (instance? TxtFormatter (:formatter text)))
    (is (instance? JsonFormatter (:formatter json)))
    (is (instance? DotFormatter (:formatter dot)))
    (is (instance? XmlDBFormatter (:formatter xmldb))))
  (is (thrown? Exception (process :formats "unknown"))))

(deftest test-debug-exclude-patterns
  (let [patterns [".*OK.*" ".*OK2.*"]
        patterns-as-string (join " " patterns)
        result (process [:debug-exclude-patterns patterns-as-string])
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
  (let [patterns [".*OK.*" ".*OK2.*"]
        patterns-as-string (join " " patterns)
        result (process [:debug-include-patterns patterns-as-string])
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

(deftest test-max-iteration
  (is (= 1 (:max-iteration (process [:max-iteration "1"]))))
  (is (= 1000 (:max-iteration (process [:max-iteration "1000"]))))
  (is (thrown? Exception (process [:max-iteration "-1"])))
  (is (thrown? Exception (process [:max-iteration "a"]))))

(deftest test-max-recursion
  (is (= 1 (:max-recursion (process [:max-recursion "1"]))))
  (is (= 1000 (:max-recursion (process [:max-recursion "1000"]))))
  (is (thrown? Exception (process [:max-recursion "-1"])))
  (is (thrown? Exception (process [:max-recursion "a"]))))

(deftest test-warnings
  (let [off (process [:warnings "off"])
        on (process [:warnings "on"])
        fail (process [:warnings "fail"])]
    (is (= -1 (:deprecationLevel off)))
    (is (not (:failOnWarn off)))
    (is (= 1 (:deprecationLevel on)))
    (is (not (:failOnWarn on)))
    (is (= 1 (:deprecationLevel fail)))
    (is (:failOnWarn fail)))
  (is (thrown? Exception (process [:warnings "unknown"]))))
  
(deftest test-verbose
  (are [x y] (= x (:verbose (process [:verbose y])))
       false false
       true true))
