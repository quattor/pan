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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/SpliceTest.java $
 $Id: SpliceTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.utils.TermFactory;

public class SpliceTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Splice.class);
	}

	@Test
	public void validStrings() throws SyntaxException {

		StringProperty string = StringProperty.getInstance("abcde");
		LongProperty start = LongProperty.getInstance(2L);
		LongProperty length = LongProperty.getInstance(0L);
		StringProperty replacement = StringProperty.getInstance("12");

		Element r1 = runDml(Splice.getInstance(null, string, start, length,
				replacement));

		assertTrue(r1 instanceof StringProperty);
		String s1 = ((StringProperty) r1).getValue();
		assertTrue("ab12cde".equals(s1));

		start = LongProperty.getInstance(-2L);
		length = LongProperty.getInstance(1L);

		r1 = runDml(Splice.getInstance(null, string, start, length));

		assertTrue(r1 instanceof StringProperty);
		s1 = ((StringProperty) r1).getValue();
		assertTrue("abce".equals(s1));

		start = LongProperty.getInstance(2L);
		length = LongProperty.getInstance(2L);
		replacement = StringProperty.getInstance("XXX");

		r1 = runDml(Splice
				.getInstance(null, string, start, length, replacement));

		assertTrue(r1 instanceof StringProperty);
		s1 = ((StringProperty) r1).getValue();
		assertTrue("abXXXe".equals(s1));

	}

	@Test
	public void validLists() throws SyntaxException, InvalidTermException {

		ListResource list = new ListResource();
		list.put(TermFactory.create(0), StringProperty.getInstance("a"));
		list.put(TermFactory.create(1), StringProperty.getInstance("b"));
		list.put(TermFactory.create(2), StringProperty.getInstance("c"));
		list.put(TermFactory.create(3), StringProperty.getInstance("d"));
		list.put(TermFactory.create(4), StringProperty.getInstance("e"));

		LongProperty start = LongProperty.getInstance(2L);
		LongProperty length = LongProperty.getInstance(0L);

		ListResource replacement = new ListResource();
		replacement.put(TermFactory.create(0), StringProperty.getInstance("1"));
		replacement.put(TermFactory.create(1), StringProperty.getInstance("2"));

		Element r1 = runDml(Splice.getInstance(null, list, start, length,
				replacement));

		assertTrue(r1 instanceof ListResource);
		ListResource s1 = (ListResource) r1;
		assertTrue(s1.size() == 7);
		assertTrue("a".equals(((StringProperty) s1.get(TermFactory.create(0)))
				.getValue()));
		assertTrue("b".equals(((StringProperty) s1.get(TermFactory.create(1)))
				.getValue()));
		assertTrue("1".equals(((StringProperty) s1.get(TermFactory.create(2)))
				.getValue()));
		assertTrue("2".equals(((StringProperty) s1.get(TermFactory.create(3)))
				.getValue()));
		assertTrue("c".equals(((StringProperty) s1.get(TermFactory.create(4)))
				.getValue()));
		assertTrue("d".equals(((StringProperty) s1.get(TermFactory.create(5)))
				.getValue()));
		assertTrue("e".equals(((StringProperty) s1.get(TermFactory.create(6)))
				.getValue()));

		start = LongProperty.getInstance(-2L);
		length = LongProperty.getInstance(1L);

		r1 = runDml(Splice.getInstance(null, list, start, length));

		assertTrue(r1 instanceof ListResource);
		s1 = (ListResource) r1;
		assertTrue(s1.size() == 4);
		assertTrue("a".equals(((StringProperty) s1.get(TermFactory.create(0)))
				.getValue()));
		assertTrue("b".equals(((StringProperty) s1.get(TermFactory.create(1)))
				.getValue()));
		assertTrue("c".equals(((StringProperty) s1.get(TermFactory.create(2)))
				.getValue()));
		assertTrue("e".equals(((StringProperty) s1.get(TermFactory.create(3)))
				.getValue()));

		start = LongProperty.getInstance(2L);
		length = LongProperty.getInstance(2L);
		replacement = new ListResource();
		replacement.put(TermFactory.create(0), StringProperty
				.getInstance("XXX"));

		r1 = runDml(Splice.getInstance(null, list, start, length, replacement));

		assertTrue(r1 instanceof ListResource);
		s1 = (ListResource) r1;
		assertTrue(s1.size() == 4);
		assertTrue("a".equals(((StringProperty) s1.get(TermFactory.create(0)))
				.getValue()));
		assertTrue("b".equals(((StringProperty) s1.get(TermFactory.create(1)))
				.getValue()));
		assertTrue("XXX"
				.equals(((StringProperty) s1.get(TermFactory.create(2)))
						.getValue()));
		assertTrue("e".equals(((StringProperty) s1.get(TermFactory.create(3)))
				.getValue()));

	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments1() throws SyntaxException {
		runDml(Splice.getInstance(null, DoubleProperty.getInstance(1.0),
				LongProperty.getInstance(1L), LongProperty.getInstance(3L)));
	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments2() throws SyntaxException {
		runDml(Splice.getInstance(null, StringProperty.getInstance("abcde"),
				LongProperty.getInstance(1L), LongProperty.getInstance(3L),
				new ListResource()));
	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments3() throws SyntaxException {
		runDml(Splice.getInstance(null, new ListResource(), LongProperty
				.getInstance(1L), LongProperty.getInstance(3L), StringProperty
				.getInstance("abcde")));
	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments4() throws SyntaxException {
		runDml(Splice.getInstance(null, StringProperty.getInstance("abcde"),
				BooleanProperty.TRUE, LongProperty.getInstance(3L),
				StringProperty.getInstance("abcde")));
	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments5() throws SyntaxException {
		runDml(Splice.getInstance(null, StringProperty.getInstance("abcde"),
				LongProperty.getInstance(1L), BooleanProperty.TRUE,
				StringProperty.getInstance("abcde")));
	}

	@Test(expected = SyntaxException.class)
	public void tooFewArguments() throws SyntaxException {
		Splice.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void tooManyArguments() throws SyntaxException {
		Splice.getInstance(null, StringProperty.getInstance("OK"), LongProperty
				.getInstance(1L), LongProperty.getInstance(1L), StringProperty
				.getInstance("OK"), LongProperty.getInstance(1L));
	}

}
