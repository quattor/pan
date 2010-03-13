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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/PropertyTestParent.java $
 $Id: PropertyTestParent.java 2828 2008-02-03 20:11:32Z loomis $
 */

package org.quattor.pan.dml.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

abstract public class PropertyTestParent extends ElementTestParent {

	@Test
	public void testIsProtected() {

		// All properties are invariant, so don't need to be protected. The
		// isProtected method should always return false.
		assertFalse(getTestElement().isProtected());
	}

	@Test
	public void testProtect() {

		// Properties are invariant so there is no need to physically duplicate
		// a property when protecting it. Check that the exact same object is
		// returned with protect().
		Element e = getTestElement();
		Element p = e.protect();
		assertTrue(e == p);
	}

	@Test(expected = EvaluationException.class)
	public void testRecursiveGet() throws InvalidTermException {
		Element e = getTestElement();
		Term[] terms = { TermFactory.create("OK") };
		e.rget(terms, 0, false, false);
	}

	@Test(expected = EvaluationException.class)
	public void testRecursivePut() throws InvalidTermException {
		Element e = getTestElement();
		Term[] terms = { TermFactory.create("OK") };
		StringProperty s = StringProperty.getInstance("OK");
		e.rput(terms, 0, s);
	}

}
