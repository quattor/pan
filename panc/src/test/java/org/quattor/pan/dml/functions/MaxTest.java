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

public class MaxTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Max.class);
	}

	@Test
	public void testLongs() throws SyntaxException {
		Element little = LongProperty.getInstance("42");
		Element big = LongProperty.getInstance("2112");

		Element r;

		// Little Big
		r = runDml(Max.getInstance(null, little, big));
		assertTrue(r instanceof LongProperty);
		assertTrue(2112 == ((LongProperty) r).getValue());

		// Big Little
		r = runDml(Max.getInstance(null, big, little));
		assertTrue(r instanceof LongProperty);
		assertTrue(2112 == ((LongProperty) r).getValue());
	}

	@Test
	public void testDoubles() throws SyntaxException {
		Element little = DoubleProperty.getInstance("41153.7");
		Element big = DoubleProperty.getInstance("48315.6");

		Element r;

		// Little Big
		r = runDml(Max.getInstance(null, little, big));
		assertTrue(r instanceof DoubleProperty);
		assertTrue(48315.6 == ((DoubleProperty) r).getValue());

		// Big Little
		r = runDml(Max.getInstance(null, big, little));
		assertTrue(r instanceof DoubleProperty);
		assertTrue(48315.6 == ((DoubleProperty) r).getValue());
	}

	@Test
	public void testMixed() throws SyntaxException {
		Element little = DoubleProperty.getInstance("5.85");
		Element big = LongProperty.getInstance("6");

		Element r;

		r = runDml(Max.getInstance(null, little, big));
		assertTrue(r instanceof DoubleProperty);
		assertTrue(6.0 == ((DoubleProperty) r).getValue());
	}

	@Test(expected = SyntaxException.class)
	public void tooFewArgs() throws SyntaxException {
		Max.getInstance(null, LongProperty.getInstance(1));
	}

	@Test(expected = SyntaxException.class)
	public void tooManyArgs() throws SyntaxException {
		Max.getInstance(null, LongProperty.getInstance(1), LongProperty.getInstance(2), LongProperty.getInstance(3));
	}

	@Test(expected = EvaluationException.class)
	public void invalidTypes() throws SyntaxException {
		runDml(Max.getInstance(null, StringProperty.getInstance("a"), StringProperty.getInstance("b")));
	}

}
