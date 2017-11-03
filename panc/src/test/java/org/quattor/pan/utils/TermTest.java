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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/TermTest.java $
 $Id: TermTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;

public class TermTest {

	@Test
	public void testLegalTerms() {
		List<String> paths = Arrays.asList("a", "_", "_0", "_1", "a+", "a-",
				"a.", "0", "1", "10", "17", "0xff", "0XFF");
		for (String s : paths) {
			try {
				TermFactory.create(s);
			} catch (EvaluationException ee) {
				fail("legal term threw an exception: " + ee.getMessage());
			}
		}
	}

	@Test
	public void testLegalNumericTerms() {
		List<Long> paths = Arrays.asList(0L, 123L);
		for (Long s : paths) {
			try {
				TermFactory.create(s);
			} catch (EvaluationException ee) {
				fail("legal numeric term threw an exception: "
						+ ee.getMessage());
			}
		}
	}

	@Test
	public void testLegalElementTerms() {
		List<Element> paths = Arrays.asList((Element) StringProperty
				.getInstance("ok"), StringProperty.getInstance("0"),
				StringProperty.getInstance("10"),
				LongProperty.getInstance(10L), LongProperty
						.getInstance((long) Integer.MAX_VALUE));
		for (Element s : paths) {
			try {
				TermFactory.create(s);
			} catch (EvaluationException ee) {
				fail("legal element term threw an exception: "
						+ ee.getMessage());
			}
		}
	}

	@Test
	public void testIllegalTerms() {
        /* 06: valid octal, 09: invalid octal, 00 and 017: previous valid in testLegalTerms */
		List<String> paths = Arrays.asList("", "06", "09", "00", "017", "-", "+",
				".", "-a", "+a", ".a");
		for (String s : paths) {
			try {
				TermFactory.create(s);
				fail("illegal term did not throw an exception (" + s + ")");
			} catch (EvaluationException ee) {
				// OK
			}
		}
	}

	@Test
	public void testIllegalNumericTerms() {
		List<Long> paths = Arrays.asList(-1L, ((long) Integer.MAX_VALUE) + 1L);
		for (Long s : paths) {
			try {
				TermFactory.create(s);
				fail("illegal numeric term did not throw an exception (" + s
						+ ")");
			} catch (EvaluationException ee) {
				// OK
			}
		}
	}

	@Test
	public void testIllegalElementTerms() {
		List<Element> paths = Arrays.asList((Element) StringProperty
				.getInstance("invalid/path"), StringProperty.getInstance("-1"),
				new ListResource(), LongProperty
						.getInstance(((long) Integer.MAX_VALUE) + 1));
		for (Element s : paths) {
			try {
				TermFactory.create(s);
				fail("illegal element term did not throw an exception");
			} catch (EvaluationException ee) {
				// OK
			}
		}
	}

	@Test
	public void testEquals() {

		Term t1 = TermFactory.create("alpha");
		Term t2 = TermFactory.create("alpha");
		assertTrue(t1.equals(t2));

		t1 = TermFactory.create("0");
		t2 = TermFactory.create("0");
		assertTrue(t1.equals(t2));

		t1 = TermFactory.create("123");
		t2 = TermFactory.create("123");
		assertTrue(t1.equals(t2));
	}

	@Test
	public void testToString() {
		Term t = TermFactory.create("alpha");
		assertTrue("alpha".equals(t.toString()));

		t = TermFactory.create("0");
		assertTrue("0".equals(t.toString()));

		t = TermFactory.create("123");
		assertTrue("123".equals(t.toString()));
	}

	@Test
	public void testOrdering() {

		Term a = TermFactory.create(10);
		Term b = TermFactory.create(100);
		assertTrue(a.compareTo(b) < 0);
		assertTrue(b.compareTo(a) > 0);

		a = TermFactory.create("alpha");
		b = TermFactory.create("beta");
		assertTrue(a.compareTo(b) < 0);
		assertTrue(b.compareTo(a) > 0);

		a = TermFactory.create(0);
		b = TermFactory.create("alpha");
		assertTrue(a.compareTo(b) < 0);
		assertTrue(b.compareTo(a) > 0);

		a = TermFactory.create(10);
		b = TermFactory.create("10");
		assertTrue(a.compareTo(b) == 0);

		a = TermFactory.create("alpha");
		b = TermFactory.create("alpha");
		assertTrue(a.compareTo(b) == 0);
	}

	@Test(expected = NullPointerException.class)
	public void testNullOrdering() {
		Term t = TermFactory.create("alpha");
		t.compareTo(null);
	}

}
