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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/output/PanTxtFormatter.java $
 $Id: PanTxtFormatter.java 3597 2008-08-17 09:08:57Z loomis $
 */

package org.quattor.pan.output;

import java.io.PrintWriter;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.Resource;

abstract public class PanTxtFormatter implements Formatter {

	/**
	 * Reads the configuration tree and writes it in an xml file.
	 * 
	 * @param root
	 *            root element of the tree
	 * @param rootName
	 *            name of the root element
	 * @param ps
	 *            the PrintWriter
	 */
	public abstract void write(Element root, String rootName, PrintWriter ps);

	/**
	 * Reads each child.
	 * 
	 * @param element
	 *            element to treat
	 * @param ps
	 *            PrintWriter
	 * 
	 * @param level
	 *            Level of the node in the tree
	 * 
	 * @param name
	 *            name of the element
	 */
	public void readChild(Element element, PrintWriter ps, int level,
			String name) {
		String nbTab = tabMaker(level);

		if (element instanceof Resource) {
			Resource resource = (Resource) element;

			writeBegin(ps, nbTab, name, level, element.getTypeAsString());

			for (Resource.Entry entry : resource) {
				Property key = entry.getKey();
				Element value = entry.getValue();

				level++;
				readChild(value, ps, level, key.toString());
				level--;
			}

			writeEnd(ps, nbTab, element.getTypeAsString());

		} else {
			Property elem = (Property) element;
			writeProperties(ps, nbTab, name, element.getTypeAsString(), elem
					.toString());
		}
	}

	/**
	 * Writes in the PrintWriter.
	 * 
	 * @param ps
	 *            PrintWriter
	 * @param nbTab
	 *            number of tab to write before text
	 * @param name
	 *            name of the element
	 * @param level
	 *            level of the node in the tree
	 * @param type
	 *            type of the element
	 */
	public abstract void writeBegin(PrintWriter ps, String nbTab, String name,
			int level, String type);

	/**
	 * Writes end tags in the PrintWriter for PanFormatter.
	 * 
	 * @param ps
	 *            PrintWriter
	 * @param nbTab
	 *            number of tab to write before text
	 * @param type
	 *            type of the element
	 */
	public abstract void writeEnd(PrintWriter ps, String nbTab, String type);

	/**
	 * Writes in the PrintWriter.
	 * 
	 * @param ps
	 *            PrintWriter
	 * @param nbTab
	 *            number of tab to write before text
	 * @param name
	 *            name of the element
	 * @param type
	 *            type of the element
	 * @param value
	 *            value of the element
	 */
	public abstract void writeProperties(PrintWriter ps, String nbTab,
			String name, String type, String value);

	/**
	 * Calculates the number of tabulations to write at the begining of a line
	 * 
	 * @param n
	 *            number of tabulations
	 */
	public String tabMaker(int n) {
		StringBuilder buf = new StringBuilder("");

		for (int i = 1; i <= n; i++) {
			buf.append("\t");
		}
		String tab = buf.toString();
		return tab;
	}
}
