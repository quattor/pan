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

(defn str->formatters
  "Returns map with vector of formatters from comma-separated list of formatter names."
  [s]
  (let [names (split s #"\s*,\s*")]
    {:formatter 
     (reduce
       (fn [v name]
         (case name
           "text" (conj v (TxtFormatter/getInstance))
           "json" (conj v (JsonFormatter/getInstance))
           "dot" (conj v (DotFormatter/getInstance))
           "pan" (conj v(PanFormatter/getInstance))
           "pan.gz" (conj v (PanFormatter/getInstance))
           "xml" (conj v(PanFormatter/getInstance))
           "xml.gz" (conj v (PanFormatter/getInstance))
           "xmldb" (conj v (XmlDBFormatter/getInstance))
           "dep" v
           "none" v
           (throw (Exception. (str "unknown formatter in formats: " name)))))
       []
       names)}))

(defn str->formatter-options
  "Returns map of formatters options from comma-separated list of formatter names."
  [s]
  (let [names (split s #"\s*,\s*")]
    (reduce 
      (fn [m name]
        (case name
          "text" (assoc m :xmlWriteEnabled true)
          "json" (assoc m :xmlWriteEnabled true)
          "dot" (assoc m :xmlWriteEnabled true)
          "pan" (assoc m :xmlWriteEnabled true)
          "pan.gz" (assoc m :xmlWriteEnabled true :gzipOutput true)
          "xml" (assoc m :xmlWriteEnabled true)
          "xml.gz" (assoc m :xmlWriteEnabled true :gzipOutput true)
          "xmldb" (assoc m :xmlWriteEnabled true)
          "dep" (assoc m :depWriteEnabled true)
          "none" (assoc m :xmlWriteEnabled false)
          (throw (Exception. (str "unknown formatter in formats: " name)))))
      {}
      names)))

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

;; FIXME: The formatters need to be accumulated into a single vector.
(defmethod process :formats
  [[k v]]
  (let [formatters (str->formatters v)
        formatter-options (str->formatter-options v)]
    (merge formatters formatter-options)))

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
