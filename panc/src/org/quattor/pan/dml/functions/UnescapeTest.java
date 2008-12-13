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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/UnescapeTest.java $
 $Id: UnescapeTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.SyntaxException;

public class UnescapeTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Unescape.class);
	}

	@Test
	public void decodeEmptyString() throws SyntaxException {

		// Execute operations.
		Element r1 = runDml(Unescape.getInstance(null, StringProperty
				.getInstance("_")));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		String sresult = ((StringProperty) r1).getValue();
		assertTrue("".equals(sresult));
	}

	@Test
	public void decodeAlphanumericString() throws SyntaxException {

		// Execute operations.
		Element r1 = runDml(Unescape.getInstance(null, StringProperty
				.getInstance("abc123")));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		String sresult = ((StringProperty) r1).getValue();
		assertTrue("abc123".equals(sresult));
	}

	@Test
	public void decodeNonAlphanumericString() throws SyntaxException {

		// Execute operations.
		Element r1 = runDml(Unescape.getInstance(null, StringProperty
				.getInstance("_5f_2b")));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		String sresult = ((StringProperty) r1).getValue();
		assertTrue("_+".equalsIgnoreCase(sresult));
	}

	@Test(expected = SyntaxException.class)
	public void incompleteEscapeSequence() throws SyntaxException {
		runDml(Unescape.getInstance(null, StringProperty.getInstance("_1")));
	}

	@Test(expected = SyntaxException.class)
	public void invalidEscapeSequence() throws SyntaxException {
		runDml(Unescape.getInstance(null, StringProperty.getInstance("_5g")));
	}

	@Test(expected = SyntaxException.class)
	public void testTooFewArguments() throws SyntaxException {
		Unescape.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void testTooManyArguments() throws SyntaxException {
		Unescape.getInstance(null, StringProperty.getInstance("OK"),
				StringProperty.getInstance("OK"));
	}

}
