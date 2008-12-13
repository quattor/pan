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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/output/FormatterTestsUtils.java $
 $Id: FormatterTestsUtils.java 2659 2008-01-07 14:48:07Z loomis $
 */

package org.quattor.pan.output;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.utils.TermFactory;

public class FormatterTestsUtils {

	// ------------------------------------------------------------------------
	//
	// Instance variables
	//
	// ------------------------------------------------------------------------
	private Vector<String> elementName;

	private Vector<String> elementParent;

	private Vector<Element> elementList;

	protected HashResource root;

	// ------------------------------------------------------------------------
	//
	// Constructors
	//
	// ------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 */
	public FormatterTestsUtils() {
		elementName = new Vector<String>();
		elementParent = new Vector<String>();
		elementList = new Vector<Element>();
		root = createRoot();
	}

	// ------------------------------------------------------------------------
	//
	// Instance methods
	//
	// ------------------------------------------------------------------------

	/**
	 * Creates the root of the configuration tree
	 */
	private HashResource createRoot() {
		HashResource cRoot;
		cRoot = new HashResource();
		int sizeElementName = elementName.size();
		int sizeElementParent = elementParent.size();
		int sizeElementList = elementList.size();

		if ((sizeElementName != 0) || (sizeElementParent != 0)
				|| (sizeElementList != 0)) {
			elementName.removeAllElements();
			elementParent.removeAllElements();
			elementList.removeAllElements();

		}

		elementName.addElement("profile");
		elementParent.addElement("none");
		elementList.addElement(cRoot);

		return cRoot;

	}

	/**
	 * Creates a child
	 * 
	 * @param name
	 *            the name of the child
	 * @param value
	 *            value of the child
	 * @param parent
	 *            the parent of the created child
	 * @param parentName
	 *            name of the parent
	 * @throws InvalidTermException 
	 */
	public void createChild(String name, Element value, Element parent,
			String parentName) throws InvalidTermException {

		if (parentName == null) {
			throw new IllegalArgumentException("parent may not be null");
		}

		if (elementName.contains(parentName) == false) {
			throw new IllegalArgumentException(
					"parent may exist to create a child");
		}

		boolean childIsPossible = verifyParentType(parent);

		if (childIsPossible == false) {
			throw new RuntimeException("This element may not have a child");
		}

		if (parent.isNlist() == true) {

			HashResource parentCast = (HashResource) parent;
			parentCast.put(TermFactory.create(name), value);
		}

		else if (parent.isList() == true) {

			ListResource parentCast = (ListResource) parent;
			parentCast.put(TermFactory.create(name), value);
		}

		elementName.addElement(name);
		elementParent.addElement(parentName);
		elementList.addElement(value);
	}

	/**
	 * Verification of the type of the element
	 * 
	 * @param elem
	 *            element to verify
	 */
	public boolean verifyParentType(Element elem) {
		if ((elem.isNlist() == true) || (elem.isList() == true)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Accessor to the root of the tree
	 */
	public HashResource getRoot() {

		return root;
	}

	/**
	 * Accessor to the vector containing the names of the elements
	 */
	public Vector<String> getElementNameList() {

		return elementName;
	}

	/**
	 * Creates an OutputStream
	 * 
	 * @param os
	 *            the OutputStream
	 * @param flagGzip
	 *            the flag indicating the necessity of a compression
	 */
	public static PrintStream createOutput(OutputStream os, boolean flagGzip)
			throws IOException {

		PrintStream ps;

		if (flagGzip) {
			ps = new PrintStream(new GZIPOutputStream(os));
		} else {
			ps = new PrintStream(os);
		}
		return ps;
	}

}
