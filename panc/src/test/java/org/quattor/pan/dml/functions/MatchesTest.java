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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/MatchesTest.java $
 $Id: MatchesTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.utils.TermFactory;

public class MatchesTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Matches.class);
	}

	@Test
	public void checkMatches() throws SyntaxException, InvalidTermException {

		Element r1 = runDml(Matches.getInstance(null, StringProperty
				.getInstance("alpha"), StringProperty.getInstance("^a(.*)")));

		// Check result.
		assertTrue(r1 instanceof ListResource);
		ListResource result = (ListResource) r1;
		assertTrue(result.size() == 2);
		Element e1 = result.get(TermFactory.create(0));
		assertTrue(e1 instanceof StringProperty);
		assertTrue("alpha".equals(((StringProperty) e1).getValue()));
		e1 = result.get(TermFactory.create(1));
		assertTrue(e1 instanceof StringProperty);
		assertTrue("lpha".equals(((StringProperty) e1).getValue()));

		r1 = runDml(Matches.getInstance(null, StringProperty
				.getInstance("Alpha"), StringProperty.getInstance("^a(.*)")));

		// Check result.
		assertTrue(r1 instanceof ListResource);
		result = (ListResource) r1;
		assertTrue(result.size() == 0);

		r1 = runDml(Matches.getInstance(null, StringProperty
				.getInstance("Alpha"), StringProperty.getInstance("^a(.*)"),
				StringProperty.getInstance("i")));

		// Check result.
		assertTrue(r1 instanceof ListResource);
		result = (ListResource) r1;
		assertTrue(result.size() == 2);
		e1 = result.get(TermFactory.create(0));
		assertTrue(e1 instanceof StringProperty);
		assertTrue("Alpha".equals(((StringProperty) e1).getValue()));
		e1 = result.get(TermFactory.create(1));
		assertTrue(e1 instanceof StringProperty);
		assertTrue("lpha".equals(((StringProperty) e1).getValue()));

		// Check that the implicit '.*?' matches at the beginning and end of
		// regular expression work.
		r1 = runDml(Matches.getInstance(null,
				StringProperty.getInstance("foo"), StringProperty
						.getInstance("(o)")));

		// Check result.
		assertTrue(r1 instanceof ListResource);
		result = (ListResource) r1;
		assertTrue(result.size() == 2);
		e1 = result.get(TermFactory.create(0));
		assertTrue(e1 instanceof StringProperty);
		assertTrue("o".equals(((StringProperty) e1).getValue()));
		e1 = result.get(TermFactory.create(1));
		assertTrue(e1 instanceof StringProperty);
		assertTrue("o".equals(((StringProperty) e1).getValue()));

		// Check that the backslash expression rewriting works.
		r1 = runDml(Matches.getInstance(null, StringProperty
				.getInstance("abbcddef aabcddeff"), StringProperty
				.getInstance("((\\w)\\2).+\\1")));

		// Check result.
		assertTrue(r1 instanceof ListResource);
		result = (ListResource) r1;
		assertTrue(result.size() == 3);
		e1 = result.get(TermFactory.create(0));
		assertTrue(e1 instanceof StringProperty);
		assertTrue("ddef aabcdd".equals(((StringProperty) e1).getValue()));
		e1 = result.get(TermFactory.create(1));
		assertTrue(e1 instanceof StringProperty);
		assertTrue("dd".equals(((StringProperty) e1).getValue()));
		e1 = result.get(TermFactory.create(2));
		assertTrue(e1 instanceof StringProperty);
		assertTrue("d".equals(((StringProperty) e1).getValue()));
	}

	@Test
	public void checkNullSuppression() throws SyntaxException,
			InvalidTermException {

		Element r1 = runDml(Matches.getInstance(null, StringProperty
				.getInstance("aaabbbccc"), StringProperty
				.getInstance("(a*)(b+)?(c+)?")));

		// All groups should match, so no problem.
		assertTrue(r1 instanceof ListResource);
		ListResource result = (ListResource) r1;
		assertTrue(result.size() == 4);

		for (int i = 0; i < result.size(); i++) {
			assertNotNull(result.get(TermFactory.create(i)));
		}

		r1 = runDml(Matches.getInstance(null, StringProperty
				.getInstance("aaaccc"), StringProperty
				.getInstance("(a*)(b+)?(c+)?")));

		// Second group won't match. Java will return null. Make sure this is
		// changed to an empty string.
		assertTrue(r1 instanceof ListResource);
		result = (ListResource) r1;
		assertTrue(result.size() == 4);

		for (int i = 0; i < result.size(); i++) {
			assertNotNull(result.get(TermFactory.create(i)));
		}

		r1 = runDml(Matches.getInstance(null,
				StringProperty.getInstance("aaa"), StringProperty
						.getInstance("(a*)(b+)?(c+)?")));

		// Second and third groups won't match. Make sure only 2 values are
		// returned.
		assertTrue(r1 instanceof ListResource);
		result = (ListResource) r1;
		assertTrue(result.size() == 2);

		for (int i = 0; i < result.size(); i++) {
			assertNotNull(result.get(TermFactory.create(i)));
		}

	}

	@Test(expected = SyntaxException.class)
	public void invalidArgument() throws SyntaxException {
		Matches.getInstance(null, LongProperty.getInstance(1L), StringProperty
				.getInstance(".*"));
	}

	@Test(expected = SyntaxException.class)
	public void invalidRegExp() throws SyntaxException {
		runDml(Matches.getInstance(null, StringProperty.getInstance("a"),
				StringProperty.getInstance("[invalid")));
	}

	@Test(expected = SyntaxException.class)
	public void invalidOption() throws SyntaxException {
		runDml(Matches.getInstance(null, StringProperty.getInstance("a"),
				StringProperty.getInstance(".*"), StringProperty
						.getInstance("q")));
	}

	@Test(expected = SyntaxException.class)
	public void invalidCallWithNoArguments() throws SyntaxException {
		Matches.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void invalidCallWithTooManyArguments() throws SyntaxException {
		Matches.getInstance(null, StringProperty.getInstance("a"),
				StringProperty.getInstance("b"), StringProperty
						.getInstance("c"), StringProperty.getInstance("d"));
	}

}
