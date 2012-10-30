(ns org.quattor.pan.cmd-option
  (:require [clojure.string :as str]
            [org.quattor.pan.settings :as settings]
            [org.quattor.pan.cmd-option-utils :as utils])
  (:import [org.quattor.pan.output TxtFormatter 
                                   JsonFormatter
                                   JsonGzipFormatter
                                   DotFormatter 
                                   PanFormatter
                                   PanGzipFormatter
                                   XmlFormatter
                                   XmlGzipFormatter
                                   DepFormatter]
           [org.quattor.pan CompilerOptions$DeprecationWarnings]
           [clojure.lang ExceptionInfo]))

(declare process)

(defn to-settings [cli-options]
  (settings/merge-defaults (into {} (mapcat process cli-options))))

(defn str->formatters
  "Returns map with vector of formatters from comma-separated list of formatter names."
  [s]
  (let [names (str/split s #"\s*,\s*")]
    {:formatter 
     (reduce
       (fn [v name]
         (case name
           "text" (conj v (TxtFormatter/getInstance))
           "json" (conj v (JsonFormatter/getInstance))
           "json.gz" (conj v (JsonGzipFormatter/getInstance))
           "dot" (conj v (DotFormatter/getInstance))
           "pan" (conj v(PanFormatter/getInstance))
           "pan.gz" (conj v (PanGzipFormatter/getInstance))
           "xml" (conj v(XmlFormatter/getInstance))
           "xml.gz" (conj v (XmlGzipFormatter/getInstance))
           "dep" (conj v (DepFormatter/getInstance))
           "none" v
           (let [msg (str "unknown formatter: " name)]
             (throw (ex-info msg {:type :options :msg msg})))))
       #{}
       names)}))

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

(defmethod process :debug-ns-include
  [[k v]]
  (let [patterns (if v [(re-pattern v)] [])]
    {:debug-include-patterns patterns}))

(defmethod process :debug-ns-exclude
  [[k v]]
  (let [patterns (if v [(re-pattern v)] [])]
    {:debug-exclude-patterns patterns}))

(defmethod process :include-path
  [[k v]]
  (let [paths (utils/split-path v)
        dirs (map utils/absolute-file paths)
        bad-dirs (filter (complement utils/directory?) dirs)]
    (if (= 0 (count bad-dirs))
      {(keyword k) dirs}
      (let [msg (str "include path must contain only existing directories: " 
                     (str/join " " bad-dirs))]
        (throw (ex-info msg {:type :options :msg msg}))))))

(defmethod process :output-dir
  [[k v]]
  (let [d (utils/absolute-file v)
        ok? (utils/directory? d)]
    (if ok?          
      {(keyword k) d}
      (let [msg (str name " must be an existing directory")]
        (throw (ex-info msg {:type :options :msg msg}))))))

(defmethod process :formats
  [[k v]]
  (str->formatters v))

(defmethod process :max-iteration
  [[k v]]
  (utils/positive-integer (keyword k) v))

(defmethod process :max-recursion
  [[k v]]
  (utils/positive-integer (keyword k) v))

(defmethod process :logging
  [[k v]]
  {(keyword k) (utils/split-on-commas v)})

(defmethod process :log-file
  [[k v]]
  {(keyword k) (utils/absolute-file v)})

(defmethod process :warnings
  [[k v]]
  (let [switches {"off" {:warnings CompilerOptions$DeprecationWarnings/OFF}
                  "on" {:warnings CompilerOptions$DeprecationWarnings/ON}
                  "fatal" {:warnings CompilerOptions$DeprecationWarnings/FATAL}}]
    (if-let [switch (switches v)]
      switch
      (let [msg (str k " value must be off, on, or fatal")]
        (throw (ex-info msg {:type :options :msg msg}))))))

