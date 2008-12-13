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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/ExistsTest.java $
 $Id: ExistsTest.java 2861 2008-02-06 08:40:49Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.SyntaxException;

public class PathExistsTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(PathExists.class);
	}

	@Test
	public void testAbsolutePathOK() throws SyntaxException {
		Operation op = PathExists.getInstance(null, StringProperty
				.getInstance("/path"));
		assertTrue(op instanceof PathExists);
	}

	@Test
	public void testExternalPathOK() throws SyntaxException {
		Operation op = PathExists.getInstance(null, StringProperty
				.getInstance("machine:/path"));
		assertTrue(op instanceof PathExists);
	}

	@Test(expected = SyntaxException.class)
	public void testRelativePathFails() throws SyntaxException {
		PathExists.getInstance(null, StringProperty.getInstance("relative"));
	}

	@Test(expected = SyntaxException.class)
	public void testInvalidArgumentType() throws SyntaxException {
		PathExists.getInstance(null, LongProperty.getInstance(1L));
	}

	@Test(expected = SyntaxException.class)
	public void testTooFewArguments() throws SyntaxException {
		PathExists.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void testTooManyArguments() throws SyntaxException {
		PathExists.getInstance(null, StringProperty.getInstance("OK"),
				StringProperty.getInstance("OK"));
	}

}
