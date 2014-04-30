(ns org.quattor.pan.pan-annotations
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clojure.java.io :as javaio])
  (:import (java.io File)
           (org.quattor.pan CompilerOptions CompilerResults)))

(defn path-splitter-re []
  (re-pattern (str "[^" File/pathSeparator "]+")))

(defn split-path [path]
  (re-seq (path-splitter-re) path))

(defn create-absolute-file [^String name]
  (.getAbsoluteFile (File. name)))

(defn current-directory []
  (create-absolute-file ""))

(defn parse-include-path [path]
  (if-let [dirs (split-path path)]
    (map create-absolute-file dirs)
    (list (create-absolute-file ""))))

(defn parse-directory [dir]
  (if (string/blank? dir)
    (current-directory)
    (create-absolute-file dir)))

(defn get-compiler-options [options]
  (when (> (:verbose options) 0)
    (println (str "base-dir=" (:base-dir options) "\toutput-dir=" (:output-dir options))))
  (let [compiler-options (CompilerOptions/createAnnotationOptions (:output-dir options) (:base-dir options))]
    (when (> (:verbose options) 1)
      (println "compiler-options:\n" compiler-options))
    compiler-options))

(defn generate-annotations [options files]
  (let [compiler-options (get-compiler-options options)
        pan-sources (map #(File. ^String %) files)]
    (org.quattor.pan.Compiler/run compiler-options nil pan-sources)))

(def cli-options
  [;; First three strings describe a short-option, long-option with optional
   ;; example argument description, and a description. All three are optional
   ;; and positional. short and long option must be replaced by nil if absent.
   [nil "--base-dir DIR" "base directory for templates" 
    :default (current-directory)
    :parse-fn parse-directory]
   [nil "--output-dir DIR" "output directory" 
    :default (current-directory)
    :parse-fn parse-directory]
   [nil "--java-opts OPTS" "options for JVM"]
   ["-v" "--verbose" "verbosity level; may be specified multiple times to increase value"
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help" "print command help"
    :default false
    :flag true]])
   
(defn usage [options-summary]
  (->> ["This tool processes annotations in pan templates."
        ""
        "Usage: panc-annotations [options] files"
        ""
        "Options:"
        options-summary
        ""
        "Arguments:"
        "  files    space-separated list of pan template files relative to base directory"
        ""]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \n errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      (< (count args) 1) (exit 1 (usage summary))
      errors (exit 1 (str (error-msg errors) "\n\n" (usage summary))))
    (when (> (:verbose options) 0)
      (println (str "Templates to process: " arguments))) 
    (let [^CompilerResults results (generate-annotations options arguments)]
      (if-let [errors (.formatErrors results)]
        (println errors)
        "")
      (println (.formatStats results)))))

