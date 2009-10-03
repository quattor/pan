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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/output/PanFormatterTest.java $
 $Id: PanFormatterTest.java 3848 2008-10-31 09:29:15Z loomis $
 */

package org.quattor.pan.output;

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;
import org.xml.sax.InputSource;

public class PanFormatterTest {

	@Test
	public void checkInvalidStringIsEncoded() throws InvalidTermException,
			TransformerException, IOException {

		Term t = TermFactory.create("element");
		StringProperty zerostring = StringProperty.getInstance("\u0000");

		HashResource root = new HashResource();
		root.put(t, zerostring);

		Formatter formatter = PanFormatter.getInstance();
		XMLFormatterUtilsTest.writeAndReadAsXML(formatter, root);
	}

	@Test
	public void testPanFormatter() throws IOException, InvalidTermException {

		FormatterTestsUtils tree = new FormatterTestsUtils();
		HashResource rootTree = tree.getRoot();
		Vector<String> elementName = tree.getElementNameList();
		String rootName = elementName.elementAt(0);

		String child1Name = "child1";
		StringProperty child1Value = StringProperty.getInstance("first child");

		String child2Name = "child2";
		HashResource child2Value = new HashResource();

		String child3Name = "child3";
		ListResource child3Value = new ListResource();

		String child4Name = "child4";
		LongProperty child4Value = LongProperty.getInstance(256);

		String child5Name = "0";
		StringProperty child5Value = StringProperty.getInstance("fifth child");

		String child6Name = "1";
		StringProperty child6Value = StringProperty.getInstance("sixth child");

		String child7Name = "2";
		LongProperty child7Value = LongProperty.getInstance(256);

		String child8Name = "3";
		ListResource child8Value = new ListResource();

		String child9Name = "0";
		StringProperty child9Value = StringProperty.getInstance("nineth child");

		String child10Name = "1";
		StringProperty child10Value = StringProperty.getInstance("tenth child");

		String child11Name = "3";
		LongProperty child11Value = LongProperty.getInstance(368);

		String child12Name = "2";
		ListResource child12Value = new ListResource();

		String child13Name = "1";
		StringProperty child13Value = StringProperty
				.getInstance("eleventh child");

		String child14Name = "0";
		LongProperty child14Value = LongProperty.getInstance(214);

		tree.createChild(child1Name, child1Value, rootTree, rootName);
		tree.createChild(child2Name, child2Value, rootTree, rootName);
		tree.createChild(child3Name, child3Value, child2Value, child2Name);
		tree.createChild(child5Name, child5Value, child3Value, child3Name);
		tree.createChild(child6Name, child6Value, child3Value, child3Name);
		tree.createChild(child7Name, child7Value, child3Value, child3Name);
		tree.createChild(child8Name, child8Value, child3Value, child3Name);
		tree.createChild(child9Name, child9Value, child8Value, child8Name);
		tree.createChild(child10Name, child10Value, child8Value, child8Name);
		tree.createChild(child12Name, child12Value, child8Value, child8Name);
		tree.createChild(child13Name, child13Value, child12Value, child12Name);
		tree.createChild(child14Name, child14Value, child12Value, child12Name);
		tree.createChild(child11Name, child11Value, child8Value, child8Name);
		tree.createChild(child4Name, child4Value, child2Value, child2Name);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Vector<String> expression = new Vector<String>();
		boolean optionzip = false;
		Boolean value = null;

		// Creation of the output in a ByteArrayOutputStream
		Formatter s = PanFormatter.getInstance();

		PrintStream pos = FormatterTestsUtils.createOutput(baos, optionzip);
		s.write(rootTree, rootName, pos);
		pos.close();

		/***********************************************************************
		 * for the pan format
		 **********************************************************************/
		String expression1 = "/nlist[(@name=\"" + rootName
				+ "\")and(@format=\"pan\")]";
		expression.addElement(expression1);

		String expression2 = "/nlist[(@name=\"" + rootName
				+ "\")and(@format=\"pan\")]/string[@name=\"" + child1Name
				+ "\"]";
		expression.addElement(expression2);

		String expression3 = "/nlist[(@name=\"" + rootName
				+ "\")and(@format=\"pan\")]/nlist[@name=\"" + child2Name
				+ "\"]";
		expression.addElement(expression3);

		String expression4 = "/nlist[(@name=\"" + rootName
				+ "\")and(@format=\"pan\")]/nlist[@name=\"" + child2Name
				+ "\"]/list[@name=\"" + child3Name + "\"]";
		expression.addElement(expression4);

		String expression5 = "/nlist[(@name=\"" + rootName
				+ "\")and(@format=\"pan\")]/nlist[@name=\"" + child2Name
				+ "\"]/list[@name=\"" + child3Name + "\"]/string";
		expression.addElement(expression5);

		String expression6 = "/nlist[(@name=\"" + rootName
				+ "\")and(@format=\"pan\")]/nlist[@name=\"" + child2Name
				+ "\"]/list[@name=\"" + child3Name + "\"]/long";
		expression.addElement(expression6);

		String expression7 = "/nlist[(@name=\"" + rootName
				+ "\")and(@format=\"pan\")]/nlist[@name=\"" + child2Name
				+ "\"]/list[@name=\"" + child3Name + "\"]/list";
		expression.addElement(expression7);

		String expression8 = "/nlist[(@name=\"" + rootName
				+ "\")and(@format=\"pan\")]/nlist[@name=\"" + child2Name
				+ "\"]/list[@name=\"" + child3Name + "\"]/list/long";
		expression.addElement(expression8);

		String expression9 = "/nlist[(@name=\"" + rootName
				+ "\")and(@format=\"pan\")]/nlist[@name=\"" + child2Name
				+ "\"]/list[@name=\"" + child3Name + "\"]/list/string";
		expression.addElement(expression9);

		String expression10 = "/nlist[(@name=\"" + rootName
				+ "\")and(@format=\"pan\")]/nlist[@name=\"" + child2Name
				+ "\"]/long[@name=\"" + child4Name + "\"]";
		expression.addElement(expression10);

		// Test of the expressions
		for (String exp : expression) {
			value = evaluer(baos, exp, optionzip);
			assertFalse("The expression: \"" + exp
					+ "\" has a wrong syntax in the tested template", !value);
		}
	}

	public static Boolean evaluer(ByteArrayOutputStream baos,
			String expression, boolean optionzip) throws IOException {
		Boolean b = null;
		GZIPInputStream gzi = null;
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		InputSource inputSource = null;
		try {
			// creation of the source
			if (optionzip) {
				gzi = new GZIPInputStream(bais);
				inputSource = new InputSource(gzi);
			} else {
				inputSource = new InputSource(bais);
			}

			// creation of the XPath
			XPathFactory fabrique = XPathFactory.newInstance();
			XPath xpath = fabrique.newXPath();
			// evaluation of the XPath expression
			XPathExpression exp = xpath.compile(expression);
			b = (Boolean) exp.evaluate(inputSource, XPathConstants.BOOLEAN);

		} catch (XPathExpressionException xpee) {
			xpee.printStackTrace();
		}
		return b;
	}

}
