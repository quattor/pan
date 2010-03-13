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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/VariableTest.java $
 $Id: VariableTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.AbstractOperationTestUtils;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.SyntaxException;

public class VariableTest extends AbstractOperationTestUtils {

	@Test
	public void testSimpleVariable() throws SyntaxException {

		Operation op = Variable.getInstance(null, "x");
		assertTrue(op instanceof SimpleVariable);
	}

	@Test
	public void testSelfSimpleVariable() throws SyntaxException {

		Operation op = Variable.getInstance(null, "SELF");
		assertTrue(op instanceof SelfSimpleVariable);
	}

	@Test
	public void testNestedVariable() throws SyntaxException {

		Operation op = Variable.getInstance(null, "x", StringProperty
				.getInstance("OK"));
		assertTrue(op instanceof NestedVariable);
	}

	@Test
	public void testSelfNestedVariable() throws SyntaxException {

		Operation op = Variable.getInstance(null, "SELF", StringProperty
				.getInstance("OK"));
		assertTrue(op instanceof SelfNestedVariable);
	}

}
