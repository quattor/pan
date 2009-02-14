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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/ListResourceTest.java $
 $Id: ListResourceTest.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.dml.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

public class ListResourceTest {

	@Test
	public void testConstructor() {
		ListResource list = new ListResource();
		assertTrue(list.size() == 0);
	}

	@Test
	public void testProtected() throws InvalidTermException {

		ListResource list = new ListResource();
		assertFalse(list.isProtected());
	}

	@Test
	public void testProtect() {

		// A new object should be returned. It should be protected.
		ListResource list = new ListResource();
		Element p = list.protect();
		assertFalse(list == p);
		assertTrue(p.isProtected());
	}

	@Test
	public void testPutGet() throws InvalidTermException {
		ListResource list = new ListResource();

		Term index = TermFactory.create(0L);
		long va = 100L;
		LongProperty lprop = LongProperty.getInstance(va);
		list.put(index, lprop);
		Element r1 = list.get(index);

		// Check that all's OK.
		assertTrue(r1 instanceof LongProperty);
		assertTrue(r1 == lprop);
	}

	@Test
	public void testDelete() throws InvalidTermException {
		ListResource list = new ListResource();

		Term index = TermFactory.create(0L);
		long va = 100L;
		LongProperty lprop = LongProperty.getInstance(va);

		list.put(index, lprop);
		list.put(index, null);
		assertTrue(list.size() == 0);

		list.put(index, lprop);
		list.put(index, Null.VALUE);
		assertTrue(list.size() == 0);
	}

	@Test
	public void testListPadding() throws InvalidTermException {
		ListResource list = new ListResource();

		long size = 4;
		Term index = TermFactory.create(size - 1L);

		long va = 100L;
		LongProperty lprop = LongProperty.getInstance(va);

		// Insert the value into the list.
		list.put(index, lprop);

		// Check that the size is correct.
		assertTrue(list.size() == size);

		// Check that all of the missing values have been padded with undef.
		for (int i = 0; i < size - 1; i++) {
			Element element = list.get(TermFactory.create((long) i));
			assertTrue(element instanceof Undef);
		}

	}

	@Test
	public void testNullGet() throws InvalidTermException {
		ListResource list = new ListResource();
		assertTrue(list.get(TermFactory.create(0L)) == null);
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidReplacement() throws InvalidTermException {

		ListResource list = new ListResource();

		Term index = TermFactory.create(0L);
		list.put(index, LongProperty.getInstance(1));
		list.put(index, DoubleProperty.getInstance(1.0));
	}

	@Test
	public void testAppend() throws InvalidTermException {

		// Indexes into the array.
		Term zero = TermFactory.create(0L);
		Term one = TermFactory.create(1L);

		// Elements to use for the values.
		Element filler = StringProperty.getInstance("FILLER");
		Element value = StringProperty.getInstance("OK");

		// Create the resource and add the filler value.
		ListResource list = new ListResource();
		list.put(zero, filler);

		// Try the append and ensure that it ended up at the end of the list.
		list.append(value);
		assertTrue(list.size() == 2);
		assertTrue(value.equals(list.get(one)));
	}

	@Test
	public void testPrepend() throws InvalidTermException {

		// Index into the array.
		Term zero = TermFactory.create(0L);

		// Elements to use for the values.
		Element filler = StringProperty.getInstance("FILLER");
		Element value = StringProperty.getInstance("OK");

		// Create the resource and add the filler value.
		ListResource list = new ListResource();
		list.put(zero, filler);

		// Try the prepend and ensure that it ended up at the beginning of the
		// list.
		list.prepend(value);
		assertTrue(list.size() == 2);
		assertTrue(value.equals(list.get(zero)));
	}

}
