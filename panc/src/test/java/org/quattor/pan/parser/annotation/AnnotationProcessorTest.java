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
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.quattor.pan.annotation.Annotation;
import org.quattor.pan.annotation.Annotation.Entry;

public class AnnotationProcessorTest {

	@Test
	public void testValidDescriptions() {

		String[] descriptions = { "simple description", "not\na=b" };

		for (String d : descriptions) {
			List<Entry> entries = AnnotationProcessor.parseAsDesc(d);

			assertEquals(1, entries.size());

			Entry entry = entries.get(0);
			assertEquals("desc", entry.getKey());
			assertEquals(d, entry.getValue());
		}
	}

	@Test(expected = NullPointerException.class)
	public void testNullArgument() {
		AnnotationProcessor.parseAsDesc(null);
	}

	@Test
	public void testInvalidDescriptions() {

		String[] pairs = { "a=b", " a=b", "a = b", "\na=b", "\n  \n a=b",
				"a = b\nc = d", "a=b=c" };

		for (String s : pairs) {
			List<Entry> entries = AnnotationProcessor.parseAsDesc(s);
			assertNull(entries);
		}
	}

	@Test
	public void testHighLevelProcessing() throws ParseException {

		String[] inputs = { "@(simple description)", "@(a=b)", "@(a=b=c)" };

		Entry[] correctEntries = { new Entry("desc", "simple description"),
				new Entry("a", "b"), new Entry("a", "b=c") };

		for (int i = 0; i < inputs.length; i++) {
			String s = inputs[i];
			Annotation annotation = AnnotationProcessor.process(s);
			List<Entry> entries = annotation.getEntries();

			assertEquals(1, entries.size());

			Entry entry = entries.get(0);
			Entry correctEntry = correctEntries[i];

			assertEquals(correctEntry.getKey(), entry.getKey());
			assertEquals(correctEntry.getValue(), entry.getValue());
		}
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

}
