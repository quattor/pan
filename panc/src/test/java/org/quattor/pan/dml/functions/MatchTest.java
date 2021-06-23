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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/MatchTest.java $
 $Id: MatchTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.SyntaxException;

public class MatchTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Match.class);
	}

	@Test
	public void checkMatches() throws SyntaxException {

		Element r1 = runDml(Match.getInstance(null, StringProperty
				.getInstance("alpha"), StringProperty.getInstance("^a.*")));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		boolean result = ((BooleanProperty) r1).getValue().booleanValue();
		assertTrue(result);

		r1 = runDml(Match.getInstance(null,
				StringProperty.getInstance("Alpha"), StringProperty
						.getInstance("^a.*")));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		result = ((BooleanProperty) r1).getValue().booleanValue();
		assertFalse(result);

		r1 = runDml(Match.getInstance(null,
				StringProperty.getInstance("Alpha"), StringProperty
						.getInstance("^a.*"), StringProperty.getInstance("i")));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		result = ((BooleanProperty) r1).getValue().booleanValue();
		assertTrue(result);

		r1 = runDml(Match.getInstance(null, StringProperty.getInstance("foo"),
				StringProperty.getInstance("o")));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		result = ((BooleanProperty) r1).getValue().booleanValue();
		assertTrue(result);

	}

	@Test(expected = SyntaxException.class)
	public void invalidArgument() throws SyntaxException {
		Match.getInstance(null, LongProperty.getInstance(1L), StringProperty
				.getInstance(".*"));
	}

	@Test(expected = SyntaxException.class)
	public void invalidRegExp() throws SyntaxException {
		runDml(Match.getInstance(null, StringProperty.getInstance("a"),
				StringProperty.getInstance("[invalid")));
	}

	@Test(expected = SyntaxException.class)
	public void invalidOption() throws SyntaxException {
		runDml(Match.getInstance(null, StringProperty.getInstance("a"),
				StringProperty.getInstance(".*"), StringProperty
						.getInstance("q")));
	}

	@Test(expected = SyntaxException.class)
	public void invalidCallWithNoArguments() throws SyntaxException {
		Match.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void invalidCallWithOneArgument() throws SyntaxException {
		Match.getInstance(null, StringProperty.getInstance("a"));
	}

	@Test(expected = SyntaxException.class)
	public void invalidCallWithTooManyArguments() throws SyntaxException {
		Match.getInstance(null, StringProperty.getInstance("a"), StringProperty
				.getInstance("b"), StringProperty.getInstance("c"),
				StringProperty.getInstance("d"));
	}

}
