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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/IndexTest.java $
 $Id: IndexTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.utils.TermFactory;

public class IndexTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Index.class);
	}

	@Test
	public void findSubstring() throws SyntaxException {

		StringProperty string = StringProperty.getInstance("abcfoodefoobar");
		StringProperty str = StringProperty.getInstance("foo");
		LongProperty start = LongProperty.getInstance(4L);

		Element r1 = runDml(Index.getInstance(null, str, string));

		assertTrue(r1 instanceof LongProperty);
		long s1 = ((LongProperty) r1).getValue().longValue();
		assertTrue(s1 == 3);

		r1 = runDml(Index.getInstance(null, str, string, start));

		assertTrue(r1 instanceof LongProperty);
		s1 = ((LongProperty) r1).getValue().longValue();
		assertTrue(s1 == 8);

		str = StringProperty.getInstance("f0o");

		r1 = runDml(Index.getInstance(null, str, string));

		assertTrue(r1 instanceof LongProperty);
		s1 = ((LongProperty) r1).getValue().longValue();
		assertTrue(s1 == -1);

	}

	@Test
	public void findPropertyInList() throws SyntaxException,
			InvalidTermException {

		ListResource list = new ListResource();
		list.put(TermFactory.create(0), StringProperty.getInstance("Foo"));
		list.put(TermFactory.create(1), StringProperty.getInstance("FOO"));
		list.put(TermFactory.create(2), StringProperty.getInstance("foo"));
		list.put(TermFactory.create(3), StringProperty.getInstance("bar"));

		Property property = StringProperty.getInstance("foo");
		LongProperty start = LongProperty.getInstance(3L);

		Element r1 = runDml(Index.getInstance(null, property, list));

		assertTrue(r1 instanceof LongProperty);
		long s1 = ((LongProperty) r1).getValue().longValue();
		assertTrue(s1 == 2);

		r1 = runDml(Index.getInstance(null, property, list, start));

		assertTrue(r1 instanceof LongProperty);
		s1 = ((LongProperty) r1).getValue().longValue();
		assertTrue(s1 == -1);

		list = new ListResource();
		list.put(TermFactory.create(0), LongProperty.getInstance(3L));
		list.put(TermFactory.create(1), LongProperty.getInstance(1L));
		list.put(TermFactory.create(2), LongProperty.getInstance(4L));
		list.put(TermFactory.create(3), LongProperty.getInstance(1L));
		list.put(TermFactory.create(4), LongProperty.getInstance(6L));

		property = LongProperty.getInstance(1L);
		start = LongProperty.getInstance(2L);

		r1 = runDml(Index.getInstance(null, property, list));

		assertTrue(r1 instanceof LongProperty);
		s1 = ((LongProperty) r1).getValue().longValue();
		assertTrue(s1 == 1);

		r1 = runDml(Index.getInstance(null, property, list, start));

		assertTrue(r1 instanceof LongProperty);
		s1 = ((LongProperty) r1).getValue().longValue();
		assertTrue(s1 == 3);

	}

	@Test
	public void findPropertyInHash() throws SyntaxException,
			InvalidTermException {

		HashResource dict = new HashResource();
		dict.put(TermFactory.create("a"), StringProperty.getInstance("Foo"));
		dict.put(TermFactory.create("b"), StringProperty.getInstance("FOO"));
		dict.put(TermFactory.create("c"), StringProperty.getInstance("foo"));
		dict.put(TermFactory.create("d"), StringProperty.getInstance("bar"));

		Property property = StringProperty.getInstance("foo");
		LongProperty start = LongProperty.getInstance(2L);

		Element r1 = runDml(Index.getInstance(null, property, dict));

		assertTrue(r1 instanceof StringProperty);
		String s1 = ((StringProperty) r1).getValue();
		assertTrue("c".equals(s1));

		r1 = runDml(Index.getInstance(null, property, dict, start));

		assertTrue(r1 instanceof StringProperty);
		s1 = ((StringProperty) r1).getValue();
		assertTrue("".equals(s1));

		dict = new HashResource();
		dict.put(TermFactory.create("a"), LongProperty.getInstance(3L));
		dict.put(TermFactory.create("b"), LongProperty.getInstance(1L));
		dict.put(TermFactory.create("c"), LongProperty.getInstance(4L));
		dict.put(TermFactory.create("d"), LongProperty.getInstance(1L));
		dict.put(TermFactory.create("e"), LongProperty.getInstance(6L));

		property = LongProperty.getInstance(1L);
		start = LongProperty.getInstance(1L);

		r1 = runDml(Index.getInstance(null, property, dict));

		assertTrue(r1 instanceof StringProperty);
		s1 = ((StringProperty) r1).getValue();
		assertTrue("b".equals(s1));

		r1 = runDml(Index.getInstance(null, property, dict, start));

		assertTrue(r1 instanceof StringProperty);
		s1 = ((StringProperty) r1).getValue();
		assertTrue("d".equals(s1));

	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments1() throws SyntaxException {
		runDml(Index.getInstance(null, StringProperty.getInstance("abcdef"),
				LongProperty.getInstance(0L), LongProperty.getInstance(3L)));
	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments2() throws SyntaxException {
		runDml(Index.getInstance(null, StringProperty.getInstance("abcdef"),
				StringProperty.getInstance("ok"), StringProperty
						.getInstance("bad")));
	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments3() throws SyntaxException {
		runDml(Index.getInstance(null, new ListResource(), StringProperty
				.getInstance("ok"), StringProperty.getInstance("bad")));
	}

	@Test(expected = SyntaxException.class)
	public void tooFewArguments() throws SyntaxException {
		Index.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void tooManyArguments() throws SyntaxException {
		Index.getInstance(null, StringProperty.getInstance("OK"),
				StringProperty.getInstance("OK"), LongProperty.getInstance(1L),
				LongProperty.getInstance(1L));
	}

}
