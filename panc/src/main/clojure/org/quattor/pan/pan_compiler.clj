(ns org.quattor.pan.pan-compiler
  (:gen-class)
  (:use clojure.tools.cli)
  (:import (java.io File)
           (org.quattor.pan.output PanFormatter)
           (org.quattor.pan CompilerOptions CompilerResults)))

(defn create-absolute-file [name]
  (.getAbsoluteFile (File. name)))

(defn current-directory []
  (create-absolute-file ""))

(defn get-compiler-options [options]
  (let [debugIncludePatterns []
        debugExcludePatterns []
        xmlWriteEnabled true
        depWriteEnabled true
        iterationLimit 10000
        callDepthLimit 10
        formatter (PanFormatter/getInstance)
        outputDirectory (current-directory)
        sessionDirectory nil
        includeDirectories [(current-directory)]
        nthread 0
        gzipOutput false
        deprecationLevel -1
        forceBuild false
        annotationDirectory nil
        annotationBaseDirectory nil
        failOnWarn false
        rootElement nil]
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
                      rootElement)))

(defn build-profiles [options files] 
  (let [compiler-options (get-compiler-options options)
        pan-sources (map #(File. %) files)]
    (org.quattor.pan.Compiler/run compiler-options nil pan-sources)))


(defn -main [& args]
  (try
    (let [[options files banner]
          (cli args
               ["--debug" "enable all debugging" :default false :flag true]
               ["--debug-ns-include" "ns regex for debugging"]
               ["--debug-ns-exclude" "ns regex to exclude debugging"]
               ["--initial-data" "set root element (must be nlist)"]
               ["--include-path" "template lookup path"]
               ["--session-dir" "session directory"]
               ["--output-dir" "output directory" ]
               ["--formats" "generated output formats" :default "pan,dep"]
               ["--compress" "compress output" :default false :flag true]
               ["--java-opts" "options for JVM"]
               ["--max-iteration" "set max. no. of iterations" :default 10000]
               ["--max-recursion" "set max. depth of recursion" :default 10]
               ["--logging" "set logging types"]
               ["--log-file" "specify log file"]
               ["--warnings" "off, on, fatal" :default "on" ]
               ["-v" "--verbose" "show statistics and progress" :default false :flag true]
               ["-h" "--help" "print command help" :default false :flag true]
               )]
      (when (:help options)
        (println banner)
        (System/exit 0))
      (let [results (build-profiles options files)]
        (if-let [errors (.formatErrors results)]
          (println errors))
        (println (.formatStats results))))
    (catch Exception e
      (println (.getMessage e))
      (System/exit 1))))
  