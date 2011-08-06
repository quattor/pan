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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/BooleanPropertyTest.java $
 $Id: BooleanPropertyTest.java 998 2006-11-15 19:44:28Z loomis $
 */

package org.quattor.pan.dml;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.StringProperty;

/**
 * Test class for testing DML functionality. This inherits from DML so that
 * optimization can be tested.
 * 
 * @author loomis
 * 
 */
public class DMLTest extends DML {

	public DMLTest() {
		super(null);
	}

	// Verify that the optimization of a DML block produces a
	// single operation, if only one operation is in the block.
	// Special functions that make a distinction between
	// identifiers and DML blocks may rely on this optimization
	// for correct code generation.
	@Test
	public void checkSingleOpOptimization() {
		StringProperty str = StringProperty.getInstance("OK");
		Operation dml = DML.getInstance(null, str);
		assertTrue(dml instanceof StringProperty);
		assertTrue(dml == str);
	}

	// Verify that nested DML blocks are inlined. Special functions that
	// distinguish between identifiers and DML blocks may rely on this
	// optimization to generate correct code.
	@Test
	public void checkInliningDMLBlocks() {
		StringProperty str = StringProperty.getInstance("OK");
		Operation op1 = DML.getInstance(null, str, str);
		Operation op2 = DML.getInstance(null, str, op1, str);

		assertTrue(op2 instanceof DML);

		DML dml2 = (DML) op2;

		assertTrue(dml2.ops.length == 4);

		for (Operation op : dml2.ops) {
			assertTrue(op == str);
		}
	}

}
