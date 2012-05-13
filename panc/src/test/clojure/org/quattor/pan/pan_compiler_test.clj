(ns org.quattor.pan.pan-compiler-test
  (:use clojure.test
        org.quattor.pan.pan-compiler)
  (:import [org.quattor.pan CompilerOptions]))

(deftest test-cli-options-to-settings
  (is (= {:verbose true :compress true} 
         (cli-options-to-settings {:verbose true :compress true}))))

(deftest test-get-compiler-options
  (is (instance? CompilerOptions (get-compiler-options {}))))

