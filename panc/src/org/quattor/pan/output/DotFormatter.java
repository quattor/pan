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
 $Id: PanFormatter.java 2985 2008-03-01 08:24:17Z loomis $
 */

package org.quattor.pan.output;

import java.io.PrintStream;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.StringProperty;

/**
 * A formatter that will transform a machine profile into a graph in dot syntax.
 * The dot syntax is that used by GraphViz.
 * 
 * @author loomis
 * 
 */
public class DotFormatter implements Formatter {

	private static final DotFormatter instance = new DotFormatter();

	private static final String suffix = "dot";

	private static final String key = "dot";

	private DotFormatter() {
	}

	public static DotFormatter getInstance() {
		return instance;
	}

	public String getFileExtension() {
		return suffix;
	}

	public String getFormatKey() {
		return key;
	}

	public void write(Element root, String rootName, PrintStream ps) {

		writeHeader(rootName, ps);
		writeRoot(rootName, root, ps);
		writeFooter(ps);

	}

	private void writeRoot(String myName, Element node, PrintStream ps) {

		String tagName = node.getTypeAsString();

		// Normally the tag name will just be the type of the element. However,
		// for links we need to be careful.
		if (node instanceof StringProperty && !"string".equals(tagName)) {
			tagName = "string";
		}

		// Fix the label for the root element.
		String myPath = "/" + myName;
		ps.append("\"" + myPath + "\" [ label = \"" + myName + "\"]\n");

		// The root must be a resource. If it isn't do nothing.
		if (node instanceof Resource) {

			// Iterate over all children of the hash, setting the name attribute
			// for each one.
			Resource resource = (Resource) node;
			for (Resource.Entry entry : resource) {
				String nameChild = entry.getKey().toString();
				writeChild(myPath, nameChild, entry.getValue(), ps);
			}
		}

	}

	private void writeChild(String parentPath, String myName, Element node,
			PrintStream ps) {

		String type = node.getTypeAsString();

		String myPath = parentPath + "/" + myName;

		if (node instanceof Resource) {

			ps.append("\"" + myPath + "\" [ label = \"" + myName + "\" ]\n");
			ps.append("\"" + parentPath + "\" -> \"" + myPath + "\"\n");

			// Iterate over all children of the hash, setting the name attribute
			// for each one.
			Resource resource = (Resource) node;
			for (Resource.Entry entry : resource) {
				String nameChild = entry.getKey().toString();
				writeChild(myPath, nameChild, entry.getValue(), ps);
			}

		} else {

			String s = ((Property) node).toString();
			String quote = ("string".equals(type)) ? "'" : "";
			s = fixString(s, quote);

			ps.append("\"" + myPath + "\" [ label = \"" + myName + "\\n" + s
					+ "\" ]\n");
			ps.append("\"" + parentPath + "\" -> \"" + myPath + "\"\n");
		}

	}

	private void writeHeader(String rootName, PrintStream ps) {
		ps.append("digraph \"" + rootName + "\" {\n" + "bgcolor = beige\n"
				+ "node [ color = black, shape = box, fontname=Helvetica ]\n"
				+ "edge [ color = black ]\n");
	}

	private void writeFooter(PrintStream ps) {
		ps.append("}\n");
	}

	private String fixString(String s, String quote) {

		// Start off with the quoting string.
		StringBuilder sb = new StringBuilder(quote);

		// Replace all new lines, carriage returns, etc. by a space.
		sb.append(s.replaceAll("[\\n\\r]+", " "));

		// Truncate the resulting string if necessary.
		if (sb.length() > 15) {
			sb.setLength(12);
			sb.append("...");
		}

		// Add the last quote.
		sb.append(quote);

		// Send back the adjusted string.
		return sb.toString();
	}

}
