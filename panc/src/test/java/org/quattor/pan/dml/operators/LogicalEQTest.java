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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/LogicalEQTest.java $
 $Id: LogicalEQTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.AbstractOperationTestUtils;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;

public class LogicalEQTest extends AbstractOperationTestUtils {

	@Test
	public void testLongs() throws SyntaxException {

		long va = 1;
		long vb = 2;

		// Execute the operations.
		Operation leq = LogicalEQ.newOperation(null, LongProperty
				.getInstance(va), LongProperty.getInstance(vb));

		// Pull the value off the stack.
		assertTrue(leq instanceof BooleanProperty);
		BooleanProperty result = (BooleanProperty) leq;
		boolean bvalue = result.getValue().booleanValue();
		assertTrue((va == vb) == bvalue);
	}

	@Test
	public void testDoubles() throws SyntaxException {

		double va = 1.0;
		double vb = 2.0;

		// Execute the operations.
		Operation leq = LogicalEQ.newOperation(null, DoubleProperty
				.getInstance(va), DoubleProperty.getInstance(vb));

		// Pull the value off the stack.
		assertTrue(leq instanceof BooleanProperty);
		BooleanProperty result = (BooleanProperty) leq;
		boolean bvalue = result.getValue().booleanValue();
		assertTrue((va == vb) == bvalue);
	}

	@Test
	public void testBooleans() throws SyntaxException {

		Boolean va = true;
		Boolean vb = false;

		Operation leq = LogicalEQ.newOperation(null, BooleanProperty.
				getInstance(va), BooleanProperty.getInstance(vb));

		assertTrue(leq instanceof BooleanProperty);
		BooleanProperty result = (BooleanProperty) leq;
		boolean bvalue = result.getValue().booleanValue();
		assertTrue((va == vb) == bvalue);
	}

	@Test
	public void testMixed() throws SyntaxException {

		long va = 1;
		double vb = 2.0;

		// Execute the operations.
		Operation leq = LogicalEQ.newOperation(null, LongProperty
				.getInstance(va), DoubleProperty.getInstance(vb));

		// Pull the value off the stack.
		assertTrue(leq instanceof BooleanProperty);
		BooleanProperty result = (BooleanProperty) leq;
		boolean bvalue = result.getValue().booleanValue();
		assertTrue((va == vb) == bvalue);
	}

	@Test
	public void testString() throws SyntaxException {

		String va = "one";
		String vb = "two";

		// Execute the operations.
		Operation leq = LogicalEQ.newOperation(null, StringProperty
				.getInstance(va), StringProperty.getInstance(vb));

		// Pull the value off the stack.
		assertTrue(leq instanceof BooleanProperty);
		BooleanProperty result = (BooleanProperty) leq;
		boolean bvalue = result.getValue().booleanValue();
		assertTrue(va.equals(vb) == bvalue);
	}

	@Test(expected = AssertionError.class)
	public void testUnderflow() throws SyntaxException {
		LogicalEQ.newOperation(null);
	}

	@Test(expected = AssertionError.class)
	public void testOverflow() throws SyntaxException {
		LongProperty i = LongProperty.getInstance(1L);
		LogicalEQ.newOperation(null, i, i, i);
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidArguments() throws SyntaxException {
		HashResource h = new HashResource();
		LogicalEQ.newOperation(null, h, h);
	}

}
