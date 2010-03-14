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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/SubstrTest.java $
 $Id: SubstrTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;

public class SubstrTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Substr.class);
	}

	@Test
	public void validSubstrings() throws SyntaxException {

		StringProperty string = StringProperty.getInstance("abcdef");
		LongProperty start = LongProperty.getInstance(2L);
		LongProperty length;

		Element r1 = runDml(Substr.getInstance(null, string, start));

		assertTrue(r1 instanceof StringProperty);
		String s1 = ((StringProperty) r1).getValue();
		assertTrue("cdef".equals(s1));

		start = LongProperty.getInstance(1L);
		length = LongProperty.getInstance(1L);

		r1 = runDml(Substr.getInstance(null, string, start, length));

		assertTrue(r1 instanceof StringProperty);
		s1 = ((StringProperty) r1).getValue();
		assertTrue("b".equals(s1));

		start = LongProperty.getInstance(1L);
		length = LongProperty.getInstance(-1L);

		r1 = runDml(Substr.getInstance(null, string, start, length));

		assertTrue(r1 instanceof StringProperty);
		s1 = ((StringProperty) r1).getValue();
		assertTrue("bcde".equals(s1));

		start = LongProperty.getInstance(-4L);

		r1 = runDml(Substr.getInstance(null, string, start));

		assertTrue(r1 instanceof StringProperty);
		s1 = ((StringProperty) r1).getValue();
		assertTrue("cdef".equals(s1));

		start = LongProperty.getInstance(-4L);
		length = LongProperty.getInstance(1L);

		r1 = runDml(Substr.getInstance(null, string, start, length));

		assertTrue(r1 instanceof StringProperty);
		s1 = ((StringProperty) r1).getValue();
		assertTrue("c".equals(s1));

		start = LongProperty.getInstance(-4L);
		length = LongProperty.getInstance(-1L);

		r1 = runDml(Substr.getInstance(null, string, start, length));

		assertTrue(r1 instanceof StringProperty);
		s1 = ((StringProperty) r1).getValue();
		assertTrue("cde".equals(s1));

	}

	@Test(expected = EvaluationException.class)
	public void invalidIndex1() throws SyntaxException {

		StringProperty string = StringProperty.getInstance("abcdef");
		LongProperty start = LongProperty.getInstance(7L);

		runDml(Substr.getInstance(null, string, start));
	}

	@Test(expected = EvaluationException.class)
	public void invalidIndex2() throws SyntaxException {

		StringProperty string = StringProperty.getInstance("abcdef");
		LongProperty start = LongProperty.getInstance(-7L);

		runDml(Substr.getInstance(null, string, start));
	}

	@Test(expected = EvaluationException.class)
	public void invalidIndex3() throws SyntaxException {

		StringProperty string = StringProperty.getInstance("abcdef");
		LongProperty start = LongProperty.getInstance(3L);
		LongProperty length = LongProperty.getInstance(-7L);

		runDml(Substr.getInstance(null, string, start, length));
	}

	@Test(expected = EvaluationException.class)
	public void invalidIndex4() throws SyntaxException {

		StringProperty string = StringProperty.getInstance("abcdef");
		LongProperty start = LongProperty.getInstance(4L);
		LongProperty length = LongProperty.getInstance(-4L);

		runDml(Substr.getInstance(null, string, start, length));
	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments1() throws SyntaxException {
		runDml(Substr
				.getInstance(null, StringProperty.getInstance("abcdef"),
						StringProperty.getInstance("bad"), LongProperty
								.getInstance(3L)));
	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments2() throws SyntaxException {
		runDml(Substr
				.getInstance(null, StringProperty.getInstance("abcdef"),
						LongProperty.getInstance(3L), StringProperty
								.getInstance("bad")));
	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments3() throws SyntaxException {
		runDml(Substr.getInstance(null, new HashResource(), LongProperty
				.getInstance(3L), LongProperty.getInstance(3L)));
	}

	@Test(expected = SyntaxException.class)
	public void tooFewArguments() throws SyntaxException {
		Substr.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void tooManyArguments() throws SyntaxException {
		Substr.getInstance(null, StringProperty.getInstance("OK"), LongProperty
				.getInstance(1L), LongProperty.getInstance(1L), LongProperty
				.getInstance(1L));
	}

}
