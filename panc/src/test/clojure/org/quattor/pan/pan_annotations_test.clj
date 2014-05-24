(ns org.quattor.pan.pan-annotations-test
  (:require [clojure.test :refer :all]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [org.quattor.pan.pan-annotations :refer :all])
  (:import java.util.regex.Pattern
           java.io.File
           org.quattor.pan.CompilerOptions))

(deftest check-create-absolute-file
  (let [dir (create-absolute-file "alpha")]
    (is (instance? File dir))
    (is (.isAbsolute dir))
    (is (= "alpha" (.getName dir)))))

(deftest check-create-absolute-file2
  (let [dir (create-absolute-file "alpha/beta")]
    (is (instance? File dir))
    (is (.isAbsolute dir))
    (is (= "beta" (.getName dir)))))

(deftest check-create-absolute-file3
  (let [dir (create-absolute-file "/absolute/alpha")]
    (is (instance? File dir))
    (is (.isAbsolute dir))
    (is (= "/absolute" (.getParent dir)))
    (is (= "alpha" (.getName dir)))))

(deftest check-create-absolute-file4
  (let [dir (create-absolute-file ".")]
    (is (instance? File dir))
    (is (.isAbsolute dir))
    (is (= "." (.getName dir)))))

(deftest check-create-absolute-file5
  (let [dir (create-absolute-file "")]
    (is (instance? File dir))
    (is (.isAbsolute dir))
    (is (not= "" (.getName dir)))))

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
  (let [{:keys [options arguments errors summary]} (parse-opts []  cli-options)
        compiler-options (get-compiler-options options)]
    (is (not (nil? compiler-options)))
    (is (instance? CompilerOptions compiler-options))))

(deftest check-get-compiler-options2
  (let [base-dir (current-directory)
        output-dir (current-directory)
        ;;{:keys [options arguments errors summary]} (parse-opts ["--base-dir" base-dir "--output-dir" output-dir "-vv"] cli-options)
        {:keys [options arguments errors summary]} (parse-opts ["--base-dir" base-dir "--output-dir" output-dir] cli-options)
        compiler-options (get-compiler-options options)]
    (is (not (nil? compiler-options)))
    (is (instance? CompilerOptions compiler-options))
    (is (= output-dir (.annotationDirectory compiler-options)))
    (is (= base-dir (.annotationBaseDirectory compiler-options)))))
