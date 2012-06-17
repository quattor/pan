/*
 Copyright (c) 2006 Charles A. Loomis, Jr, Cedric Duprilot, and
 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/output/PanFormatter.java $
 $Id: PanFormatter.java 3597 2008-08-17 09:08:57Z loomis $
 */

package org.quattor.pan.output;

import static org.quattor.pan.utils.MessageUtils.MSG_UNEXPECTED_EXCEPTION_WHILE_WRITING_OUTPUT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.tasks.Valid2Result;
import org.quattor.pan.utils.Base64;
import org.quattor.pan.utils.XmlUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class PanFormatter implements Formatter {

	private static final PanFormatter instance = new PanFormatter();

	private static final String PAN_NS = XMLConstants.NULL_NS_URI;

	private static final String suffix = "xml";

	private static final String key = "pan";

	private PanFormatter() {
	}

	public static PanFormatter getInstance() {
		return instance;
	}

	public String getFileExtension() {
		return suffix;
	}

	public URI getResultURI(String objectName) {
		return FormatterUtils.getResultURI(objectName, suffix);
	}

	public String getFormatKey() {
		return key;
	}

	public void write(String objectName, URI outputDirectory,
			Valid2Result result) throws Exception {

		URI resultURI = getResultURI(objectName);
		URI absoluteURI = outputDirectory.resolve(resultURI);
		File absolutePath = new File(absoluteURI);

		FormatterUtils.createParentDirectories(absolutePath);

		OutputStream os = new FileOutputStream(absolutePath);

		// GZIP OUTPUT
		// absolutePath = new File(absolutePath.toString() + ".gz");
		// os = new GZIPOutputStream(new FileOutputStream(absolutePath));

		PrintWriter ps = new PrintWriter(os);
		write(result.getRoot(), "profile", ps);
		ps.close();

		// Make sure that the file has the timestamp passed into the
		// constructor.
		if (!absolutePath.setLastModified(result.timestamp)) {
			// Probably a warning should be emitted here, but currently
			// there are no facilities for warnings in the pan compiler
			// yet.
		}

	}

	public void write(Element root, String rootName, PrintWriter ps) {

		try {

			TransformerHandler handler = XmlUtils.getSaxTransformerHandler();

			// Ok, feed SAX events to the output stream.
			handler.setResult(new StreamResult(ps));

			// Create an list of attributes which can be reused on a "per-call"
			// basis. This allows the class to remain a singleton.
			AttributesImpl atts = new AttributesImpl();

			// Begin the document and start the root element.
			handler.startDocument();

			// Add the attributes for the root element.
			atts.addAttribute(PAN_NS, null, "format", "CDATA", "pan");
			atts.addAttribute(PAN_NS, null, "name", "CDATA", rootName);

			// Process children recursively.
			writeChild(handler, atts, ps, root);

			// Close the document. This will flush and close the underlying
			// stream.
			handler.endDocument();

		} catch (SAXException se) {
			Error error = CompilerError
					.create(MSG_UNEXPECTED_EXCEPTION_WHILE_WRITING_OUTPUT);
			error.initCause(se);
			throw error;
		}
	}

	private void writeChild(TransformerHandler handler, AttributesImpl atts,
			PrintWriter ps, Element node) throws SAXException {

		String tagName = node.getTypeAsString();
		String stringContents = null;

		if (node instanceof StringProperty) {

			// Normally the tag name will just be the type of the element.
			// However, for links we need to be careful.
			if (!"string".equals(tagName)) {
				atts.addAttribute(PAN_NS, null, "type", "CDATA", tagName);
				tagName = "string";
			}

			// Check to see if the string contents need to be encoded.
			String s = ((Property) node).toString();
			if (XMLFormatterUtils.isValidXMLString(s)) {
				stringContents = s;
			} else {
				stringContents = Base64.encodeBytes(s.getBytes());
				atts.addAttribute(PAN_NS, null, "encoding", "CDATA", "base64");
			}

		}

		// Start the element. The name attribute must be passed in by the
		// parent. Any additional attributes can also be passed in.
		handler.startElement(PAN_NS, null, tagName, atts);

		// Clear the attribute structure for reuse.
		atts.clear();

		if (node instanceof HashResource) {

			// Iterate over all children of the hash, setting the name attribute
			// for each one.
			HashResource hash = (HashResource) node;
			for (Resource.Entry entry : hash) {
				String name = entry.getKey().toString();
				atts.addAttribute(PAN_NS, null, "name", "CDATA", name);
				writeChild(handler, atts, ps, entry.getValue());
			}

		} else if (node instanceof ListResource) {

			// Iterate over all children of the list. Children of lists are
			// anonymous; do not set name attribute.
			ListResource list = (ListResource) node;
			for (Resource.Entry entry : list) {
				writeChild(handler, atts, ps, entry.getValue());
			}

		} else if (node instanceof StringProperty) {

			handler.characters(stringContents.toCharArray(), 0,
					stringContents.length());

		} else if (node instanceof Property) {

			String s = ((Property) node).toString();
			handler.characters(s.toCharArray(), 0, s.length());
		}

		// Finish the element.
		handler.endElement(PAN_NS, null, tagName);
	}

}
