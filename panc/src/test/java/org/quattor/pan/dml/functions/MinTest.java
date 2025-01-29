/*
 Copyright (c) 2025 Charles A. Loomis, Jr, Cedric Duprilot,
 Centre National de la Recherche Scientifique (CNRS),
 James Adams and UK Research and Innovation (UKRI).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;

public class MinTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Min.class);
	}

	@Test
	public void testLongs() throws SyntaxException {
		Element little = LongProperty.getInstance("42");
		Element big = LongProperty.getInstance("2112");

		Element r;

		// Little Big
		r = runDml(Min.getInstance(null, little, big));
		assertTrue(r instanceof LongProperty);
		assertTrue(42 == ((LongProperty) r).getValue());

		// Big Little
		r = runDml(Min.getInstance(null, big, little));
		assertTrue(r instanceof LongProperty);
		assertTrue(42 == ((LongProperty) r).getValue());
	}

	@Test
	public void testDoubles() throws SyntaxException {
		Element little = DoubleProperty.getInstance("41153.7");
		Element big = DoubleProperty.getInstance("48315.6");

		Element r;

		// Little Big
		r = runDml(Min.getInstance(null, little, big));
		assertTrue(r instanceof DoubleProperty);
		assertTrue(41153.7 == ((DoubleProperty) r).getValue());

		// Big Little
		r = runDml(Min.getInstance(null, big, little));
		assertTrue(r instanceof DoubleProperty);
		assertTrue(41153.7 == ((DoubleProperty) r).getValue());
	}

	@Test
	public void testMixed() throws SyntaxException {
		Element little = LongProperty.getInstance("3");
		Element big = DoubleProperty.getInstance("3.14");

		Element r;

		r = runDml(Min.getInstance(null, little, big));
		assertTrue(r instanceof DoubleProperty);
		assertTrue(3.0 == ((DoubleProperty) r).getValue());
	}

	@Test(expected = SyntaxException.class)
	public void tooFewArgs() throws SyntaxException {
		Min.getInstance(null, LongProperty.getInstance(1));
	}

	@Test(expected = SyntaxException.class)
	public void tooManyArgs() throws SyntaxException {
		Min.getInstance(null, LongProperty.getInstance(1), LongProperty.getInstance(2), LongProperty.getInstance(3));
	}

	@Test(expected = EvaluationException.class)
	public void invalidOctal() throws SyntaxException {
		runDml(Min.getInstance(null, StringProperty.getInstance("a"), StringProperty.getInstance("b")));
	}


}
