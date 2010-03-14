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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/ModTest.java $
 $Id: ModTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.AbstractOperationTestUtils;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;

public class ModTest extends AbstractOperationTestUtils {

	@Test
	public void testLongs() throws SyntaxException {

		long va = 3;
		long vb = 2;

		// Execute the operations.
		Operation mod = Mod.newOperation(null, LongProperty.getInstance(va),
				LongProperty.getInstance(vb));

		// Check result.
		assertTrue(mod instanceof LongProperty);
		LongProperty result = (LongProperty) mod;
		long lvalue = result.getValue().longValue();
		assertTrue((va % vb) == lvalue);
	}

	@Test(expected = SyntaxException.class)
	public void testIllegalDivisor() throws SyntaxException {
		Mod.newOperation(null, LongProperty.getInstance(3), LongProperty
				.getInstance(0));
	}

	@Test(expected = AssertionError.class)
	public void testUnderflow() throws SyntaxException {
		Mod.newOperation(null);
	}

	@Test(expected = AssertionError.class)
	public void testOverflow() throws SyntaxException {
		LongProperty i = LongProperty.getInstance(1L);
		Mod.newOperation(null, i, i, i);
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidArgument() throws SyntaxException {
		HashResource h = new HashResource();
		Mod.newOperation(null, h, h);
	}
}
