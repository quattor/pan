(ns org.quattor.pan.pan-compiler
  (:gen-class)
  (:require [clojure.tools.cli :refer [cli]]
            [clojure.java.io :as io]
            [org.quattor.pan.cmd-option :refer [to-settings]]
            [org.quattor.pan.settings :as settings])
  (:import [org.quattor.pan CompilerOptions CompilerResults]
           [clojure.lang ExceptionInfo]))

(def ^:const bug-report-msg
  "**********
An unexpected exception was thrown in the compiler.
Please file a bug report with including the stack trace.
**********")

(defn format-ex-info [e]
  (let [info (ex-data e)
        type (.toUpperCase (name (:type info)))
        msg (:msg info)]
    (str type " ERROR: " msg)))

(defn create-compiler-options []
  (let [ {:keys [debug-ns-include
                 debug-ns-exclude 
                 max-iteration 
                 max-recursion 
                 formatter 
                 output-dir
                 sessionDirectory 
                 includeDirectories 
                 warnings 
                 annotationDirectory 
                 annotationBaseDirectory 
                 rootElement] } settings/*settings* ]
   (CompilerOptions. debug-ns-include 
                     debug-ns-exclude 
                     max-iteration 
                     max-recursion 
                     formatter 
                     output-dir 
                     sessionDirectory 
                     includeDirectories 
                     warnings 
                     annotationDirectory
                     annotationBaseDirectory
                     rootElement)))

(defn default-compiler-options []
  (let [ {:keys [debug-ns-include
                 debug-ns-exclude 
                 max-iteration 
                 max-recursion 
                 formatter 
                 output-dir 
                 sessionDirectory 
                 includeDirectories 
                 warnings 
                 annotationDirectory 
                 annotationBaseDirectory 
                 rootElement] } (settings/defaults) ]
   (CompilerOptions. debug-ns-include 
                     debug-ns-exclude 
                     max-iteration 
                     max-recursion 
                     formatter 
                     output-dir 
                     sessionDirectory 
                     includeDirectories 
                     warnings 
                     annotationDirectory
                     annotationBaseDirectory
                     rootElement)))

(defn process-cli-args [args]
  (try
    (cli args
         ["--debug" "enable all debugging" :default false :flag true]
         ["--debug-ns-include" "ns regex to include debugging"]
         ["--debug-ns-exclude" "ns regex to exclude debugging"]
         ["--initial-data" "set root element (must be nlist)"]
         ["--include-path" "template lookup path"]
         ["--output-dir" "output directory" ]
         ["--formats" "generated output formats" :default "pan,dep"]
         ["--java-opts" "options for JVM"]
         ["--max-iteration" "set max. no. of iterations" :default 10000]
         ["--max-recursion" "set max. depth of recursion" :default 50]
         ["--logging" "set logging types"]
         ["--log-file" "specify log file"]
         ["--warnings" "off, on, fatal" :default "on" ]
         ["-v" "--verbose" "show statistics and progress" :default false :flag true]
         ["-h" "--help" "print command help" :default false :flag true])
    (catch Exception e
      (let [msg (.getMessage e)]
        (throw (ex-info msg {:type :options :msg msg}))))))

(defn banner-and-exit [banner]
  (println (str "\npanc [options] [pan source files...]\n\n" banner))
  (System/exit 0))

(defn compiler-error-and-exit [t]
  (println bug-report-msg)
  (.printStackTrace t)
  (System/exit 1))

(defn error-and-exit [e]
  (println (format-ex-info e))
  (System/exit 1))

(defn build-profiles [options pan-sources] 
  (let [compiler-options (create-compiler-options)]
    (org.quattor.pan.Compiler/run compiler-options nil pan-sources)))

(defn -main [& args]
  (try
    (let [[options files banner] (process-cli-args args)]
      (when (:help options)
        (banner-and-exit banner))
      (settings/with-settings (to-settings options)
        (let [sources (map io/file files)
              results (build-profiles options sources)
              errors (.formatErrors results)
              rc (if errors 1 0)]
          (if errors
            (println errors))
          (if (:verbose options)
            (println (.formatStats results)))
          (System/exit rc))))
    (catch ExceptionInfo e
      (error-and-exit e))
    (catch Throwable t
      (compiler-error-and-exit t))))
  