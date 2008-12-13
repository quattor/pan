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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/IsStringTest.java $
 $Id: IsStringTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.SyntaxException;

public class IsStringTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(IsString.class);
	}

	@Test
	public void testString() throws SyntaxException {

		// Execute operations.
		Element r1 = runDml(IsString.getInstance(null, StringProperty
				.getInstance("OK")));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		BooleanProperty s1 = (BooleanProperty) r1;
		boolean sresult = s1.getValue().booleanValue();
		assertTrue(sresult);
	}

	@Test
	public void testNonStrings() throws SyntaxException {

		// Check long value.
		Element r1 = runDml(IsString.getInstance(null, LongProperty
				.getInstance(1L)));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		BooleanProperty s1 = (BooleanProperty) r1;
		boolean sresult = s1.getValue().booleanValue();
		assertFalse(sresult);

		// Check nlist resource.
		r1 = runDml(IsString.getInstance(null, new HashResource()));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		s1 = (BooleanProperty) r1;
		sresult = s1.getValue().booleanValue();
		assertFalse(sresult);
	}

	@Test(expected = SyntaxException.class)
	public void testTooFewArguments() throws SyntaxException {
		IsString.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void testTooManyArguments() throws SyntaxException {
		IsString.getInstance(null, StringProperty.getInstance("OK"),
				StringProperty.getInstance("OK"));
	}

}
