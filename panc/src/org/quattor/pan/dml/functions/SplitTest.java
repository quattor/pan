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
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.utils.TermFactory;

public class SplitTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Split.class);
	}

	@Test
	public void testDefaultSplit() throws SyntaxException, InvalidTermException {

		String[] tvalue = new String[] { "a", "b", "c" };

		// Execute operations.
		Element r1 = runDml(Split.getInstance(null, StringProperty
				.getInstance("\\s*,\\s*"), StringProperty
				.getInstance("a,b , c,,")));

		// Check result.
		assertTrue(r1 instanceof ListResource);
		ListResource list1 = (ListResource) r1;
		assertTrue(list1.size() == tvalue.length);

		// Check values.
		for (int i = 0; i < list1.size(); i++) {
			Element t = list1.get(TermFactory.create((long) i));
			assertTrue(t instanceof StringProperty);
			String s = ((StringProperty) t).getValue();
			assertTrue(tvalue[i].equals(s));
		}

		// Execute operations.
		Element r2 = runDml(Split.getInstance(null, StringProperty
				.getInstance("\\s*,\\s*"), LongProperty.getInstance(0L),
				StringProperty.getInstance("a,b , c,,")));

		// Check result.
		assertTrue(r2 instanceof ListResource);
		ListResource list2 = (ListResource) r2;
		assertTrue(list2.size() == tvalue.length);

		// Check values.
		for (int i = 0; i < list2.size(); i++) {
			Element t = list2.get(TermFactory.create((long) i));
			assertTrue(t instanceof StringProperty);
			String s = ((StringProperty) t).getValue();
			assertTrue(tvalue[i].equals(s));
		}
	}

	@Test
	public void testFullSplit() throws SyntaxException, InvalidTermException {

		String[] tvalue = new String[] { "a", "b", "c", "", "" };

		// Execute operations.
		Element r1 = runDml(Split.getInstance(null, StringProperty
				.getInstance("\\s*,\\s*"), LongProperty.getInstance(-1L),
				StringProperty.getInstance("a,b , c,,")));

		// Check result.
		assertTrue(r1 instanceof ListResource);
		ListResource list1 = (ListResource) r1;
		assertTrue(list1.size() == tvalue.length);

		// Check values.
		for (int i = 0; i < list1.size(); i++) {
			Element t = list1.get(TermFactory.create((long) i));
			assertTrue(t instanceof StringProperty);
			String s = ((StringProperty) t).getValue();
			assertTrue(tvalue[i].equals(s));
		}

	}

	@Test
	public void testPartialSplit() throws SyntaxException, InvalidTermException {

		String[] tvalue = new String[] { "a", "b , c,," };

		// Execute operations.
		Element r1 = runDml(Split.getInstance(null, StringProperty
				.getInstance("\\s*,\\s*"), LongProperty.getInstance(2L),
				StringProperty.getInstance("a,b , c,,")));

		// Check result.
		assertTrue(r1 instanceof ListResource);
		ListResource list1 = (ListResource) r1;
		assertTrue(list1.size() == tvalue.length);

		// Check values.
		for (int i = 0; i < list1.size(); i++) {
			Element t = list1.get(TermFactory.create((long) i));
			assertTrue(t instanceof StringProperty);
			String s = ((StringProperty) t).getValue();
			assertTrue(tvalue[i].equals(s));
		}

	}

	@Test(expected = EvaluationException.class)
	public void testInvalidRegex() throws SyntaxException {
		runDml(Split.getInstance(null, StringProperty.getInstance("\\p"),
				LongProperty.getInstance(0L), StringProperty
						.getInstance("a,b , c,,")));
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidLimit() throws SyntaxException {
		runDml(Split.getInstance(null, StringProperty.getInstance("\\s*,\\s*"),
				StringProperty.getInstance("bad"), StringProperty
						.getInstance("a,b , c,,")));
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidTarget1() throws SyntaxException {
		runDml(Split.getInstance(null, StringProperty.getInstance("\\s*,\\s*"),
				LongProperty.getInstance(0L)));
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidTarget2() throws SyntaxException {
		runDml(Split.getInstance(null, StringProperty.getInstance("\\s*,\\s*"),
				LongProperty.getInstance(0L), LongProperty.getInstance(0L)));
	}

	@Test(expected = SyntaxException.class)
	public void testTooFewArguments() throws SyntaxException {
		Split.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void testTooManyArguments() throws SyntaxException {
		Split.getInstance(null, StringProperty.getInstance("OK"),
				StringProperty.getInstance("OK"), StringProperty
						.getInstance("OK"), StringProperty.getInstance("OK"));
	}

}
