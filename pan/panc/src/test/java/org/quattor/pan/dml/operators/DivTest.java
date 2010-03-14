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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/DivTest.java $
 $Id: DivTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.AbstractOperationTestUtils;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;

public class DivTest extends AbstractOperationTestUtils {

	@Test
	public void divideLongs() throws SyntaxException {

		long va = 3;
		long vb = 2;

		// Execute the operations.
		Operation div = Div.newOperation(null, LongProperty.getInstance(va),
				LongProperty.getInstance(vb));

		// Check result.
		assertTrue(div instanceof LongProperty);
		LongProperty result = (LongProperty) div;
		long lvalue = result.getValue().longValue();
		assertTrue((va / vb) == lvalue);
	}

	@Test
	public void divideDoubles() throws SyntaxException {

		double va = 3;
		double vb = 2;

		// Execute the operations.
		Operation div = Div.newOperation(null, DoubleProperty.getInstance(va),
				DoubleProperty.getInstance(vb));

		// Check result.
		assertTrue(div instanceof DoubleProperty);
		DoubleProperty result = (DoubleProperty) div;
		double dvalue = result.getValue().doubleValue();
		assertTrue((va / vb) == dvalue);
	}

	@Test
	public void divideMixed() throws SyntaxException {

		long va = 3;
		double vb = 2;

		// Execute the operations.
		Operation div = Div.newOperation(null, LongProperty.getInstance(va),
				DoubleProperty.getInstance(vb));

		// Check result.
		assertTrue(div instanceof DoubleProperty);
		DoubleProperty result = (DoubleProperty) div;
		double dvalue = result.getValue().doubleValue();
		assertTrue((va / vb) == dvalue);
	}

	@Test(expected = SyntaxException.class)
	public void divideByLongZero() throws SyntaxException {
		Div.newOperation(null, LongProperty.getInstance(3), LongProperty
				.getInstance(0));
	}

	@Test(expected = SyntaxException.class)
	public void divideByDoubleZero() throws SyntaxException {
		Div.newOperation(null, DoubleProperty.getInstance(3.0), DoubleProperty
				.getInstance(0.0));
	}

	@Test(expected = AssertionError.class)
	public void testUnderflow() throws SyntaxException {
		Div.newOperation(null);
	}

	@Test(expected = AssertionError.class)
	public void testOverflow() throws SyntaxException {
		LongProperty i = LongProperty.getInstance(1L);
		Div.newOperation(null, i, i, i);
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidArgument() throws SyntaxException {
		HashResource h = new HashResource();
		Div.newOperation(null, h, h);
	}
}
