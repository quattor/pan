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

public class ReplaceTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Replace.class);
	}

	@Test
	public void testReplacement() throws SyntaxException {

		// Execute operations.
		Element r1 = runDml(Replace.getInstance(null, StringProperty
				.getInstance("\\d"), StringProperty.getInstance("-"),
				StringProperty.getInstance("a1b2c3d")));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		StringProperty s1 = (StringProperty) r1;
		String sresult = s1.getValue();
		assertTrue("a-b-c-d".equals(sresult));
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidRegex() throws SyntaxException {
		runDml(Replace.getInstance(null, StringProperty.getInstance("\\p"),
				StringProperty.getInstance("OK"), StringProperty
						.getInstance("OK")));
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidRepl() throws SyntaxException {
		runDml(Replace.getInstance(null, StringProperty.getInstance("OK"),
				LongProperty.getInstance(1L), StringProperty.getInstance("OK")));
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidTarget() throws SyntaxException {
		runDml(Replace.getInstance(null, StringProperty.getInstance("OK"),
				StringProperty.getInstance("OK"), LongProperty.getInstance(1L)));
	}

	@Test(expected = SyntaxException.class)
	public void testTooFewArguments() throws SyntaxException {
		Replace.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void testTooManyArguments() throws SyntaxException {
		Replace.getInstance(null, StringProperty.getInstance("OK"),
				StringProperty.getInstance("OK"), StringProperty
						.getInstance("OK"), StringProperty.getInstance("OK"));
	}

}
