(ns org.quattor.pan.pan-compiler-test
  (:use clojure.test
        org.quattor.pan.pan-compiler)
  (:import [org.quattor.pan CompilerOptions]))

(deftest test-create-compiler-options
  (is (instance? CompilerOptions (create-compiler-options))))

