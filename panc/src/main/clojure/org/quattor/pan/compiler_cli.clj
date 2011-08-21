(ns org.quattor.pan.compiler-cli
  (:gen-class)
  (:use [clojure.tools.cli]))

(defn- as-absolute-directory [path]
  (.getAbsoluteFile (new java.io.File path)))

(defn- get-cwd [] 
  (as-absolute-directory (System/getProperty "user.dir")))

(defn- parse-comma-separated-values [values]
  (if (= values nil)
    []
    (vec (.split values ","))))

(defn- as-absolute-directories [paths]
  (map as-absolute-directory (parse-comma-separated-values paths)))

(defn- get-formatter [name] 
  (org.quattor.pan.output.FormatterUtils/getFormatterInstance name))

(defn- parse-options [& args]
  (cli args
       (optional ["-d" "--debug" "enable all pan debug/traceback functions" :default false])
       (optional ["--debug-include" "regex to enable debug/traceback functions"] #(parse-comma-separated-values %))
       (optional ["--debug-exclude" "regex to disable debug/traceback functions"] #(parse-comma-separated-values %))
       (optional ["--dump-annotations" "print annotations to standard output" :default false])
       (optional ["--annotation-dir" "directory to store the annotation files" :default (get-cwd)])
       (optional ["--annotation-base-dir" "base directory to store the annotation files" :default (get-cwd)])
       (optional ["-a" "--verbose" "show compilation statistics" :default false])
       (optional ["-z" "--xml-write" "write machine config. files (usually XML files)" :default true])
       (optional ["-j" "--objects" "objects to process (comma separated list)"])
       (optional ["-J" "--objects-file" "file containing objects to process"])
       (optional ["-f" "--file" "file containing paths of files to process"])
       (optional ["-c" "--check" "only check the syntax of the given source files" :default false])
       (optional ["-S" "--session-dir" "which session directory to use"])
       (optional ["-I" "--include-dir" "which source directory to consider"] #(as-absolute-directories %))
       (optional ["-O" "--output-dir" "base directory to store the output files" :default (get-cwd)])
       (optional ["-x" "--xml-style" "select XML output style" :default "pan"] #(get-formatter %))
       (optional ["-y" "--dependency" "output dependency information" :default false])
       
       (optional ["--nthread" "number of parallel threads" :default 0])

       (optional ["-i" "--max-iteration" "max. number of iterations" :default 10000])
       (optional ["-r" "--max-recursion" "max. number of recursions" :default 10])
       (optional ["-g" "--gzip" "compress machine configuration files" :default false])
       (optional ["--failonwarn" "treating warnings as errors" :default false])
       (optional ["-p" "--deprecation" "set deprecation level" :default 0])

       (optional ["--java-opts" "define java options"])

       (optional ["-k" "--noconf" "do not use configuration files" :default false])

       (optional ["--logging" "enable logging" :default [] ])
       (optional ["--logfile" "name of the log file to write"])

       (optional ["-v" "--version" "print the version of the compiler"])))

(defn- create-compiler-options [options] 
  (new org.quattor.pan.CompilerOptions
       (:debug-include options)
       (:debug-exclude options)
       (:xml-write options)
       (:dependency options)
       (:max-iteration options)
       (:max-recursion options)
       (:xml-style options)
       (:output-dir options)
       (:session-dir options)
       (:include-dir options)
       (:nthread options)
       (:gzip options)
       (:deprecation options)
       false
       (:annotation-dir options)
       (:annotation-base-dir options)
       (:failonwarn options)
       ))

(defn- run-compiler [compiler-options] 
  (let [compiler-results (org.quattor.pan.Compiler/run compiler-options [] [])]
    (println (.formatStats compiler-results))))

(defn -main [& args]
  (let [options (apply parse-options args)
        compiler-options (create-compiler-options options)]
    (println options)
    (println compiler-options)
    (run-compiler compiler-options)
    ))
