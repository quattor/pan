(ns org.quattor.pan.pan-compiler-test
  (:require [clojure.test :refer :all]
            [clojure.tools.cli :as cli]
            [org.quattor.pan.cmd-option :as option]
            [org.quattor.pan.settings :as settings]
            [org.quattor.pan.pan-compiler :refer :all])
  (:import [org.quattor.pan CompilerOptions]))

(deftest test-create-compiler-options
  (is (instance? CompilerOptions (create-compiler-options))))

(deftest test-cli-global-trace-options
  (let [{:keys [options errors summary]} (cli/parse-opts ["--trace-object-glob" "site/[x].?*"
                                                         "--trace-variable-glob" "a.[x]?*"] cli-args)
        configured (settings/with-settings (option/to-settings options) (create-compiler-options))]
    (is (nil? errors))
    (is (.isGlobalVariableTraceEnabled configured "site/[x].a/host"))
    (is (not (.isGlobalVariableTraceEnabled configured "site/x.a/host")))
    (is (.matches (.matcher (.getGlobalVariableTracePattern configured) "a.[x]/b")))
    (is (not (.matches (.matcher (.getGlobalVariableTracePattern configured) "a.x/b"))))
    (is (.contains summary "stderr"))
    (is (.contains summary "sensitive")))
  (is (not (.isGlobalVariableTraceEnabled (default-compiler-options) "site/host")))
  (let [configured (settings/with-settings (option/to-settings {:trace-variable-glob "*"})
                     (create-compiler-options))]
    (is (not (.isGlobalVariableTraceEnabled configured "site/host"))))
  (let [configured (settings/with-settings (option/to-settings {:trace-object-glob "site/*"})
                     (create-compiler-options))]
    (is (.isGlobalVariableTraceEnabled configured "site/host"))
    (is (.matches (.matcher (.getGlobalVariableTracePattern configured) "a/b"))))
  (doseq [option-name ["--trace-object-glob" "--trace-variable-glob"]]
    (let [{:keys [options errors]} (cli/parse-opts [option-name ""] cli-args)]
      (is (nil? errors))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo (re-pattern option-name)
                            (option/to-settings options))))))
