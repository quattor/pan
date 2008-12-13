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

import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Map;

import org.junit.Test;

public class AnnotationParserTest {

	protected Map<String, String> getAnnotationMap(String s)
			throws ParseException {
		AnnotationParser parser = new AnnotationParser(new StringReader(s));
		return parser.annotation();
	}

	@Test
	public void testAnnotationParserConstructor() throws ParseException {
		String s = "key = value\n";
		new AnnotationParser(new StringReader(s));
	}

	@Test
	public void testEmptyAnnotation() throws ParseException {
		Map<String, String> map = getAnnotationMap("");
		assertTrue(map.size() == 0);

		map = getAnnotationMap("\n");
		assertTrue(map.size() == 0);

		map = getAnnotationMap("\r");
		assertTrue(map.size() == 0);

		map = getAnnotationMap("\r\n");
		assertTrue(map.size() == 0);
	}

	@Test
	public void testSimpleValues() throws ParseException {
		Map<String, String> map = getAnnotationMap("X=X");
		assertTrue(map.size() == 1);
		assertTrue("X".equals(map.get("X")));

		map = getAnnotationMap("X='X'");
		assertTrue(map.size() == 1);
		assertTrue("X".equals(map.get("X")));

		map = getAnnotationMap("X=\"X\"");
		assertTrue(map.size() == 1);
		assertTrue("X".equals(map.get("X")));

		map = getAnnotationMap("X =X");
		assertTrue(map.size() == 1);
		assertTrue("X".equals(map.get("X")));

		map = getAnnotationMap("X= X");
		assertTrue(map.size() == 1);
		assertTrue("X".equals(map.get("X")));

		map = getAnnotationMap("X = X");
		assertTrue(map.size() == 1);
		assertTrue("X".equals(map.get("X")));

		map = getAnnotationMap("X = X ");
		assertTrue(map.size() == 1);
		assertTrue("X ".equals(map.get("X")));
	}

	@Test
	public void testContinuedValues() throws ParseException {
		Map<String, String> map = getAnnotationMap("X=X\\\nY");
		assertTrue(map.size() == 1);
		assertTrue("XY".equals(map.get("X")));

		map = getAnnotationMap("X=\\\nXY");
		assertTrue(map.size() == 1);
		assertTrue("XY".equals(map.get("X")));

		map = getAnnotationMap("X=XY\\\n");
		assertTrue(map.size() == 1);
		assertTrue("XY".equals(map.get("X")));

		map = getAnnotationMap("X=XY\\\n\n");
		assertTrue(map.size() == 1);
		assertTrue("XY".equals(map.get("X")));
	}

	@Test
	public void testMultilineValues() throws ParseException {
		Map<String, String> map = getAnnotationMap("X='X\nY'");
		assertTrue(map.size() == 1);
		assertTrue("X\nY".equals(map.get("X")));

		map = getAnnotationMap("X=\"X\nY\"");
		assertTrue(map.size() == 1);
		assertTrue("X\nY".equals(map.get("X")));
	}

	@Test
	public void testMultipleValues() throws ParseException {
		Map<String, String> map = getAnnotationMap("X=X\nY=Z");
		assertTrue(map.size() == 2);
		assertTrue("X".equals(map.get("X")));
		assertTrue("Z".equals(map.get("Y")));
	}

	@Test
	public void testQuoteReplacement() throws ParseException {
		Map<String, String> map = getAnnotationMap("X='X'''");
		assertTrue(map.size() == 1);
		assertTrue("X'".equals(map.get("X")));

		map = getAnnotationMap("X='''X'");
		assertTrue(map.size() == 1);
		assertTrue("'X".equals(map.get("X")));

		map = getAnnotationMap("X=\"X\"\"\"");
		assertTrue(map.size() == 1);
		assertTrue("X\"".equals(map.get("X")));

		map = getAnnotationMap("X=\"\"\"X\"");
		assertTrue(map.size() == 1);
		assertTrue("\"X".equals(map.get("X")));
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
