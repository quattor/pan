(ns org.quattor.pan.cmd-option-utils
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  (:import java.io.File
           [org.quattor.pan.output TxtFormatter 
            JsonFormatter 
            DotFormatter 
            PanFormatter 
            XmlDBFormatter]))

(defn to-integer
  "Converts string to integer.  If the string is not a valid
   integer, then the method returns nil."
  [s]
  (try
    (Integer/valueOf s)
    (catch Exception e nil)))

(defn positive-integer
  "Ensure that the given value is a positive integer, otherwise thrown an
   IllegalArgumentException."
  [name value]
  (if-let [i (to-integer value)]
    (if (pos? i)
      {name i}
      (let [msg (str "only postive values for " name "are allowed")]
        (throw (ex-info msg {:type :options :msg msg}))))
    (let [msg (str "invalid integer value (" value ") for " name)]
      (throw (ex-info msg {:type :options :msg msg})))))

(defn split-on-commas
  "Returns list of non-blank and non-null strings,
   splitting on any combination of commas and
   whitespace."
  [s]
  (filter (complement str/blank?)
          (filter (complement nil?)
                  (str/split (or s "") #"[\s,]+"))))  

(defn split-path
  "Returns list of non-blank and non-null strings,
   splitting on the operating system's path separator."
  [s]
  (filter (complement str/blank?)
          (filter (complement nil?)
                  (str/split (or s "") (re-pattern File/pathSeparator))))) 

(defn absolute-file
  "Returns an absolute File object based on the argument.
   If no argument is given or is nil, then the current
   working directory is returned.  This method does not
   verify that the named file or directory exists."
  ([]
    (.getAbsoluteFile (io/file (System/getProperty "user.dir"))))
  ([x]
    (if (nil? x)
      (absolute-file)
      (.getAbsoluteFile (io/as-file x)))))

(defn directory?
  "Returns true if the given file exists and is a directory."
  [file]
  (if (instance? File file)
    (.isDirectory file)))

