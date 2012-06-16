(ns org.quattor.pan.pan-annotations
  (:gen-class)
  (:use clojure.tools.cli
        [clojure.string :only (blank?)])
  (:import (java.io File)
           (org.quattor.pan CompilerOptions CompilerResults)))

(defn path-splitter-re []
  (re-pattern (str "[^" File/pathSeparator "]+")))

(defn split-path [path]
  (re-seq (path-splitter-re) path))

(defn create-absolute-file [name]
  (.getAbsoluteFile (File. name)))

(defn current-directory []
  (create-absolute-file ""))

(defn parse-include-path [path]
  (if-let [dirs (split-path path)]
    (map create-absolute-file dirs)
    (list (create-absolute-file ""))))

(defn parse-directory [dir]
  (if (blank? dir)
    (current-directory)
    (create-absolute-file dir)))

(defn get-compiler-options [{base-dir :base-dir output-dir :output-dir}]
    (CompilerOptions/createAnnotationOptions output-dir base-dir))

(defn generate-annotations [options files]
  (let [compiler-options (get-compiler-options options)
        pan-sources (map #(File. %) files)]
    (org.quattor.pan.Compiler/run compiler-options nil pan-sources)))

(defn -main [& args]
  (let [[options files banner]
        (cli args
             ["--base-dir" "base directory for templates" 
              :default (current-directory) :parse-fn parse-directory]
             ["--output-dir" "output directory" 
              :default (current-directory) :parse-fn parse-directory]
             ["--java-opts" "options for JVM"]
             ["-v" "--verbose" "show statistics and progress" :default false :flag true]
             ["-h" "--help" "print command help" :default false :flag true])]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (let [results (generate-annotations options files)]
      (if-let [errors (.formatErrors results)]
        (println errors)
        "")
      (println (.formatStats results)))))
