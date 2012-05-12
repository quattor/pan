(ns org.quattor.pan.cmd-option-utils
  (:use [clojure.string :only [split blank?]]
        [clojure.java.io :only [as-file file]])
  (:import (org.quattor.pan.output TxtFormatter 
                                   JsonFormatter 
                                   DotFormatter 
                                   PanFormatter 
                                   XmlDBFormatter)))

(defn to-integer
  "Converts string to integer.  If the string is not a valid
   integer, then the method returns nil."
  [s]
  (try
    (Integer/valueOf s)
    (catch Exception e nil)))

(defn positive-integer
  [name value]
  (if-let [i (to-integer value)]
    (if (pos? i)
      {name i}
      (throw (Exception. (str "only postive values for " name "are allowed"))))
    (throw (Exception. (str "invalid integer value (" value ") for " name)))))

(defn split-on-commas
  "Returns list of non-blank and non-null strings,
   splitting on any combination of commas and
   whitespace."
  [s]
  (filter (complement blank?)
          (filter (complement nil?)
                  (split s #"[\s,]+"))))
        
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

(defn directory?
  [file]
  (.isDirectory file))

(defn pattern-list
  "Takes a list of regular expression patterns in a single
   string separated by whitespace and returns a list of
   regular expressions."
  ([]
    (pattern-list nil))
  ([s]
    (let [patterns-as-string (or s "")]
      (map re-pattern (split-on-commas patterns-as-string)))))
