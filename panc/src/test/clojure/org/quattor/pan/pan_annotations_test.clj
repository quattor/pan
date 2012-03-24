(ns org.quattor.pan.pan-annotations-test
  (:use clojure.test 
        [clojure.string :only (join)]
        org.quattor.pan.pan-annotations)
  (:import java.util.regex.Pattern
           java.io.File
           org.quattor.pan.CompilerOptions))

(deftest splitter-is-pattern
  (is (not (nil? (path-splitter-re))))
  (is (instance? Pattern (path-splitter-re))))

(deftest splitter-matches-correctly
  (let [splitter (path-splitter-re)
        sep File/pathSeparator]
    (is (= "alpha" (re-find splitter "alpha")))
    (is (nil? (re-find splitter sep)))
    (is (= "a" (re-find splitter (str "a" sep "b"))))
    (is (= "b" (re-find splitter (str sep "b"))))))

(deftest split-path-works
  (let [sep File/pathSeparator]
    (is (= ["a" "b" "c"] (split-path (join sep ["a" "b" "c"]))))
    (is (= ["b" "c"] (split-path (join sep ["" "b" "c"]))))
    (is (= ["a" "c"] (split-path (join sep ["a" "" "c"]))))
    (is (= ["a"] (split-path "a")))
    (is (nil? (split-path "")))))

(deftest check-create-absolute-file
  (let [dir (create-absolute-file "alpha")]
    (is (instance? File dir))
    (is (.isAbsolute dir))
    (is (= "alpha" (.getName dir)))))

(deftest check-current-directory 
  (let [dir (current-directory)]
    (is (instance? File dir))
    (is (.isAbsolute dir))
    (is (.isDirectory dir))
    (is (= dir (create-absolute-file "")))))

(deftest check-parse-directory 
  (is (not (nil? (parse-directory nil))))
  (is (not (nil? (parse-directory ""))))
  (is (not (nil? (parse-directory " "))))
  (is (not (nil? (parse-directory "\n"))))
  (is (not (nil? (parse-directory "alpha"))))
  (is (instance? File (parse-directory "alpha")))
  (is (is (= "alpha" (.getName (parse-directory "alpha"))))))

(deftest check-get-compiler-options 
  (let [base-dir (current-directory)
        output-dir (current-directory)
        cli-options {:base-dir base-dir
                     :output-dir output-dir}
        compiler-options (get-compiler-options cli-options)]
    (is (not (nil? compiler-options)))
    (is (instance? CompilerOptions compiler-options))
    (is (= output-dir (.annotationDirectory compiler-options)))
    (is (= base-dir (.annotationBaseDirectory compiler-options)))))
  
(deftest parse-path 
  (let [sep File/pathSeparator]
    (is (= 1 (count (parse-include-path ""))))
    (is (= 2 (count (parse-include-path (str "a" sep "b")))))
    (is (= 2 (count (parse-include-path (str sep "a" sep "b")))))))
