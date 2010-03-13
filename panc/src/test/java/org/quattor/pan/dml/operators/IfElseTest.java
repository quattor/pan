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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/IfElseTest.java $
 $Id: IfElseTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.AbstractOperationTestUtils;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.SyntaxException;

public class IfElseTest extends AbstractOperationTestUtils {

	@Test
	public void testIfTrue() throws SyntaxException {

		long va = 1L;
		Operation condition = BooleanProperty.TRUE;
		Operation trueClause = LongProperty.getInstance(va);
		Operation ifstatement = IfElse
				.newOperation(null, condition, trueClause);

		assertTrue(ifstatement instanceof LongProperty);
		LongProperty result = (LongProperty) ifstatement;
		long lvalue = result.getValue().longValue();
		assertTrue(va == lvalue);
	}

	@Test
	public void testIfFalse() throws SyntaxException {

		long va = 1L;
		Operation condition = BooleanProperty.FALSE;
		Operation trueClause = LongProperty.getInstance(va);
		Operation ifstatement = IfElse
				.newOperation(null, condition, trueClause);

		assertTrue(ifstatement instanceof Undef);
	}

	@Test
	public void testIfElseTrue() throws SyntaxException {

		long va = 1L;
		long vb = 2L;
		Operation condition = BooleanProperty.TRUE;
		Operation trueClause = LongProperty.getInstance(va);
		Operation falseClause = LongProperty.getInstance(vb);
		Operation ifstatement = IfElse.newOperation(null, condition,
				trueClause, falseClause);

		assertTrue(ifstatement instanceof LongProperty);
		LongProperty result = (LongProperty) ifstatement;
		long lvalue = result.getValue().longValue();
		assertTrue(va == lvalue);
	}

	@Test
	public void testIfElseFalse() throws SyntaxException {

		long va = 1L;
		long vb = 2L;
		Operation condition = BooleanProperty.FALSE;
		Operation trueClause = LongProperty.getInstance(va);
		Operation falseClause = LongProperty.getInstance(vb);
		Operation ifstatement = IfElse.newOperation(null, condition,
				trueClause, falseClause);

		assertTrue(ifstatement instanceof LongProperty);
		LongProperty result = (LongProperty) ifstatement;
		long lvalue = result.getValue().longValue();
		assertTrue(vb == lvalue);
	}

	@Test(expected = AssertionError.class)
	public void testUnderflow() throws SyntaxException {
		BooleanProperty b = BooleanProperty.TRUE;
		IfElse.newOperation(null, b);
	}

	@Test(expected = AssertionError.class)
	public void testOverflow() throws SyntaxException {
		BooleanProperty b = BooleanProperty.TRUE;
		IfElse.newOperation(null, b, b, b, b);
	}

	@Test(expected = SyntaxException.class)
	public void testInvalidArguments() throws SyntaxException {
		HashResource h = new HashResource();
		Operation trueClause = LongProperty.getInstance(1L);
		Operation falseClause = LongProperty.getInstance(1L);
		IfElse.newOperation(null, h, trueClause, falseClause);
	}

}
