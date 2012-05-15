(ns org.quattor.pan.settings
  (:use clojure.tools.cli
        [clojure.java.io :only (as-file file)])
  (:import org.quattor.pan.output.PanFormatter))

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

(def ^:dynamic *settings* (defaults))

(defmacro with-settings [settings & body]
 `(binding [*settings* ~settings]
   ~@body))

(defn init! [settings]
 (alter-var-root #'*settings* settings))

(defn merge-defaults [m]
  (merge (defaults) (or m {})))
