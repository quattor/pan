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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/output/XmlDBFormatterTest.java $
 $Id: XmlDBFormatterTest.java 3848 2008-10-31 09:29:15Z loomis $
 */

package org.quattor.pan.output;

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

public class XmlDBFormatterTest {

	private static final Pattern p = Pattern.compile("^[\\w\\.-]+$");

	@Test
	public void checkInvalidStringIsEncoded() throws InvalidTermException,
			TransformerException, IOException {

		Term t = TermFactory.create("element");
		StringProperty zerostring = StringProperty.getInstance("\u0000");

		HashResource root = new HashResource();
		root.put(t, zerostring);

		Formatter formatter = XmlDBFormatter.getInstance();
		XMLFormatterUtilsTest.writeAndReadAsXML(formatter, root);
	}

	@Test
	public void testXmlDBFormatter() throws IOException, InvalidTermException {

		FormatterTestsUtils tree = new FormatterTestsUtils();
		HashResource rootTree = tree.getRoot();
		Vector<String> elementName = tree.getElementNameList();
		String rootName = elementName.elementAt(0);

		String child1Name = "chil+d1";
		StringProperty child1Value = StringProperty.getInstance("first child");

		String child2Name = "child2";
		HashResource child2Value = new HashResource();

		String child3Name = "chi+ld3";
		ListResource child3Value = new ListResource();

		String child15Name = "2";
		HashResource child15Value = new HashResource();

		String child16Name = "child16";
		ListResource child16Value = new ListResource();

		String child17Name = "0";
		StringProperty child17Value = StringProperty.getInstance("17'th child");

		String child18Name = "1";
		StringProperty child18Value = StringProperty.getInstance("18'th child");

		String child4Name = "child4";
		LongProperty child4Value = LongProperty.getInstance(256);

		String child5Name = "0";
		StringProperty child5Value = StringProperty.getInstance("fifth child");

		String child6Name = "1";
		StringProperty child6Value = StringProperty.getInstance("sixth child");

		String child7Name = "4";
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
		tree.createChild(child15Name, child15Value, child3Value, child3Name);
		tree.createChild(child16Name, child16Value, child15Value, child15Name);
		tree.createChild(child17Name, child17Value, child16Value, child16Name);
		tree.createChild(child18Name, child18Value, child16Value, child16Name);
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
		Formatter s = XmlDBFormatter.getInstance();

		PrintStream pos = FormatterTestsUtils.createOutput(baos, optionzip);
		s.write(rootTree, rootName, pos);
		pos.close();

		/***********************************************************************
		 * for the xmldb format
		 **********************************************************************/

		String expression11 = "/" + rootName + "[@format=\"xmldb\"]";
		expression.addElement(expression11);

		String expression13 = "/" + rootName + "/" + checkName(child1Name)
				+ "[@type=\"string\"]";
		expression.addElement(expression13);

		String expression14 = "/" + rootName + "/" + checkName(child2Name);
		expression.addElement(expression14);

		String expression15 = "/" + rootName + "/" + checkName(child2Name)
				+ "/" + checkName(child3Name)
				+ "[(@type=\"string\")and(@list=\"1\")]";
		expression.addElement(expression15);

		String expression16 = "/" + rootName + "/" + checkName(child2Name)
				+ "/" + checkName(child3Name)
				+ "[(@type=\"long\")and(@list=\"1\")]";
		expression.addElement(expression16);

		String expression17 = "/" + rootName + "/" + checkName(child2Name)
				+ "/" + checkName(child3Name) + "[@list=\"1\"]";
		expression.addElement(expression17);

		String expression18 = "/" + rootName + "/" + checkName(child2Name)
				+ "/" + checkName(child3Name) + "/" + checkName(child3Name)
				+ "[(@type=\"string\")and(@list=\"2\")]";
		expression.addElement(expression18);

		String expression19 = "/" + rootName + "/" + checkName(child2Name)
				+ "/" + checkName(child3Name) + "/" + checkName(child3Name)
				+ "[@list=\"2\"]";
		expression.addElement(expression19);

		String expression20 = "/profile/" + child2Name + "/"
				+ checkName(child3Name) + "/" + checkName(child3Name) + "/"
				+ checkName(child3Name) + "[(@type=\"long\")and(@list=\"3\")]";
		expression.addElement(expression20);

		String expression21 = "/profile/" + child2Name + "/"
				+ checkName(child3Name) + "/" + checkName(child3Name) + "/"
				+ checkName(child3Name)
				+ "[(@type=\"string\")and(@list=\"3\")]";
		expression.addElement(expression21);

		String expression22 = "/" + rootName + "/" + checkName(child2Name)
				+ "/" + checkName(child3Name) + "/" + checkName(child3Name)
				+ "[(@type=\"long\")and(@list=\"2\")]";
		expression.addElement(expression22);

		String expression12 = "/" + rootName + "/" + checkName(child2Name)
				+ "/" + checkName(child4Name) + "[@type=\"long\"]";
		expression.addElement(expression12);

		String expression1 = "/" + rootName + "/" + checkName(child2Name) + "/"
				+ checkName(child3Name) + "[@list=\"1\"]";
		expression.addElement(expression1);

		String expression2 = "/" + rootName + "/" + checkName(child2Name) + "/"
				+ checkName(child3Name) + "/" + checkName(child16Name)
				+ "[(@type=\"string\")and(@list=\"1\")]";
		expression.addElement(expression2);

		String expression3 = "/" + rootName + "/" + checkName(child2Name) + "/"
				+ checkName(child3Name) + "/" + checkName(child16Name)
				+ "[(@type=\"string\")and(@list=\"1\")]";
		expression.addElement(expression3);

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
