(ns org.quattor.pan.settings
  (:require [clojure.tools.cli :refer :all]
            [clojure.java.io :refer [as-file file]])
  (:import (org.quattor.pan.output PanFormatter
                                   DepFormatter)
           (org.quattor.pan CompilerOptions$DeprecationWarnings)))

(defn absolute-file
  "Returns an absolute File object based on the argument.
   If no argument is given or is nil, then the current
   working directory is returned.  This method does not
   verify that the named file or directory exists."
  ([]
    (.getAbsoluteFile (file (System/getProperty "user.dir"))))
  ([x]
    (if (nil? x)
      (absolute-file)
      (.getAbsoluteFile (as-file x)))))

(defn defaults []
  {:debug-ns-include nil
   :debug-ns-exclude nil
   :max-iteration 10000
   :max-recursion 50
   :formatter #{(PanFormatter/getInstance), (DepFormatter/getInstance)}
   :output-dir (absolute-file)
   :include-path [(absolute-file)]
   :gzipOutput false
   :deprecationWarnings CompilerOptions$DeprecationWarnings/OFF
   :annotationDirectory nil
   :annotationBaseDirectory nil
   :initial-data nil
   :nthread 0})

(def ^:dynamic *settings* (defaults))

(defmacro with-settings [settings & body]
 `(binding [*settings* ~settings]
   ~@body))

(defn init! [settings]
 (alter-var-root #'*settings* settings))

(defn merge-defaults [m]
  (merge (defaults) (or m {})))
