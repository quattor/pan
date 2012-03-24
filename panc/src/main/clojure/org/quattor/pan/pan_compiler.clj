(ns org.quattor.pan.pan-compiler
  (:gen-class)
  (:use clojure.tools.cli))

(defn -main [& args]
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
             ["--jvm-options" "options for JVM"]
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
    (println options)
    (println files)))
