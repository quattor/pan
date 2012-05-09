(ns org.quattor.pan.cmd-option
  (:use [clojure.string :only [split blank?]])
  (:import (org.quattor.pan.output TxtFormatter 
                                   JsonFormatter 
                                   DotFormatter 
                                   PanFormatter 
                                   XmlDBFormatter)))
(defn pattern-list
  "Takes a list of regular expression patterns in a single
   string separated by whitespace and returns a list of
   regular expressions."
  [s]
  (let [patterns (or s "")]
    (map re-pattern (filter (complement blank?) (split patterns #"\s+")))))

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


(defmulti process
  "Process a command line option given the name and
   string value passed in.  Returns validated and
   updated value.  The dispatch value is the parameter
   name as a keyword."
  (fn [name value] (keyword name)))

(defmethod process :default
  [name value]
  {(keyword name) value})

(defmethod process :warnings
  [name value]
  (case value
    "off" {:deprecationLevel -1 :failOnWarn false}
    "on" {:deprecationLevel 1 :failOnWarn false}
    "fail" {:deprecationLevel 1 :failOnWarn true}
    (throw (Exception. "warnings value must be off, on, or fail"))))

(defmethod process :formats
  [name value]
  (let [formatter-names (split value #"\s*,\s*")]
    (apply merge
           (map
             (fn [formatter-name]
               (case formatter-name
                 "text" {:formatter (TxtFormatter/getInstance)}
                 "json" {:formatter (JsonFormatter/getInstance)}
                 "dot" {:formatter (DotFormatter/getInstance)}
                 "pan" {:formatter (PanFormatter/getInstance)}
                 "xmldb" {:formatter (XmlDBFormatter/getInstance)}
                 "dep" {:depWriteEnabled true}
                 (throw (Exception. (str "unknown formatter in formats: " formatter-name)))))
             formatter-names))))

(defmethod process :compress
  [name value]
  (if value
    {:gzip-output true}
    {:gzip-output false}))

(defmethod process :debug-exclude-patterns
  [name value]
  {name (pattern-list value)})

(defmethod process :debug-include-patterns
  [name value]
  {name (pattern-list value)})

(defmethod process :iteration-limit
  [name value]
  (positive-integer name value))

(defmethod process :call-depth-limit
  [name value]
  (positive-integer name value))



