(ns org.quattor.pan.pan-annotations
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str])
  (:import (java.io File)
           (org.quattor.pan CompilerOptions CompilerResults)))

(def ^:const usage-fmt "This command processes annotations in pan templates.

Usage: panc-annotations [options] files

  Options:
  %s

  Arguments:
    files    space-separated list of pan template files relative to base directory
")

(defn create-absolute-file
  "Creates an absolute file from the given file name.  If
   the filename is nil, then an absolute File to the
   current working directory is returned."
  [^String name]
  (-> (or name "")
      (File.)
      (.getAbsoluteFile)))

(defn current-directory
  []
  (create-absolute-file nil))

(defn parse-directory
  [dir]
  (if (str/blank? dir)
    (current-directory)
    (create-absolute-file dir)))

(defn get-compiler-options
  [{:keys [base-dir output-dir verbose]}]

  (let [compiler-options (CompilerOptions/createAnnotationOptions output-dir base-dir)]
    (when (pos? verbose)
      (println (format "base-dir=%s\toutput-dir=%s" base-dir output-dir)))
    (when (> verbose 1)
      (println "compiler-options:\n" compiler-options))
    compiler-options))

(defn generate-annotations
  [options files]
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
    ["-q" "--quiet" "do not print statistics"
     :default false
     :flag true]
    ["-v" "--verbose" "verbosity level; may be specified multiple times to increase value"
     :default 0
     :assoc-fn (fn [m k _] (update-in m [k] inc))]
    [nil "--version" "show pan compiler version" 
     :default false
     :flag true]
    ["-h" "--help" "print help message"
     :default false
     :flag true]])

(defn usage
  [options-summary]
  (format usage-fmt options-summary))

(defn compiler-version []
  (println "pan compiler version:" (org.quattor.pan.Compiler/version))
  (System/exit 0))

(defn error-msg
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \n errors)))

(defn exit
  [status msg]
  (println msg)
  (System/exit status))

(defn -main
  [& args]

  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (let [{:keys [help verbose version quiet]} options]
      (cond
        help (exit 0 (usage summary))
        version (compiler-version)
        (zero? (count args)) (exit 1 (usage summary))
        errors (exit 1 (str (error-msg errors) "\n\n" (usage summary))))
      (when (pos? verbose)
        (println "Templates to process: " arguments))
      (let [^CompilerResults results (generate-annotations options arguments)]
        (if-let [errors (.formatErrors results)]
          (println errors))
        (if-not quiet
          (println (.formatStats results)))))))
