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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/branches/7.2/src/org/quattor/pan/statement/StaticIncludeStatementTest.java $
 $Id: StaticIncludeStatementTest.java 1339 2007-02-16 23:14:24Z loomis $
 */

package org.quattor.pan.statement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.operators.NestedVariable;
import org.quattor.pan.dml.operators.SetSelf;
import org.quattor.pan.dml.operators.Variable;
import org.quattor.pan.exceptions.SyntaxException;

public class IncludeStatementTest extends StatementTestUtils {

	private static Operation[] ops = { LongProperty.getInstance(0L),
			StringProperty.getInstance("a") };

	@Test
	public void legalNames() throws Exception {

		// Check some odd, but valid, template names.
		assertTrue(IncludeStatement.validIdentifier("1.2"));
		assertTrue(IncludeStatement.validIdentifier("_"));
		assertTrue(IncludeStatement.validIdentifier("c--"));
		assertTrue(IncludeStatement.validIdentifier("a-b.c"));
	}

	@Test
	public void illegalNames() throws Exception {

		// Check for common illegal names.
		assertFalse(IncludeStatement.validIdentifier("a//b"));
		assertFalse(IncludeStatement.validIdentifier("a/"));
		assertFalse(IncludeStatement.validIdentifier(""));
	}

	@Test
	public void illegalSelfReference() throws SyntaxException {

		Operation[] operations = {
				new SetSelf(null, StringProperty.getInstance("OK")),
				Variable.getInstance(null, "SELF"),
				Variable.getInstance(null, "SELF", ops),
				NestedVariable.getInstance(null, "SELF"),
				NestedVariable.getInstance(null, "SELF", ops), };

		for (Operation op : operations) {

			try {
				IncludeStatement.newIncludeStatement(null, op);

				fail("include statement did not throw SyntaxException for operation of type "
						+ op.getClass().getSimpleName());
			} catch (SyntaxException e) {
				// OK
			}

		}

	}
}
