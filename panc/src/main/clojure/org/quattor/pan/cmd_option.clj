(ns org.quattor.pan.cmd-option
  (:use [org.quattor.pan.cmd-option-utils]
        [clojure.string :only [split blank? join]]
        [clojure.java.io :only [as-file]])
  (:require [org.quattor.pan.settings :as settings])
  (:import (org.quattor.pan.output TxtFormatter 
                                   JsonFormatter 
                                   DotFormatter 
                                   PanFormatter 
                                   XmlDBFormatter)))

(declare process)

(defn to-settings [cli-options]
  (settings/merge-defaults (into {} (mapcat process cli-options))))

(defmulti process
  "Process a command line option given the name and
   string value passed in.  Returns validated and
   updated value.  The dispatch value is the parameter
   name as a keyword."
  (fn [[k v]] (keyword k)))

(defmethod process :default
  [[k v]]
  {(keyword k) v})

(defmethod process :debug
  [[k v]]
  (if v
    {:debug-exclude-patterns []
     :debug-include-patterns [#".*"]}
    {}))

(defmethod process :debug-exclude-patterns
  [[k v]]
  {(keyword k) (pattern-list v)})

(defmethod process :debug-include-patterns
  [[k v]]
  {(keyword k) (pattern-list v)})

(defmethod process :include-path
  [[k v]]
  (let [paths (split-on-commas v)
        dirs (map absolute-file paths)
        bad-dirs (filter (complement directory?) dirs)]
    (if (= 0 (count bad-dirs))
      {(keyword k) dirs}
      (throw (Exception. (str 
                           "include path must contain only existing directories: " 
                           (join " " bad-dirs)))))))

(defmethod process :session-dir
  [[k v]]
  (let [d (absolute-file v)
        ok? (directory? d)]
    (if ok?          
      {(keyword k) d}
      (throw (Exception. (str name " must be an existing directory"))))))

(defmethod process :output-dir
  [[k v]]
  (let [d (absolute-file v)
        ok? (directory? d)]
    (if ok?          
      {(keyword k) d}
      (throw (Exception. (str name " must be an existing directory"))))))

(defmethod process :formats
  [[k v]]
  (let [formatter-names (split v #"\s*,\s*")]
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
  [[k v]]
  (positive-integer (keyword k) v))

(defmethod process :max-recursion
  [[k v]]
  (positive-integer (keyword k) v))

(defmethod process :logging
  [[k v]]
  {(keyword k) (split-on-commas v)})

(defmethod process :log-file
  [[k v]]
  {(keyword k) (absolute-file v)})

(defmethod process :warnings
  [[k v]]
  (let [switches {"off" {:deprecationLevel -1 :failOnWarn false}
                  "on" {:deprecationLevel 1 :failOnWarn false}
                  "fail" {:deprecationLevel 1 :failOnWarn true}}]
    (if-let [switch (switches v)]
      switch
      (throw (Exception. (str k " value must be off, on, or fail"))))))
