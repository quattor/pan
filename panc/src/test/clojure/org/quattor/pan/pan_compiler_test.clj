(ns org.quattor.pan.pan-compiler-test
  (:require [clojure.test :refer :all]
            [org.quattor.pan.pan-compiler :refer :all])
  (:import [org.quattor.pan CompilerOptions]))

(deftest test-create-compiler-options
  (is (instance? CompilerOptions (create-compiler-options))))

