(ns org.quattor.pan.cmd-option-test
  (:require [clojure.test :refer :all]
            [clojure.string :refer [join]]
            [clojure.set :refer [subset?]]
            [org.quattor.pan.cmd-option :refer :all])
  (:import [org.quattor.pan.output TxtFormatter 
                                   JsonFormatter 
                                   DotFormatter 
                                   PanFormatter 
                                   DepFormatter]
           [org.quattor.pan CompilerOptions$DeprecationWarnings]
           [java.util.regex Pattern]
           [clojure.lang ExceptionInfo]))

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
        dot (process [:formats "dot"])]
    (is (= #{(PanFormatter/getInstance) (DepFormatter/getInstance)} (:formatter pandep)))
    (is (= #{(TxtFormatter/getInstance)} (:formatter text)))
    (is (= #{(JsonFormatter/getInstance)} (:formatter json)))
    (is (= #{(DotFormatter/getInstance)} (:formatter dot))))
  (is (thrown? ExceptionInfo (process [:formats "unknown"]))))

(deftest test-nil-debug-patterns
  (let [result (process [:debug-ns-exclude nil])
        regex (:debug-ns-exclude result)]
    (is (nil? regex)))
  (let [result (process [:debug-ns-include nil])
        regex (:debug-ns-include result)]
    (is (nil? regex))))

(deftest test-debug-exclude-patterns
  (let [pattern ".*OK.*"
        result (process [:debug-ns-exclude pattern])
        regex (:debug-ns-exclude result)]
    (is (= 1 (count result)))
    (is (instance? Pattern regex))
    (is (re-matches regex "...OK..."))
    (is (re-matches regex "xxxOKxxx"))
    (is (not (re-matches regex "...BAD...")))))

(deftest test-debug-include-patterns
  (let [pattern ".*OK.*"
        result (process [:debug-ns-include pattern])
        regex (:debug-ns-include result)]
    (is (= 1 (count result)))
    (is (instance? Pattern regex))
    (is (re-matches regex "...OK..."))
    (is (re-matches regex "xxxOKxxx"))
    (is (not (re-matches regex "...BAD...")))))

(deftest test-max-iteration
  (is (= 1 (:max-iteration (process [:max-iteration "1"]))))
  (is (= 1000 (:max-iteration (process [:max-iteration "1000"]))))
  (is (thrown? ExceptionInfo (process [:max-iteration "-1"])))
  (is (thrown? ExceptionInfo (process [:max-iteration "a"]))))

(deftest test-max-recursion
  (is (= 1 (:max-recursion (process [:max-recursion "1"]))))
  (is (= 1000 (:max-recursion (process [:max-recursion "1000"]))))
  (is (thrown? ExceptionInfo (process [:max-recursion "-1"])))
  (is (thrown? ExceptionInfo (process [:max-recursion "a"]))))

(deftest test-warnings
  (let [off (process [:warnings "off"])
        on (process [:warnings "on"])
        fatal (process [:warnings "fatal"])]
    (is (= CompilerOptions$DeprecationWarnings/OFF (:warnings off)))
    (is (= CompilerOptions$DeprecationWarnings/ON (:warnings on)))
    (is (= CompilerOptions$DeprecationWarnings/FATAL (:warnings fatal))))
  (is (thrown? ExceptionInfo (process [:warnings "unknown"]))))
  
(deftest test-verbose
  (are [x y] (= x (:verbose (process [:verbose y])))
       false false
       true true))
