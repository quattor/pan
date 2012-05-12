(ns org.quattor.pan.cmd-option
  (:use org.quattor.pan.cmd-option-utils
        [clojure.string :only [split blank? join]]
        [clojure.java.io :only [as-file]])
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

(defmethod process :debug
  [name value]
  (if value
    {:debug-exclude-patterns []
     :debug-include-patterns [#".*"]}
    {}))

(defmethod process :debug-exclude-patterns
  [name value]
  {name (pattern-list value)})

(defmethod process :debug-include-patterns
  [name value]
  {name (pattern-list value)})

(defmethod process :include-path
  [name value]
  (let [paths (split-on-commas value)
        dirs (map absolute-file paths)
        bad-dirs (filter (complement directory?) dirs)]
    (if (= 0 (count bad-dirs))
      {name dirs}
      (throw (Exception. (str 
                           "include path must contain only existing directories: " 
                           (join " " bad-dirs)))))))

(defmethod process :session-dir
  [name value]
  (let [d (absolute-file value)
        ok? (directory? d)]
    (if ok?          
      {name d}
      (throw (Exception. (str name " must be an existing directory"))))))

(defmethod process :output-dir
  [name value]
  (let [d (absolute-file value)
        ok? (directory? d)]
    (if ok?          
      {name d}
      (throw (Exception. (str name " must be an existing directory"))))))

(defmethod process :formats
  [name value]
  (let [formatter-names (split value #"\s*,\s*")]
    (apply merge
           (map
             (fn [formatter-name]
               (case formatter-name
                 "text" {:xmlWriteEnabled true
                         :formatter (TxtFormatter/getInstance)}
                 "json" {:xmlWriteEnabled true
                         :formatter (JsonFormatter/getInstance)}
                 "dot" {:xmlWriteEnabled true
                        :formatter (DotFormatter/getInstance)}
                 "pan" {:xmlWriteEnabled true
                        :formatter (PanFormatter/getInstance)}
                 "pan.gz" {:xmlWriteEnabled true
                           :formatter (PanFormatter/getInstance)
                           :gzip-output true}
                 "xmldb" {:xmlWriteEnabled true
                          :formatter (XmlDBFormatter/getInstance)}
                 "dep" {:depWriteEnabled true}
                 (throw (Exception. (str "unknown formatter in formats: " formatter-name)))))
             formatter-names))))

(defmethod process :max-iteration
  [name value]
  (positive-integer name value))

(defmethod process :max-recursion
  [name value]
  (positive-integer name value))

(defmethod process :logging
  [name value]
  {name (split-on-commas value)})

(defmethod process :log-file
  [name value]
  {name (absolute-file value)})

(defmethod process :warnings
  [name value]
  (let [switches {"off" {:deprecationLevel -1 :failOnWarn false}
                  "on" {:deprecationLevel 1 :failOnWarn false}
                  "fail" {:deprecationLevel 1 :failOnWarn true}}]
    (if-let [switch (switches value)]
      switch
      (throw (Exception. (str name " value must be off, on, or fail"))))))
