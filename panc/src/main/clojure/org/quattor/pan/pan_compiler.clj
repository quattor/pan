(ns org.quattor.pan.pan-compiler
  (:gen-class)
  (:require [clojure.tools.cli :as cli]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [org.quattor.pan.cmd-option :refer [to-settings]]
            [org.quattor.pan.settings :as settings]
            [clojure.string :as str])
  (:import [org.quattor.pan CompilerOptions CompilerResults]
           [clojure.lang ExceptionInfo]))

(def ^:const bug-report-msg
  "**********
An unexpected exception was thrown in the compiler.
Please file a bug report including the stack trace.
**********")

(defn format-ex-info
  [ex]
  (let [{:keys [type msg]} (ex-data ex)]
    (-> type
        (name)
        (.toUpperCase)
        (str " ERROR: " msg))))

(defn create-compiler-options []
  (let [{:keys [debug-ns-include
                debug-ns-exclude
                max-iteration
                max-recursion
                formatter
                output-dir
                include-path
                warnings
                annotationDirectory
                annotationBaseDirectory
                rootElement
                nthread
                disableEscaping]} settings/*settings*]
    (CompilerOptions. debug-ns-include
                      debug-ns-exclude
                      max-iteration
                      max-recursion
                      formatter
                      output-dir
                      include-path
                      warnings
                      annotationDirectory
                      annotationBaseDirectory
                      rootElement
                      nthread
                      disableEscaping)))

(defn default-compiler-options []
  (let [{:keys [debug-ns-include
                debug-ns-exclude
                max-iteration
                max-recursion
                formatter
                output-dir
                include-path
                warnings
                annotationDirectory
                annotationBaseDirectory
                rootElement
                nthread
                disableEscaping]} (settings/defaults)]
    (CompilerOptions. debug-ns-include
                      debug-ns-exclude
                      max-iteration
                      max-recursion
                      formatter
                      output-dir
                      include-path
                      warnings
                      annotationDirectory
                      annotationBaseDirectory
                      rootElement
                      nthread
                      disableEscaping)))

(defn parse-int
  [^String s]
  (try
    (Integer/parseInt s)
    (catch Exception e
      (throw (ex-info (str "invalid integer: '" s "'") {})))))

(def cli-args
  [[nil "--debug" "enable all debugging" :default false]
   [nil "--debug-ns-include REGEX" "ns regex to include debugging"]
   [nil "--debug-ns-exclude REGEX" "ns regex to exclude debugging"]
   [nil "--initial-data DICT" "set root element (must be a dict)"]
   [nil "--include-path PATH" "template lookup path"]
   [nil "--output-dir DIR" "output directory"]
   [nil "--formats FORMATS" "generated output formats" :default "pan,dep"]
   [nil "--java-opts OPTS" "options for JVM"]
   [nil "--max-iteration LIMIT" "set max. no. of iterations" :default "10000"]
   [nil "--max-recursion LIMIT" "set max. depth of recursion" :default "50"]
   [nil "--nthread NUM" "no. of executor threads (0=no. CPU)" :default "0"]
   [nil "--disable-escaping" "disable path element escaping" :default false]
   [nil "--logging LOG_TYPES" "set logging types"]
   [nil "--log-file FILE" "specify log file"]
   [nil "--warnings FLAG" "off, on, fatal" :default "on"]
   ["-v" "--verbose" "show statistics and progress" :default false]
   ["-h" "--help" "print command help" :default false]])

(defn banner-and-exit [banner]
  (println (str "\npanc [options] [pan source files...]\n\n" banner))
  (System/exit 0))

(defn compiler-error-and-exit [^Throwable t]
  (println bug-report-msg)
  (.printStackTrace t)
  (System/exit 1))

(defn error-and-exit [e]
  (println (format-ex-info e))
  (System/exit 1))

(defn build-profiles [options pan-sources]
  (let [compiler-options (create-compiler-options)]
    (org.quattor.pan.Compiler/run compiler-options nil pan-sources)))

(defn error-message
  [errors]
  (pprint errors)
  (let [msg (str/join \newline errors)]
    (throw (ex-info msg {:type :options :msg msg}))))

(defn run-compiler
  [options arguments]
  (settings/with-settings
    (to-settings options)

    (let [sources (map io/file arguments)
          ^CompilerResults results (build-profiles options sources)
          errors (.formatErrors results)
          rc (if errors 1 0)]
      (if errors
        (println errors))
      (if (:verbose options)
        (println (.formatStats results)))
      (System/exit rc))))

(defn -main [& args]
  (try
    (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-args)]
      (cond
        (:help options) (banner-and-exit summary)
        errors (error-message errors)
        :else (run-compiler options arguments)))
    (catch ExceptionInfo e
      (error-and-exit e))
    (catch Throwable t
      (compiler-error-and-exit t))))
