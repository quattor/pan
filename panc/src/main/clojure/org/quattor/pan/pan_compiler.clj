(ns org.quattor.pan.pan-compiler
  (:gen-class)
  (:use [clojure.tools.cli :only [cli]]
        [clojure.java.io :only [file]]
        [org.quattor.pan.cmd-option :only (to-settings)])
  (:require [org.quattor.pan.settings :as settings])
  (:import [org.quattor.pan CompilerOptions CompilerResults]))

(defn create-compiler-options []
  (let [ {:keys [debugIncludePatterns
                 debugExcludePatterns 
                 xmlWriteEnabled 
                 depWriteEnabled 
                 iterationLimit 
                 callDepthLimit 
                 formatter 
                 outputDirectory 
                 sessionDirectory 
                 includeDirectories 
                 nthread 
                 gzipOutput 
                 warnings 
                 forceBuild 
                 annotationDirectory 
                 annotationBaseDirectory 
                 rootElement] } settings/*settings* ]
   (CompilerOptions. debugIncludePatterns 
                     debugExcludePatterns 
                     xmlWriteEnabled 
                     depWriteEnabled 
                     iterationLimit 
                     callDepthLimit 
                     formatter 
                     outputDirectory 
                     sessionDirectory 
                     includeDirectories 
                     nthread 
                     gzipOutput
                     warnings 
                     forceBuild 
                     annotationDirectory
                     annotationBaseDirectory
                     rootElement)))

(defn default-compiler-options []
  (let [ {:keys [debugIncludePatterns
                 debugExcludePatterns 
                 xmlWriteEnabled 
                 depWriteEnabled 
                 iterationLimit 
                 callDepthLimit 
                 formatter 
                 outputDirectory 
                 sessionDirectory 
                 includeDirectories 
                 nthread 
                 gzipOutput 
                 warnings 
                 forceBuild 
                 annotationDirectory 
                 annotationBaseDirectory 
                 rootElement] } (settings/defaults) ]
   (CompilerOptions. debugIncludePatterns 
                     debugExcludePatterns 
                     xmlWriteEnabled 
                     depWriteEnabled 
                     iterationLimit 
                     callDepthLimit 
                     formatter 
                     outputDirectory 
                     sessionDirectory 
                     includeDirectories 
                     nthread 
                     gzipOutput
                     warnings 
                     forceBuild 
                     annotationDirectory
                     annotationBaseDirectory
                     rootElement)))

(defn process-cli-args [args]
  (cli args
       ["--debug" "enable all debugging" :default false :flag true]
       ["--debug-ns-include" "ns regex for debugging"]
       ["--debug-ns-exclude" "ns regex to exclude debugging"]
       ["--initial-data" "set root element (must be nlist)"]
       ["--include-path" "template lookup path"]
       ["--session-dir" "session directory"]
       ["--output-dir" "output directory" ]
       ["--formats" "generated output formats" :default "pan,dep"]
       ["--java-opts" "options for JVM"]
       ["--max-iteration" "set max. no. of iterations" :default 10000]
       ["--max-recursion" "set max. depth of recursion" :default 10]
       ["--logging" "set logging types"]
       ["--log-file" "specify log file"]
       ["--warnings" "off, on, fatal" :default "on" ]
       ["-v" "--verbose" "show statistics and progress" :default false :flag true]
       ["-h" "--help" "print command help" :default false :flag true]))

(defn banner-and-exit [banner]
  (println (str "\npanc-new [options] [pan source files...]\n\n" banner))
  (System/exit 0))

(defn compiler-error-and-exit [e]
  (println (.getMessage e))
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
        (let [sources (map file files)
              results (build-profiles options sources)
              errors (.formatErrors results)
              rc (if errors 1 0)]
          (if errors
            (println errors))
          (if (:verbose options)
            (println (.formatStats results)))
          (System/exit rc))))
    (catch Exception e
      (compiler-error-and-exit e))))
  