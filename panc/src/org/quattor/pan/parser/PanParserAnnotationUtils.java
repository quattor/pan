package org.quattor.pan.parser;

import static org.quattor.pan.utils.MessageUtils.MSG_MISSING_SAX_TRANSFORMER;
import static org.quattor.pan.utils.MessageUtils.MSG_UNEXPECTED_EXCEPTION_WHILE_WRITING_OUTPUT;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.quattor.pan.dml.data.Property;
import org.quattor.pan.exceptions.CompilerError;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class PanParserAnnotationUtils {

	public static void printXML(ASTTemplate ast) {

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
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			handler.setResult(new StreamResult(baos));

			// Create an list of attributes which can be reused on a "per-call"
			// basis. This allows the class to remain a singleton.
			AttributesImpl atts = new AttributesImpl();

			// Begin the document and start the root element.
			handler.startDocument();

			// Add the attributes for the root element.
			atts.addAttribute("pan-annotations", null, "format", "CDATA",
					"xmldb");

			// Process children recursively.
			writeASTNode(handler, ast);

			// Close the document. This will flush and close the underlying
			// stream.
			handler.endDocument();

			System.err.println(baos.toString());

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
		}

	}

	private static void writeASTNode(TransformerHandler handler, Node ast)
			throws SAXException {

		AttributesImpl atts = new AttributesImpl();

		handler.startElement("pan-annotations", null, ast.getClass()
				.getSimpleName(), atts);

		if (ast instanceof SimpleNode) {
			SimpleNode node = (SimpleNode) ast;
			for (Token t : node.getSpecialTokens()) {
				writeSpecialToken(handler, t);
			}
		}

		int nchild = ast.jjtGetNumChildren();
		for (int i = 0; i < nchild; i++) {
			writeASTNode(handler, ast.jjtGetChild(i));
		}

		handler.endElement("pan-annotations", null, ast.getClass()
				.getSimpleName());
	}

	private static void writeSpecialToken(TransformerHandler handler, Token t)
			throws SAXException {

		AttributesImpl atts = new AttributesImpl();

		handler.startElement("pan-annotations", null, t.getClass()
				.getSimpleName(), atts);

		String s = t.image;
		handler.characters(s.toCharArray(), 0, s.length());

		handler.endElement("pan-annotations", null, t.getClass()
				.getSimpleName());
	}

}
