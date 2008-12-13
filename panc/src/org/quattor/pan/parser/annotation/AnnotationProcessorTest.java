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

import java.util.Map;

import org.junit.Test;

public class AnnotationProcessorTest {

	@Test
	public void testDescMap() {
		String s = "simple description";
		Map<String, String> map = AnnotationProcessor.parseAsDesc(s);
		assertTrue(map.size() == 1);
		assertTrue(map.containsKey("desc"));
		assertTrue("simple description".equals(map.get("desc")));
	}

	@Test(expected = NullPointerException.class)
	public void testNullArgument() {
		AnnotationProcessor.parseAsDesc(null);
	}

	@Test
	public void testKeyValuePairSyntax() {
		Map<String, String> map;

		map = AnnotationProcessor.parseAsDesc("a=b");
		assertTrue(map == null);

		map = AnnotationProcessor.parseAsDesc(" a=b");
		assertTrue(map == null);

		map = AnnotationProcessor.parseAsDesc("a = b");
		assertTrue(map == null);

		map = AnnotationProcessor.parseAsDesc("\na=b");
		assertTrue(map == null);

		map = AnnotationProcessor.parseAsDesc("\n  \n a=b");
		assertTrue(map == null);

		map = AnnotationProcessor.parseAsDesc("a = b\nc = d");
		assertTrue(map == null);

		map = AnnotationProcessor.parseAsDesc("a=b=c");
		assertTrue(map == null);

	}

	@Test
	public void testNotKeyValuePairSyntax() {

		Map<String, String> map;

		map = AnnotationProcessor.parseAsDesc("not\na=b");
		assertTrue(map.size() == 1);
		assertTrue(map.containsKey("desc"));
		assertTrue("not\na=b".equals(map.get("desc")));
	}

	@Test
	public void testHighLevelProcessing() throws ParseException {

		String s = "@(simple description)";
		Map<String, String> map = AnnotationProcessor.process(s);
		assertTrue(map.size() == 1);
		assertTrue(map.containsKey("desc"));
		assertTrue("simple description".equals(map.get("desc")));

		s = "@(a=b)";
		map = AnnotationProcessor.process(s);
		assertTrue(map.size() == 1);
		assertTrue(map.containsKey("a"));
		assertTrue("b".equals(map.get("a")));

		s = "@(a=b=c)";
		map = AnnotationProcessor.process(s);
		assertTrue(map.size() == 1);
		assertTrue(map.containsKey("a"));
		assertTrue("b=c".equals(map.get("a")));
	}

	@Test(expected = ParseException.class)
	public void testHighLevelProcessingErrors() throws ParseException {
		AnnotationProcessor.process("@(\na=b\nc&=d)");
	}

	@Test(expected = ParseException.class)
	public void testHighLevelProcessingErrors2() throws ParseException {
		AnnotationProcessor.process("@(a=b\nc&=d)");
	}

	@Test(expected = ParseException.class)
	public void testHighLevelError() throws ParseException {
		AnnotationProcessor.process("@(a&=b)");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnmodifiableMap() throws ParseException {
		Map<String, String> map = AnnotationProcessor.process("@(a=b)");
		assertTrue(map.size() == 1);
		assertTrue(map.containsKey("a"));
		assertTrue("b".equals(map.get("a")));

		map.put("bad", "bad");
	}

}
