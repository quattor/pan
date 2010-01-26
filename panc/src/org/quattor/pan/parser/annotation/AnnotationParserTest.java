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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/parser/PanParserTest.java $
 $Id: PanParserTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.parser.annotation;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;
import org.quattor.pan.annotation.Annotation.Entry;

public class AnnotationParserTest {

	protected List<Entry> getAnnotationMap(String s) throws ParseException {

		AnnotationParser parser = new AnnotationParser(new StringReader(s));
		return parser.annotation();
	}

	@Test
	public void testAnnotationParserConstructor() throws ParseException {
		new AnnotationParser(new StringReader("a=b"));
	}

	@Test
	public void testEmptyAnnotation() throws ParseException {

		String[] emptyAnnotations = { "", "\t", "\n", "\r", "\r\n" };

		for (String s : emptyAnnotations) {
			List<Entry> entries = getAnnotationMap(s);
			assertEquals(0, entries.size());
		}

	}

	@Test
	public void testSimpleValues() throws ParseException {

		String[] annotations = { "X=X", "X='X'", "X=\"X\"", "X =X", "X= X",
				"X = X", "X = X " };

		for (String s : annotations) {
			List<Entry> entries = getAnnotationMap(s);

			assertEquals(1, entries.size());

			Entry entry = entries.get(0);

			assertEquals("X", entry.getKey());
			assertEquals("X", entry.getValue());
		}

	}

	@Test
	public void testContinuedValues() throws ParseException {

		String[] annotations = { "X=X\\\nY", "X=\\\nXY", "X=XY\\\n",
				"X=XY\\\n\n" };

		for (String s : annotations) {
			List<Entry> entries = getAnnotationMap(s);

			assertEquals(1, entries.size());

			Entry entry = entries.get(0);

			assertEquals("X", entry.getKey());
			assertEquals("XY", entry.getValue());
		}

	}

	@Test
	public void testMultilineValues() throws ParseException {

		String[] annotations = { "X='X\nY'", "X=\"X\nY\"" };

		for (String s : annotations) {
			List<Entry> entries = getAnnotationMap(s);

			assertEquals(1, entries.size());

			Entry entry = entries.get(0);

			assertEquals("X", entry.getKey());
			assertEquals("X\nY", entry.getValue());
		}

	}

	@Test
	public void testMultipleValues() throws ParseException {

		List<Entry> entries = getAnnotationMap("X=X\nY=Z");

		assertEquals(2, entries.size());

		Entry entry = entries.get(0);

		assertEquals("X", entry.getKey());
		assertEquals("X", entry.getValue());

		entry = entries.get(1);

		assertEquals("Y", entry.getKey());
		assertEquals("Z", entry.getValue());
	}

	@Test
	public void testQuoteReplacement() throws ParseException {

		String[] annotations = { "X='X'''", "X='''X'", "X=\"X\"\"\"",
				"X=\"\"\"X\"" };
		String[] results = { "X'", "'X", "X\"", "\"X" };

		for (int i = 0; i < annotations.length; i++) {
			String s = annotations[i];
			List<Entry> entries = getAnnotationMap(s);

			assertEquals(1, entries.size());

			Entry entry = entries.get(0);

			assertEquals("X", entry.getKey());
			assertEquals(results[i], entry.getValue());
		}

	}

	@Test(expected = ParseException.class)
	public void testInvalidKey() throws ParseException {
		getAnnotationMap("a&=X");
	}

	@Test(expected = ParseException.class)
	public void testInvalidTextAfterString() throws ParseException {
		getAnnotationMap("X='X' invalid");
	}

	@Test(expected = ParseException.class)
	public void testUnterminatedString1() throws ParseException {
		getAnnotationMap("X='X");
	}

	@Test(expected = ParseException.class)
	public void testUnterminatedString2() throws ParseException {
		getAnnotationMap("X=\"X");
	}

}
