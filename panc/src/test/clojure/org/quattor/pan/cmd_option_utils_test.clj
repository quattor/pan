(ns org.quattor.pan.cmd-option-utils-test
  (:use org.quattor.pan.cmd-option-utils
        [clojure.test :only [deftest is are]])
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [java.util.regex Pattern]
           [java.net URL]
           [java.io File]))

(deftest test-to-integer []
  (is (= 0 (to-integer "0")))
  (is (= 1001 (to-integer "1001")))
  (is (= -1 (to-integer "-1")))
  (is (nil? (to-integer "a"))))

(deftest test-positive-integer []
  (is (= 1 (:name (positive-integer :name "1"))))
  (is (= 1000 (:name (positive-integer :name "1000"))))
  (is (thrown? Exception (positive-integer :name "-1")))
  (is (thrown? Exception (positive-integer :name "a"))))

(deftest test-split-on-commas []
  (let [correct ["a" "b" "c"]]
    (is (= [] (split-on-commas nil)))
    (are [x] (= correct (split-on-commas x))
         "a,b,c"
         "a,,b,,c"
         ",a,b,c"
         "a,b,c,"
         "a , b , c"
         " a , b, c , ")))

(deftest test-split-path []
  (let [correct ["a" "b" "c"]]
    (is (= [] (split-path nil)))
    (are [x] (= correct (split-path x))
         (str/join File/pathSeparator ["a" "b" "c"])
         (str/join File/pathSeparator ["a" "" "b" "" "c"])
         (str/join File/pathSeparator ["" "a" "b" "c"])
         (str/join File/pathSeparator ["a" "b" "c" ""]))))

(deftest test-absolute-file []
  (let [user-dir (System/getProperty "user.dir")]
    (is (= (io/file user-dir) (absolute-file)))
    (are [x y] (= x (absolute-file y))
         (io/file user-dir) nil
         (io/file user-dir) ""
         (io/file "/my/file") "/my/file"
         (io/file "/my/file") (URL. "file:///my/file")
         (io/file user-dir "relative/file") "relative/file"
         (io/file user-dir "relative/file") (URL. "file:relative/file"))))

(deftest test-directory? []
  (is (directory? (absolute-file)))
  (is (not (directory? nil)))
  (is (not (directory? (absolute-file "/dummy/file/")))))
