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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/VariableStatementTest.java $
 $Id: VariableStatementTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.statement;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.ttemplate.Context;

public class VariableStatementTest extends StatementTestUtils {

	@Test
	public void variableDefinition() throws Exception {

		Context context = setupTemplateToRun2("vs1", "variable x = 1;", false);

		Element result = context.getGlobalVariable("x");
		assertTrue(result != null);
		assertTrue(result instanceof LongProperty);
		assertTrue(((LongProperty) result).getValue().longValue() == 1L);
	}

}
