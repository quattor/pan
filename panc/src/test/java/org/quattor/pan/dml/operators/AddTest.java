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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/AddTest.java $
 $Id: AddTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.AbstractOperationTestUtils;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;

public class AddTest extends AbstractOperationTestUtils {

	@Test
	public void addLongs() throws SyntaxException {

		long a = 1;
		long b = 2;

		// Create the add operation.
		Operation add = Add.newOperation(null, LongProperty.getInstance(a),
				LongProperty.getInstance(b));

		// Because of optimization there is no need to run the operation.
		assertTrue(add instanceof LongProperty);
		LongProperty r = (LongProperty) add;
		Long lvalue = (Long) r.getValue();
		assertTrue((a + b) == lvalue.longValue());
	}

	@Test
	public void addDoubles() throws SyntaxException {

		double a = 1.0;
		double b = 2.0;

		// Create the add operation.
		Operation add = Add.newOperation(null, DoubleProperty.getInstance(a),
				DoubleProperty.getInstance(b));

		// Because of optimization there is no need to run the operation.
		assertTrue(add instanceof DoubleProperty);
		DoubleProperty r = (DoubleProperty) add;
		Double dvalue = (Double) r.getValue();
		assertTrue((a + b) == dvalue.doubleValue());
	}

	@Test
	public void addMixed() throws SyntaxException {

		long a = 1;
		double b = 2.0;

		// Create the add operation.
		Operation add = Add.newOperation(null, LongProperty.getInstance(a),
				DoubleProperty.getInstance(b));

		// Because of optimization there is no need to run the operation.
		assertTrue(add instanceof DoubleProperty);
		DoubleProperty r = (DoubleProperty) add;
		Double dvalue = (Double) r.getValue();
		assertTrue((a + b) == dvalue.doubleValue());
	}

	@Test
	public void addStrings() throws SyntaxException {

		String a = "one";
		String b = "two";

		// Create the add operation.
		Operation add = Add.newOperation(null, StringProperty.getInstance(a),
				StringProperty.getInstance(b));

		// Because of optimization there is no need to run the operation.
		assertTrue(add instanceof StringProperty);
		StringProperty r = (StringProperty) add;
		String svalue = (String) r.getValue();
		assertTrue((a + b).equals(svalue));
	}

	@Test(expected = SyntaxException.class)
	public void testOptimizationError() throws SyntaxException {

		Long a = 1L;
		String b = "two";

		// Incompatible arguments should throw exception.
		Add.newOperation(null, LongProperty.getInstance(a), StringProperty
				.getInstance(b));
	}

	@Test(expected = AssertionError.class)
	public void testUnderflow() throws SyntaxException {
		Add.newOperation(null);
	}

	@Test(expected = AssertionError.class)
	public void testOverflow() throws SyntaxException {
		LongProperty i = LongProperty.getInstance(1L);
		Add.newOperation(null, i, i, i);
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidArguments() throws SyntaxException {
		HashResource h = new HashResource();
		runDml(Add.newOperation(null, h, h));
	}

}
