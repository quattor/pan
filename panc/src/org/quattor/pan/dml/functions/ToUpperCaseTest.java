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
 $Id: IsStringTest.java 2618 2007-12-08 16:32:02Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;

public class ToUpperCaseTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(ToUpperCase.class);
	}

	@Test
	public void testString() throws SyntaxException {

		// Execute operations.
		Element r1 = runDml(ToUpperCase.getInstance(null, StringProperty
				.getInstance("ok")));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		StringProperty s1 = (StringProperty) r1;
		String sresult = s1.getValue();
		assertTrue("OK".equals(sresult));
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidArgument() throws SyntaxException {
		runDml(ToUpperCase.getInstance(null, LongProperty.getInstance(1L)));
	}

	@Test(expected = SyntaxException.class)
	public void testTooFewArguments() throws SyntaxException {
		ToUpperCase.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void testTooManyArguments() throws SyntaxException {
		ToUpperCase.getInstance(null, StringProperty.getInstance("OK"),
				StringProperty.getInstance("OK"));
	}

}
