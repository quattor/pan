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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/TypeStatementTest.java $
 $Id: TypeStatementTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.statement;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;

public class TypeStatementTest extends StatementTestUtils {

	@Test
	public void typeDefinition() throws Exception {

		Context context = setupTemplateToRun2("ts1", "type x = string;", false);

		assertTrue(context.getFullType("x") != null);
	}

	@Test(expected = SyntaxException.class)
	public void illegalNonconstantDefault() throws Exception {

		runExpectingException("ts2", "type x = string = 1+x;");
	}

	@Test(expected = EvaluationException.class)
	public void illegalTypeLoop() throws Exception {
		setupTemplateToRun2("ts3", "type x = x;", false);
	}

}
