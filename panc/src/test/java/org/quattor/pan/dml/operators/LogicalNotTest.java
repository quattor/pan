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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/LogicalNotTest.java $
 $Id: LogicalNotTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.AbstractOperationTestUtils;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;

public class LogicalNotTest extends AbstractOperationTestUtils {

	@Test
	public void testBoolean() throws SyntaxException {

		boolean va = true;

		// Execute the operations.
		Operation lnot = LogicalNot.newOperation(null, BooleanProperty
				.getInstance(va));

		// Pull the value off the stack.
		assertTrue(lnot instanceof BooleanProperty);
		BooleanProperty result = (BooleanProperty) lnot;
		boolean bvalue = result.getValue().booleanValue();
		assertTrue(va != bvalue);
	}

	@Test(expected = AssertionError.class)
	public void testUnderflow() throws SyntaxException {
		LogicalNot.newOperation(null);
	}

	@Test(expected = AssertionError.class)
	public void testOverflow() throws SyntaxException {
		BooleanProperty b = BooleanProperty.getInstance(true);
		LogicalNot.newOperation(null, b, b);
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidArgument() throws SyntaxException {
		HashResource h = new HashResource();
		LogicalNot.newOperation(null, h);
	}

}
