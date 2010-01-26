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

package org.quattor.pan.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.quattor.pan.annotation.Annotation.validKey;
import static org.quattor.pan.annotation.Annotation.validName;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.quattor.pan.annotation.Annotation.Entry;
import org.quattor.pan.parser.annotation.AnnotationProcessor;
import org.quattor.pan.parser.annotation.ParseException;

public class AnnotationTest {

	String[] validKeys = { "a", "A", "_", "aa", "AA", "__", "a-", "A-", "_-",
			"a.", "A.", "_.", "A-.A" };

	String[] invalidKeys = { "-", ".", "$", "-a", ".a", "$a", "a$", "XML",
			"xml", "Xml", "xMl", "xmL", "XMLa" };

	String emptyString = "";

	@Test
	public void checkValidKeys() {
		for (String key : validKeys) {
			assertTrue(validKey(key));
		}
	}

	@Test
	public void checkInvalidKeys() {
		for (String key : invalidKeys) {
			assertFalse(validKey(key));
		}
		assertFalse(validKey(emptyString));
	}

	@Test
	public void checkValidNames() {
		for (String key : validKeys) {
			assertTrue(validName(key));
		}
		assertTrue(validName(emptyString));
	}

	@Test
	public void checkInvalidNames() {
		for (String key : invalidKeys) {
			assertFalse(validName(key));
		}
	}

	@Test
	public void checkValidEntries() {
		for (String key : validKeys) {
			new Entry(key, "ok");
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkIllegalNullValue() {
		new Entry("ok", null);
	}

	@Test
	public void checkInvalidEntries() {
		for (String key : invalidKeys) {
			try {
				new Entry(key, "ok");
				fail("invalid key (" + key
						+ ") did not cause exception in Entry constructor");
			} catch (IllegalArgumentException e) {
				// OK
			}
		}
	}

	@Test
	public void checkNullsInConstructor() {
		Annotation annotation = new Annotation(null, null);
		List<Entry> entries = annotation.getEntries();

		assertEquals("", annotation.getName());
		assertTrue(annotation.isAnonymous());
		assertNotNull(entries);
		assertEquals(0, entries.size());
	}

	@Test
	public void checkNotAnonymous() {
		Annotation annotation = new Annotation("ok", null);

		assertFalse(annotation.isAnonymous());
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkInvalidAnnotationNameThrowsException() {
		new Annotation("-", null);
	}

	@Test
	public void checkEntries() {

		List<Entry> entriesIn = new LinkedList<Entry>();
		for (String key : validKeys) {
			new Entry(key, key);
		}
		Annotation annotation = new Annotation("", entriesIn);

		List<Entry> entriesOut = annotation.getEntries();

		assertEquals(entriesIn.size(), entriesOut.size());

		for (int i = 0; i < entriesOut.size(); i++) {
			String keyIn = validKeys[i];
			Entry entry = entriesOut.get(i);

			assertEquals(keyIn, entry.getKey());
			assertEquals(keyIn, entry.getValue());
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnmodifiableList() throws ParseException {

		Annotation annotation = AnnotationProcessor.process("@(a=b)");
		List<Entry> entries = annotation.getEntries();

		assertEquals(1, entries.size());

		Entry entry = entries.get(0);

		assertEquals("a", entry.getKey());
		assertEquals("b", entry.getValue());

		entries.add(new Entry("bad", "bad"));
	}

}
