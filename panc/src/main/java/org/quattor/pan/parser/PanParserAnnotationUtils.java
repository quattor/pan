package org.quattor.pan.parser;

import static org.quattor.pan.utils.MessageUtils.MSG_ERROR_CREATING_DIRECTORY;
import static org.quattor.pan.utils.MessageUtils.MSG_ERROR_WHILE_WRITING_OUTPUT;
import static org.quattor.pan.utils.MessageUtils.MSG_UNEXPECTED_EXCEPTION_WHILE_WRITING_OUTPUT;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.quattor.pan.annotation.Annotation;
import org.quattor.pan.annotation.Annotation.Entry;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.exceptions.SystemException;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.parser.ASTStatement.StatementType;
import org.quattor.pan.parser.ASTOperation.OperationType;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.XmlUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class PanParserAnnotationUtils {

    public static final String PAN_ANNO_NS = "http://quattor.org/pan/annotations";

    public static void printXML(File outputFile, ASTTemplate ast) {

        String templateName = ast.getIdentifier();

        createDirectories(outputFile);

        Writer writer = null;

        try {

            TransformerHandler handler = XmlUtils.getSaxTransformerHandler();

            // Ok, feed SAX events to the output stream.
            writer = new PrintWriter(outputFile, "UTF-8");
            handler.setResult(new StreamResult(writer));

            handler.startDocument();

            // Process children recursively.
            writeASTNode(handler, ast);

            // Flushes and closes the underlying stream.
            handler.endDocument();

        } catch (SAXException se) {
            Error error = CompilerError
                    .create(MSG_UNEXPECTED_EXCEPTION_WHILE_WRITING_OUTPUT);
            error.initCause(se);
            throw error;

        } catch (IOException e) {
            String msg = MessageUtils.format(MSG_ERROR_WHILE_WRITING_OUTPUT,
                    outputFile);
            SystemException exception = new SystemException(msg);
            throw exception;

        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException consumed) {
                }
            }
        }

    }

    private static void createDirectories(File outputFile) {

        File outputDir = outputFile.getParentFile();

        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new SystemException(MSG_ERROR_CREATING_DIRECTORY, outputDir);
            }
        }

    }

    private static void writeASTNode(TransformerHandler handler, Node ast)
            throws SAXException {

        AttributesImpl atts = new AttributesImpl();

        String elementName = getElementInfo(ast, atts);

        if (elementName != null) {
            handler.startElement(PAN_ANNO_NS, null, elementName, atts);

            if (ast instanceof SimpleNode) {
                SimpleNode node = (SimpleNode) ast;
                for (Token t : node.getSpecialTokens()) {
                    writeAnnotationToken(handler, t);
                }
            }
        }

        int nchild = ast.jjtGetNumChildren();
        for (int i = 0; i < nchild; i++) {
            writeASTNode(handler, ast.jjtGetChild(i));
        }

        if (elementName != null) {
            handler.endElement(PAN_ANNO_NS, null, elementName);
        }

    }

    private static String getElementInfo(Node ast, AttributesImpl atts) {

        String elementName = null;

        if (ast instanceof ASTTemplate) {

            ASTTemplate tplNode = (ASTTemplate) ast;

            elementName = "template";

            addNameAttribute(atts, tplNode.getIdentifier());
            addSourceRangeAttribute(atts, tplNode);
            addAttribute(atts, "type", tplNode.getTemplateType());

        } else if (ast instanceof ASTStatement) {

            ASTStatement node = (ASTStatement) ast;

            StatementType type = node.getStatementType();

            switch (type) {

            case FUNCTION: // fall through
            case VARIABLE: // fall through
            case TYPE:
                elementName = node.getStatementType().toString().toLowerCase();

                addNameAttribute(atts, node.getIdentifier());
                addSourceRangeAttribute(atts, node);

                break;

            default:
                elementName = null;

            }

        } else if (ast instanceof ASTFieldSpec) {

            ASTFieldSpec node = (ASTFieldSpec) ast;

            if (node.getInclude() == null) {

                elementName = "field";

                try {
                    addNameAttribute(atts, node.getKey());
                } catch (SyntaxException consumed) {
                }
                addSourceRangeAttribute(atts, node);
                addAttribute(atts, "required", node.isRequired());

            } else {

                elementName = "include";

                addNameAttribute(atts, node.getInclude());
                addSourceRangeAttribute(atts, node);

            }

        } else if (ast instanceof ASTBaseTypeSpec) {

            ASTBaseTypeSpec node = (ASTBaseTypeSpec) ast;

            elementName = "basetype";

            addNameAttribute(atts, node.getIdentifier());
            addSourceRangeAttribute(atts, node);
            addAttribute(atts, "extensible", node.isExtensible());
            addAttribute(atts, "range", node.getRange());

        } else if (ast instanceof ASTOperation) {
            ASTOperation node = (ASTOperation) ast;
            if (ast.jjtGetParent() instanceof ASTOperation) {
                ASTOperation parent = (ASTOperation) ast.jjtGetParent();
                if (parent.getOperationType() == OperationType.DML &&
                    parent.jjtGetParent() instanceof ASTOperation) {
                    ASTOperation grandparent = (ASTOperation) parent.jjtGetParent();
                    if (grandparent.getOperationType() == OperationType.DEFAULT) {
                        if (node.getOperation() != null) {
                            elementName = "default";
                            addSourceRangeAttribute(atts, node);
                            addAttribute(atts, "text", node.getOperation().toString());
                        }
                    }
                }
            }
        }

        return elementName;
    }

    private static void writeAnnotationToken(TransformerHandler handler, Token t)
            throws SAXException {

        if (t instanceof AnnotationToken) {
            AnnotationToken token = (AnnotationToken) t;
            Annotation annotation = (Annotation) token.getValue();

            String name = annotation.getName();

            AttributesImpl atts = new AttributesImpl();

            if (!annotation.isAnonymous()) {
                handler.startElement(PAN_ANNO_NS, null, name, atts);
            }

            for (Entry entry : annotation.getEntries()) {

                String elementName = entry.getKey();
                char[] elementContents = entry.getValue().toCharArray();

                handler.startElement(PAN_ANNO_NS, null, elementName, atts);
                handler.characters(elementContents, 0, elementContents.length);
                handler.endElement(PAN_ANNO_NS, null, elementName);

            }

            if (!annotation.isAnonymous()) {
                handler.endElement(PAN_ANNO_NS, null, name);
            }

        }
    }

    public static void addAttribute(AttributesImpl atts, String name,
            Object value) {
        if (value != null) {
            atts.addAttribute(PAN_ANNO_NS, null, name, "CDATA", value
                    .toString());
        }
    }

    public static void addAttribute(AttributesImpl atts, String name,
            boolean value) {
        Boolean bvalue = Boolean.valueOf(value);
        addAttribute(atts, name, bvalue);
    }

    public static void addNameAttribute(AttributesImpl atts, Object value) {
        addAttribute(atts, "name", value);
    }

    public static void addSourceRangeAttribute(AttributesImpl atts,
            SimpleNode node) {
        SourceRange sourceRange = node.getSourceRange();
        addAttribute(atts, "source-range", sourceRange);
    }

}
