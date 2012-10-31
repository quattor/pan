(ns org.quattor.pan.parser.ast
  (:require [org.quattor.pan.pan-compiler :refer [default-compiler-options]]
            [clojure.java.io :as io]
            [org.quattor.pan.settings :as settings])
  (:import [org.quattor.pan.parser
            PanParser
            ASTTemplate
            ASTStatement
            ASTStatement$StatementType
            ASTOperation
            ASTOperation$OperationType
            ASTDivOperation
            ASTAddOperation
            ASTMulOperation]
           [org.quattor.pan.template
            Template
            Template$TemplateType]))

(defn parse [pan-source]
  (let [pan-source-file (io/as-file pan-source)]
    (with-open [rdr (io/reader pan-source-file)]
      (let [parser (PanParser. rdr)
            options (default-compiler-options)]
        (.setFile parser pan-source-file)
        (.setCompilerOptions parser options)
        (.template parser)))))

(declare convert)

(defn convert-children [ast]
  (for [i (range (.jjtGetNumChildren ast))]
    (let [child (.jjtGetChild ast i)]
      (convert child))))
  
(defmulti convert
  (fn [ast] [(class ast) (.getSubtype ast)]))

(defmethod convert :default
  [ast]
  (cons (str (class ast) (.toString ast "")) (convert-children ast)))

(defmethod convert [ASTTemplate Template$TemplateType/OBJECT]
  [ast]
  (concat 
    (list `template (.getIdentifier ast))
    (convert-children ast)))

(defmethod convert [ASTStatement ASTStatement$StatementType/ASSIGN]
  [ast]
  (list 
    (.getStatementType ast)
    (.getIdentifier ast)
    (.getConditionalFlag ast)
    (.getFinalFlag ast)
    (convert-children ast)))

(defmethod convert [ASTOperation ASTOperation$OperationType/LITERAL]
  [ast]
  (.toString (.getOperation ast)))

(defmethod convert [ASTOperation ASTOperation$OperationType/DML]
  [ast]
  (let [result (convert-children ast)
        n (count result)]
    (if (== n 1)
      (first result)
      result)))

(defmethod convert [ASTOperation ASTOperation$OperationType/PLUS]
  [ast]
  (let [type (.getOperationType ast)]
    (cons type (convert-children ast))))

(defmethod convert [ASTOperation ASTOperation$OperationType/MINUS]
  [ast]
  (let [type (.getOperationType ast)]
    (cons type (convert-children ast))))

(defmethod convert [ASTDivOperation nil]
  [ast]
  (cons "/" (convert-children ast)))

(defmethod convert [ASTAddOperation nil]
  [ast]
  (cons "+" (convert-children ast)))

(defmethod convert [ASTMulOperation nil]
  [ast]
  (cons "*" (convert-children ast)))
