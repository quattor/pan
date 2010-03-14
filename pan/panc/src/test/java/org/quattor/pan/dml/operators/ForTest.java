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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/ForeachTest.java $
 $Id: ForeachTest.java 3600 2008-08-17 14:48:32Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.AbstractOperationTestUtils;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.CompileTimeContext;
import org.quattor.pan.template.Context;

public class ForTest extends AbstractOperationTestUtils {

	@Test
	public void testNoIterations() throws SyntaxException {

		// Create the loop.
		Operation op = new For(null, LongProperty.getInstance(1L),
				BooleanProperty.FALSE, Undef.VALUE, Undef.VALUE);

		// Run the operation.
		Context context = new CompileTimeContext();
		Element r1 = op.execute(context);

		// Check the value.
		assertTrue(r1 instanceof LongProperty);
		long value = ((LongProperty) r1).getValue();
		assertTrue(value == 1L);
	}

	@Test(expected = AssertionError.class)
	public void testUnderflow() {
		new For(null);
	}

	@Test(expected = AssertionError.class)
	public void testOverflow() throws SyntaxException {
		new For(null, Undef.VALUE, BooleanProperty.FALSE, Undef.VALUE,
				Undef.VALUE, Undef.VALUE);
	}

	@Test(expected = EvaluationException.class)
	public void testInfiniteLoop() throws SyntaxException {

		// Create the loop.
		Operation op = new For(null, Undef.VALUE, BooleanProperty.TRUE,
				Undef.VALUE, Undef.VALUE);

		// This loop should hit the iteration limit and die.
		Context context = new CompileTimeContext();
		op.execute(context);
	}

}
