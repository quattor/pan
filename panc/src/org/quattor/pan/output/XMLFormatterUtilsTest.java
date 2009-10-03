/*
 Copyright (c) 2008 Charles A. Loomis, Jr, Cedric Duprilot, and
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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/TermTest.java $
 $Id: TermTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.output;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;

public class XMLFormatterUtilsTest {

	// This is a utility function to write the output to a byte array and then
	// to read it back in again to check that it is well-formed.
	public static void writeAndReadAsXML(Formatter formatter, Element root)
			throws TransformerException, IOException {

		// Write the output to a byte array.
		byte[] buffer = null;
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			formatter.write(root, "profile", ps);
		} finally {
			if (baos != null) {
				baos.close();
				buffer = baos.toByteArray();
			}
		}

		// Recover the bytes that were written and create a Source for an XML
		// transformation. The easiest way to check that the input is
		// well-formed is just to use an identity transformation.
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		StreamSource source = new StreamSource(bais);

		// Create a sink for the transformer output.
		baos = new ByteArrayOutputStream();
		StreamResult sink = new StreamResult(baos);

		// Actually do the transformation. Any problems will result in an
		// exception being thrown.
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.transform(source, sink);
	}

	@Test
	public void checkValidXMLNames() {

		String[] strings = { "Asuffix", "asuffix", "_suffix", "\u00C0suffix",
				"A0suffix", "A.suffix", "A-suffix" };
		for (String s : strings) {
			assertTrue(XMLFormatterUtils.isValidXMLName(s));
		}
	}

	@Test
	public void checkInvalidXMLNames() {

		String[] xs = { "x", "X" };
		String[] ms = { "m", "M" };
		String[] ls = { "l", "L" };
		String suffix = "DummyValue";

		// Everything starting with "xml" is reserved in the specification. The
		// case of the letters is not important.
		for (String x : xs) {
			for (String m : ms) {
				for (String l : ls) {
					String s = x + m + l + suffix;
					assertFalse(XMLFormatterUtils.isValidXMLName(s));
				}
			}
		}

		// Check other invalid names.
		String[] strings = { "", ".suffix", "-suffix", "0suffix",
				"\u0300suffix" };
		for (String s : strings) {
			assertFalse(XMLFormatterUtils.isValidXMLName(s));
		}
	}

	@Test
	public void checkValidCharacters() {
		int[] codepoints = { 0x9, 0xA, 0xD, 0x20, 0xD7FF, 0xE000, 0xFFFD,
				0x100000, 0x10FFFF };
		for (int i : codepoints) {
			assertTrue(XMLFormatterUtils.isValidXMLCharacter(i));
		}
	}

	@Test
	public void checkInvalidCharacters() {
		int[] codepoints = { 0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0xB,
				0xC, 0xE, 0xF, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
				0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0xD800, 0xDFFF,
				0xFFFF, 0x110000 };
		for (int i : codepoints) {
			assertFalse(XMLFormatterUtils.isValidXMLCharacter(i));
		}
	}

	@Test
	public void checkValidString() {
		String[] strings = { "\u0009", "\uFFFA", "\uD800\uDC00", "\uDBFF\uDFFF" };
		for (String s : strings) {
			assertTrue(XMLFormatterUtils.isValidXMLString(s));
		}
	}

	@Test
	public void checkInvalidString() {
		String[] strings = { "\u0000", "\uD801", "\uDC00", "\uDBFF" };
		for (String s : strings) {
			assertFalse(XMLFormatterUtils.isValidXMLString(s));
		}
	}

	@Test
	public void encodeEmptyStringAsXMLName() {
		assertTrue("_".equals(XMLFormatterUtils.encodeAsXMLName("")));
	}

	@Test
	public void checkEncodedXMLNames() {
		assertTrue("_00".equals(XMLFormatterUtils.encodeAsXMLName("\u0000")));
		assertTrue("_3031".equals(XMLFormatterUtils.encodeAsXMLName("01")));
		assertTrue("_4142".equals(XMLFormatterUtils.encodeAsXMLName("AB")));
	}

}
