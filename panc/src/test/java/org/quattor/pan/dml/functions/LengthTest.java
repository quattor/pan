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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/LengthTest.java $
 $Id: LengthTest.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.utils.TermFactory;

public class LengthTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Length.class);
	}

	@Test
	public void stringLength() throws SyntaxException {

		// Check long value.
		Element r1 = runDml(Length.getInstance(null, StringProperty
				.getInstance("")));

		// Check result.
		assertTrue(r1 instanceof LongProperty);
		long length = ((LongProperty) r1).getValue().longValue();
		assertTrue(length == 0);

		// Check long value.
		r1 = runDml(Length.getInstance(null, StringProperty.getInstance("a")));

		// Check result.
		assertTrue(r1 instanceof LongProperty);
		length = ((LongProperty) r1).getValue().longValue();
		assertTrue(length == 1);
	}

	@Test
	public void resourceLength() throws SyntaxException, InvalidTermException {

		// Check long value.
		ListResource list = new ListResource();
		list.put(TermFactory.create(0), StringProperty.getInstance("a"));
		list.put(TermFactory.create(1), Undef.VALUE);
		Element r1 = runDml(Length.getInstance(null, list));

		// Check result.
		assertTrue(r1 instanceof LongProperty);
		long length = ((LongProperty) r1).getValue().longValue();
		assertTrue(length == 2);

		// Check long value.
		HashResource dict = new HashResource();
		dict.put(TermFactory.create("a"), LongProperty.getInstance(1L));
		r1 = runDml(Length.getInstance(null, dict));

		// Check result.
		assertTrue(r1 instanceof LongProperty);
		length = ((LongProperty) r1).getValue().longValue();
		assertTrue(length == 1);

		// Special case. Length of undef should be zero.
		r1 = runDml(Length.getInstance(null, Undef.VALUE));
		assertTrue(r1 instanceof LongProperty);
		length = ((LongProperty) r1).getValue().longValue();
		assertTrue(length == 0);
	}

	@Test(expected = EvaluationException.class)
	public void invalidArgument() throws SyntaxException {
		runDml(Length.getInstance(null, LongProperty.getInstance(2L)));
	}

	@Test(expected = SyntaxException.class)
	public void invalidCallWithNoArguments() throws SyntaxException {
		Length.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void invalidCallWithTooManyArguments() throws SyntaxException {
		Length.getInstance(null, StringProperty.getInstance("a"),
				StringProperty.getInstance("b"));
	}

}
