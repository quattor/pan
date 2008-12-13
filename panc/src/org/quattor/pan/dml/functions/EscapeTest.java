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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/EscapeTest.java $
 $Id: EscapeTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.SyntaxException;

public class EscapeTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Escape.class);
	}

	@Test
	public void encodeEmptyString() throws SyntaxException {

		// Execute operations.
		Element r1 = runDml(Escape.getInstance(null, StringProperty
				.getInstance("")));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		String sresult = ((StringProperty) r1).getValue();
		assertTrue("_".equals(sresult));
	}

	@Test
	public void encodeAlphanumericString() throws SyntaxException {

		// Execute operations.
		Element r1 = runDml(Escape.getInstance(null, StringProperty
				.getInstance("abc123")));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		String sresult = ((StringProperty) r1).getValue();
		assertTrue("abc123".equals(sresult));

		// Execute operations.
		r1 = runDml(Escape.getInstance(null, StringProperty
				.getInstance("123abc")));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		sresult = ((StringProperty) r1).getValue();
		assertFalse("123abc".equals(sresult));
		assertTrue("_3123abc".equalsIgnoreCase(sresult));

		// Execute operations.
		r1 = runDml(Escape.getInstance(null, StringProperty
				.getInstance("\u0080\u00a4\u00ee")));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		sresult = ((StringProperty) r1).getValue();
		assertTrue("_80_a4_ee".equalsIgnoreCase(sresult));
	}

	@Test
	public void encodeNonAlphanumericString() throws SyntaxException {

		// Execute operations.
		Element r1 = runDml(Escape.getInstance(null, StringProperty
				.getInstance("_+")));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		String sresult = ((StringProperty) r1).getValue();
		assertTrue("_5f_2b".equalsIgnoreCase(sresult));
	}

	@Test(expected = SyntaxException.class)
	public void testTooFewArguments() throws SyntaxException {
		Escape.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void testTooManyArguments() throws SyntaxException {
		Escape.getInstance(null, StringProperty.getInstance("OK"),
				StringProperty.getInstance("OK"));
	}

}
