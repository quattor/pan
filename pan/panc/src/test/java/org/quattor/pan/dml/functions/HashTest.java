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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/HashTest.java $
 $Id: HashTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.utils.TermFactory;

public class HashTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Hash.class);
	}

	@Test
	public void testValueInHash() throws SyntaxException, InvalidTermException {

		// Check long value.
		Element r1 = runDml(Hash.getInstance(null, StringProperty
				.getInstance("a"), LongProperty.getInstance(1L)));

		// Check result.
		assertTrue(r1 instanceof HashResource);
		HashResource s1 = (HashResource) r1;
		Element sresult = s1.get(TermFactory.create("a"));
		assertTrue(sresult instanceof LongProperty);
		LongProperty r2 = (LongProperty) sresult;
		assertTrue(1L == r2.getValue().longValue());
	}

	@Test(expected = SyntaxException.class)
	public void testInvalidKey() throws SyntaxException {
		runDml(Hash.getInstance(null, LongProperty.getInstance(2L),
				LongProperty.getInstance(1L)));
	}

	@Test(expected = SyntaxException.class)
	public void testInvalidNumberOfArguments() throws SyntaxException {
		Hash.getInstance(null, StringProperty.getInstance("one"));
	}

}
