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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Base64DecodeTest.java $
 $Id: Base64DecodeTest.java 1042 2006-11-28 10:04:35Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.SyntaxException;

public class DeprecatedTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Deprecated.class);
	}

	@Test
	public void testInstanceType() throws SyntaxException {
		Operation op = Deprecated.getInstance(null, LongProperty
				.getInstance(1L), StringProperty.getInstance("message"));
		assertTrue(op instanceof Deprecated);
	}

	@Test(expected = SyntaxException.class)
	public void testInvalidFirstArgument() throws SyntaxException {
		Deprecated.getInstance(null, StringProperty.getInstance("message"),
				StringProperty.getInstance("message"));
	}

	@Test(expected = SyntaxException.class)
	public void testInvalidFirstArgument2() throws SyntaxException {
		Deprecated.getInstance(null, LongProperty.getInstance(-1L),
				StringProperty.getInstance("message"));
	}

	@Test(expected = SyntaxException.class)
	public void testInvalidSecondArgument() throws SyntaxException {
		Deprecated.getInstance(null, LongProperty.getInstance(1L), LongProperty
				.getInstance(1L));
	}

	@Test(expected = SyntaxException.class)
	public void testTooFewArguments() throws SyntaxException {
		Deprecated.getInstance(null, LongProperty.getInstance(1L));
	}

	@Test(expected = SyntaxException.class)
	public void testTooManyArguments() throws SyntaxException {
		Deprecated.getInstance(null, LongProperty.getInstance(1L),
				StringProperty.getInstance("message"), StringProperty
						.getInstance("message"));
	}

}
