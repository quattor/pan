(ns org.quattor.pan.pan-compiler
  (:gen-class)
  (:use [clojure.tools.cli :only [cli]]
        [clojure.java.io :only [file]]
        [org.quattor.pan.cmd-option-utils :only [absolute-file]])
  (:require org.quattor.pan.cmd-option)
  (:import [java.io File]
           [org.quattor.pan.output PanFormatter]
           [org.quattor.pan CompilerOptions CompilerResults]))

(def default-compiler-settings
  {:debugIncludePatterns []
   :debugExcludePatterns []
   :xmlWriteEnabled true
   :depWriteEnabled true
   :iterationLimit 10000
   :callDepthLimit 10
   :formatter (PanFormatter/getInstance)
   :outputDirectory (absolute-file)
   :sessionDirectory nil
   :includeDirectories [(absolute-file)]
   :nthread 0
   :gzipOutput false
   :deprecationLevel -1
   :forceBuild false
   :annotationDirectory nil
   :annotationBaseDirectory nil
   :failOnWarn false
   :rootElement nil})

(defn create-compiler-options
  [ {:keys [debugIncludePatterns
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
            deprecationLevel 
            forceBuild 
            annotationDirectory 
            annotationBaseDirectory 
            failOnWarn 
            rootElement] } ]
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
                     deprecationLevel 
                     forceBuild 
                     annotationDirectory
                     annotationBaseDirectory
                     failOnWarn 
                     rootElement))

(defn cli-options-to-settings [cli-options]
  (into {} (mapcat org.quattor.pan.cmd-option/process cli-options)))

(defn get-compiler-options [cli-options]  
  (create-compiler-options 
    (merge default-compiler-settings
           (cli-options-to-settings cli-options))))

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
  (let [compiler-options (get-compiler-options options)]
    (org.quattor.pan.Compiler/run compiler-options nil pan-sources)))

(defn -main [& args]
  (try
    (let [[options files banner] (process-cli-args args)]
      (when (:help options)
        (banner-and-exit))
      (let [settings {}
            sources (map file files)
            results (build-profiles options sources)]
        (if-let [errors (.formatErrors results)]
          (println errors))
        (if (:verbose options)
          (println (.formatStats results)))))
    (catch Exception e
      compiler-error-and-exit)))
  