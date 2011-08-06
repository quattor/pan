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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/HashResourceTest.java $
 $Id: HashResourceTest.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.dml.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

public class HashResourceTest {

	@Test
	public void testConstructor() {
		HashResource hash = new HashResource();
		assertTrue(hash.size() == 0);
	}

	@Test
	public void testProtected() throws InvalidTermException {

		HashResource hash = new HashResource();
		assertFalse(hash.isProtected());
	}

	@Test
	public void testProtect() {

		// A new object should be returned. It should be protected.
		HashResource hash = new HashResource();
		Element p = hash.protect();
		assertFalse(hash == p);
		assertTrue(p.isProtected());
	}

	@Test
	public void testDelete() throws InvalidTermException {
		HashResource hash = new HashResource();

		Term index = TermFactory.create("a");
		long va = 100L;
		LongProperty lprop = LongProperty.getInstance(va);

		hash.put(index, lprop);
		hash.put(index, null);
		assertTrue(hash.size() == 0);

		hash.put(index, lprop);
		hash.put(index, Null.VALUE);
		assertTrue(hash.size() == 0);
	}

	@Test
	public void testPutGet() throws InvalidTermException {
		HashResource hash = new HashResource();

		Term index = TermFactory.create("a");
		long va = 100L;
		LongProperty lprop = LongProperty.getInstance(va);
		hash.put(index, lprop);
		Element r1 = hash.get(index);

		// Check that all's OK.
		assertTrue(r1 instanceof LongProperty);
		assertTrue(r1 == lprop);
	}

	@Test
	public void testNullGet() throws InvalidTermException {
		HashResource hash = new HashResource();
		assertTrue(hash.get(TermFactory.create("a")) == null);
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidReplacement() throws InvalidTermException {

		HashResource hash = new HashResource();

		Term index = TermFactory.create("a");
		hash.put(index, LongProperty.getInstance(1));
		hash.put(index, DoubleProperty.getInstance(1.0));
	}

}
