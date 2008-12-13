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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/output/XmlDBFormatter.java $
 $Id: XmlDBFormatter.java 3597 2008-08-17 09:08:57Z loomis $
 */

package org.quattor.pan.output;

import static org.quattor.pan.utils.MessageUtils.MSG_MISSING_SAX_TRANSFORMER;
import static org.quattor.pan.utils.MessageUtils.MSG_UNEXPECTED_EXCEPTION_WHILE_WRITING_OUTPUT;

import java.io.PrintStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.exceptions.CompilerError;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class XmlDBFormatter implements Formatter {

	private static final XmlDBFormatter instance = new XmlDBFormatter();

	private static final String XMLDB_NS = XMLConstants.NULL_NS_URI;

	private static final Pattern p = Pattern.compile("^[\\w\\.-]+$");

	private static final String suffix = "xml";

	private static final String key = "xmldb";

	private XmlDBFormatter() {
	}

	public static XmlDBFormatter getInstance() {
		return instance;
	}

	public String getFileExtension() {
		return suffix;
	}

	public String getFormatKey() {
		return key;
	}

	public void write(Element root, String rootName, PrintStream ps) {

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
			handler.setResult(new StreamResult(ps));

			// Create an list of attributes which can be reused on a "per-call"
			// basis. This allows the class to remain a singleton.
			AttributesImpl atts = new AttributesImpl();

			// Begin the document and start the root element.
			handler.startDocument();

			// Add the attributes for the root element.
			atts.addAttribute(XMLDB_NS, null, "format", "CDATA", "xmldb");

			// Counter for the number of lists
			Integer nbList = 0;

			// Process children recursively.
			writeChild(handler, atts, ps, root, rootName, nbList);

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
		}
	}

	private void writeChild(TransformerHandler handler, AttributesImpl atts,
			PrintStream ps, Element node, String name, Integer nbList)
			throws SAXException {

		// Normally the tag name will just be the type of the element.
		String tagName = node.getTypeAsString();

		String nameCheck = null;

		if (nbList != 0) {
			if (!tagName.equals("list") && !tagName.equals("nlist")) {
				atts.addAttribute(XMLDB_NS, null, "type", "CDATA", tagName);
			}
			atts.addAttribute(XMLDB_NS, null, "list", "CDATA", nbList
					.toString());
		} else if (!tagName.equals("nlist")) {
			atts.addAttribute(XMLDB_NS, null, "type", "CDATA", tagName);
		}

		// Start the element. The name attribute must be passed in by the
		// parent. Any additional attributes can also be passed in.
		if (!(tagName.equals("list") && nbList == 0)) {
			nameCheck = checkName(name);
			if (!nameCheck.equals(name)) {
				atts.addAttribute(XMLDB_NS, null, "unencoded", "CDATA", name);
			}
			handler.startElement(XMLDB_NS, null, nameCheck, atts);
		}

		// Clear the attribute structure for reuse.
		atts.clear();

		if (node instanceof HashResource) {

			// Iterate over all children of the hash, setting the name attribute
			// for each one.
			HashResource hash = (HashResource) node;
			int nblistmem = nbList;
			nbList = 0;
			for (Resource.Entry entry : hash) {
				String nameChild = entry.getKey().toString();
				writeChild(handler, atts, ps, entry.getValue(), nameChild,
						nbList);
			}
			nbList = nblistmem;

		} else if (node instanceof ListResource) {

			// Iterate over all children of the list. Children of lists are
			// anonymous; do not set name attribute.
			ListResource list = (ListResource) node;
			for (Resource.Entry entry : list) {
				nbList++;
				writeChild(handler, atts, ps, entry.getValue(), name, nbList);
				nbList--;
			}

		} else {

			String s = ((Property) node).toString();
			handler.characters(s.toCharArray(), 0, s.length());
		}

		// Finish the element.
		if (!(tagName.equals("list") && nbList == 0)) {
			handler.endElement(XMLDB_NS, null, nameCheck);
		}
	}

	private String checkName(String name) {
		Matcher m = p.matcher(name);
		boolean b = m.matches();
		if ((name.startsWith("xml")) || (!b)) {
			name = "_".concat(data2hex(name.getBytes()));
		} else if (name.equals("")) {
			name = "_";
		}
		return name;
	}

	private static final String data2hex(byte[] data) {
		if (data == null) {
			return null;
		}

		int len = data.length;
		StringBuffer buf = new StringBuffer(len * 2);
		for (int pos = 0; pos < len; pos++) {
			buf.append(toHexChar((data[pos] >>> 4) & 0x0F)).append(
					toHexChar(data[pos] & 0x0F));
		}
		return buf.toString();
	}

	private static char toHexChar(int i) {
		if ((0 <= i) && (i <= 9)) {
			return (char) ('0' + i);
		} else {
			return (char) ('a' + (i - 10));
		}
	}
}
