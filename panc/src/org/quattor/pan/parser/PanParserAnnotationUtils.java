package org.quattor.pan.parser;

import static org.quattor.pan.utils.MessageUtils.MSG_MISSING_SAX_TRANSFORMER;
import static org.quattor.pan.utils.MessageUtils.MSG_UNEXPECTED_EXCEPTION_WHILE_WRITING_OUTPUT;
import static org.quattor.pan.utils.MessageUtils.MSG_ERROR_WHILE_WRITING_OUTPUT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.quattor.pan.annotation.Annotation;
import org.quattor.pan.annotation.Annotation.Entry;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.exceptions.SystemException;
import org.quattor.pan.parser.ASTStatement.StatementType;
import org.quattor.pan.utils.MessageUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class PanParserAnnotationUtils {

	public static final String PAN_ANNO_NS = "http://quattor.org/pan/annotations";

	public static void printXML(File annotationDirectory, ASTTemplate ast) {

		String templateName = ast.getIdentifier();

		File outputFile = setupOutputFile(annotationDirectory, templateName);

		// Generate the transformer factory. Need to guarantee that we get a
		// SAXTransformerFactory.
		TransformerFactory factory = TransformerFactory.newInstance();
		if (!factory.getFeature(SAXTransformerFactory.FEATURE)) {
			throw CompilerError.create(MSG_MISSING_SAX_TRANSFORMER);
		}

		try {

			// Can safely cast the factory to a SAX-specific one. Get the
			// handler to feed with SAX events.
			SAXTransformerFactory saxfactory = (SAXTransformerFactory) factory;
			TransformerHandler handler = saxfactory.newTransformerHandler();

			// Set parameters of the embedded transformer.
			Transformer transformer = handler.getTransformer();
			Properties properties = new Properties();
			properties.setProperty(OutputKeys.INDENT, "yes");
			properties.setProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperties(properties);

			// Ok, feed SAX events to the output stream.
			OutputStream os = new FileOutputStream(outputFile);
			handler.setResult(new StreamResult(os));

			// Create an list of attributes which can be reused on a "per-call"
			// basis. This allows the class to remain a singleton.
			AttributesImpl atts = new AttributesImpl();

			// Begin the document and start the root element.
			handler.startDocument();

			// Add the attributes for the root element.
			atts.addAttribute(PAN_ANNO_NS, null, "format", "CDATA", "xmldb");

			// Process children recursively.
			writeASTNode(handler, ast);

			// Close the document. This will flush and close the underlying
			// stream.
			handler.endDocument();

		} catch (TransformerConfigurationException tce) {
			Error error = CompilerError
					.create(MSG_UNEXPECTED_EXCEPTION_WHILE_WRITING_OUTPUT);
			error.initCause(tce);
			throw error;

		} catch (SAXException se) {
			Error error = CompilerError
					.create(MSG_UNEXPECTED_EXCEPTION_WHILE_WRITING_OUTPUT);
			error.initCause(se);
			throw error;

		} catch (FileNotFoundException e) {
			String msg = MessageUtils.format(MSG_ERROR_WHILE_WRITING_OUTPUT,
					outputFile);
			SystemException exception = new SystemException(msg);
			throw exception;
		}

	}

	private static File setupOutputFile(File annotationDirectory,
			String templateName) {

		String separator = System.getProperty("file.separator");

		String localizedName = templateName.replaceAll("/", separator)
				+ ".annotation.xml";

		File outputFile = new File(annotationDirectory, localizedName);

		File outputDir = outputFile.getParentFile();

		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		return outputFile;
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

		String elementName = ast.getClass().getSimpleName();

		if (ast instanceof ASTTemplate) {

			ASTTemplate tplNode = (ASTTemplate) ast;

			elementName = "template";

			atts.addAttribute(PAN_ANNO_NS, null, "name", "CDATA", tplNode
					.getIdentifier());
			atts.addAttribute(PAN_ANNO_NS, null, "type", "CDATA", tplNode
					.getTemplateType().toString());

		} else if (ast instanceof ASTStatement) {

			ASTStatement node = (ASTStatement) ast;

			StatementType type = node.getStatementType();

			switch (type) {

			case FUNCTION: // fall through
			case VARIABLE: // fall through
			case TYPE:
				elementName = node.getStatementType().toString().toLowerCase();

				atts.addAttribute(PAN_ANNO_NS, null, "name", "CDATA", node
						.getIdentifier());

				break;

			default:
				elementName = null;

			}

		} else if (ast instanceof ASTFieldSpec) {

			ASTFieldSpec node = (ASTFieldSpec) ast;

			elementName = "field";

			try {
				atts.addAttribute(PAN_ANNO_NS, null, "name", "CDATA", node
						.getKey().toString());
			} catch (SyntaxException consumed) {
			}

			atts.addAttribute(PAN_ANNO_NS, null, "required", "CDATA", (node
					.isRequired() ? "yes" : "no"));

		} else if (ast instanceof ASTOperation) {
			elementName = null;
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

}
