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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/ProtectedHashResourceTest.java $
 $Id: ProtectedHashResourceTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

public class ProtectedHashResourceTest {

	@Test
	public void testProtected() throws InvalidTermException {

		HashResource baseHash = new HashResource();
		Term index = TermFactory.create("a");
		baseHash.put(index, StringProperty.getInstance("OK"));

		ProtectedHashResource hash = new ProtectedHashResource(baseHash);

		// The protected hash must return a flag indicating this.
		assertFalse(baseHash.isProtected());
		assertTrue(hash.isProtected());
	}

	@Test
	public void testProtect() {

		// The same object should be returned. It should be protected.
		HashResource hash = new HashResource();
		Element p = hash.protect();
		assertFalse(hash == p);
		assertFalse(hash.isProtected());
		assertTrue(p.isProtected());
	}

	@Test
	public void testGet() throws InvalidTermException {

		HashResource baseHash = new HashResource();
		Term index = TermFactory.create("a");
		baseHash.put(index, StringProperty.getInstance("OK"));

		ProtectedHashResource hash = new ProtectedHashResource(baseHash);

		Element r1 = hash.get(index);

		// Check that all's OK.
		assertTrue(r1 instanceof StringProperty);
		assertTrue("OK".equals(((StringProperty) r1).getValue()));
	}

	@Test(expected = CompilerError.class)
	public void testIllegalWrite() throws InvalidTermException {

		HashResource baseHash = new HashResource();
		Term index = TermFactory.create("a");
		baseHash.put(index, StringProperty.getInstance("OK"));

		ProtectedHashResource hash = new ProtectedHashResource(baseHash);

		index = TermFactory.create("b");
		hash.put(index, StringProperty.getInstance("BAD"));
	}

	@Test(expected = CompilerError.class)
	public void testIllegalWrite2() throws InvalidTermException {

		HashResource baseHash = new HashResource();
		Term index = TermFactory.create("a");
		baseHash.put(index, StringProperty.getInstance("OK"));

		ProtectedHashResource hash = new ProtectedHashResource(baseHash);

		List<Term> terms = new ArrayList<Term>();
		terms.add(index);

		hash.rput(terms, 0, StringProperty.getInstance("BAD"));
	}

}
