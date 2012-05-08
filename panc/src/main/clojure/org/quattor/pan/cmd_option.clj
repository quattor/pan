(ns org.quattor.pan.cmd-option
  (:use [clojure.string :only [split]])
  (:import (org.quattor.pan.output TxtFormatter 
                                   JsonFormatter 
                                   DotFormatter 
                                   PanFormatter 
                                   XmlDBFormatter)))

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
