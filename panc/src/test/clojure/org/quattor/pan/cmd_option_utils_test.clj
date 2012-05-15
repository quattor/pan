(ns org.quattor.pan.cmd-option-utils-test
  (:use org.quattor.pan.cmd-option-utils
        [clojure.test :only [deftest is are]]
        [clojure.java.io :only [file]])
  (:import [java.util.regex Pattern]
           [java.net URL]))

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

(deftest test-absolute-file []
  (let [user-dir (System/getProperty "user.dir")]
    (is (= (file user-dir) (absolute-file)))
    (are [x y] (= x (absolute-file y))
         (file user-dir) nil
         (file user-dir) ""
         (file "/my/file") "/my/file"
         (file "/my/file") (URL. "file:///my/file")
         (file user-dir "relative/file") "relative/file"
         (file user-dir "relative/file") (URL. "file:relative/file"))))

(deftest test-directory? []
  (is (directory? (absolute-file)))
  (is (not (directory? nil)))
  (is (not (directory? (absolute-file "/dummy/file/")))))

(deftest test-pattern-list []
  ;; identical patterns are not equal to each other so
  ;; only count the return values, don't compare regexes
  (is (= [] (pattern-list)))
  (are [x y] (= x (count (pattern-list y)))
       0 nil
       1 "[abc]*,"
       1 "[abc]*,"
       1 " [abc]*,"
       1 "[abc]* ,"
       1 "[abc]* ,, "
       2 "[abc]* ,, [def]+"))
