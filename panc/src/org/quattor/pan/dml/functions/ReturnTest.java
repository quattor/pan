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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/ReturnTest.java $
 $Id: ReturnTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.exceptions.ReturnValueException;
import org.quattor.pan.exceptions.SyntaxException;

public class ReturnTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Return.class);
	}

	@Test
	public void testReturnedValue() throws SyntaxException {

		long va = 1L;

		try {

			// Execute operations.
			runDml(Return.getInstance(null, LongProperty.getInstance(va)));

		} catch (ReturnValueException rve) {

			// Check result.
			Element r1 = rve.getElement();
			assertTrue(r1 instanceof LongProperty);
			LongProperty s1 = (LongProperty) r1;
			Long sresult = (Long) s1.getValue();
			assertTrue(va == sresult.longValue());
		}
	}

	@Test(expected = SyntaxException.class)
	public void testTooFewArguments() throws SyntaxException {
		Return.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void testTooManyArguments() throws SyntaxException {
		Return.getInstance(null, LongProperty.getInstance(1L), LongProperty
				.getInstance(2L));
	}

}
