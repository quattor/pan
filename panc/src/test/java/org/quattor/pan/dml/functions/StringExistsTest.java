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
 $Id: ExistsTest.java 2618 2007-12-08 16:32:02Z loomis $
 */

package org.quattor.pan.dml.functions;

import org.junit.Test;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.operators.Variable;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.CompileTimeContext;
import org.quattor.pan.ttemplate.Context;

public class StringExistsTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(StringExists.class);
	}

	// Note: Most testing of this class is done though the full processing
	// tests because setting up the environment is too difficult in a
	// standalone unit test.

	@Test
	public void validArgument() throws SyntaxException {
		StringExists.getInstance(null, StringProperty.getInstance("/dummy"));
	}

	@Test(expected = CompilerError.class)
	public void invalidArgument() throws SyntaxException {
		StringExists.getInstance(null, Variable.getInstance(null, "dummy"));
	}

	@Test(expected = SyntaxException.class)
	public void invalidArgumentType() throws SyntaxException {
		StringExists.getInstance(null, LongProperty.getInstance(1L));
	}

	@Test(expected = EvaluationException.class)
	public void invalidEmptyString() throws SyntaxException {
		runDml(StringExists.getInstance(null, StringProperty.getInstance("")));
	}

	@Test(expected = EvaluationException.class)
	public void invalidTemplateName() throws SyntaxException {
		runDml(StringExists.getInstance(null, StringProperty
				.getInstance("-not-valid-name/really")));
	}

	@Test(expected = EvaluationException.class)
	public void invalidStringTerm() throws SyntaxException {
		runDml(StringExists.getInstance(null, StringProperty
				.getInstance("/-invalid/term")));
	}

	@Test(expected = EvaluationException.class)
	public void invalidIndexTerm() throws SyntaxException {
		runDml(StringExists.getInstance(null, StringProperty
				.getInstance("/alpha/0xfg")));
	}

	@Test(expected = EvaluationException.class)
	public void verifyNoCompileTimeEval() throws SyntaxException {

		Context compileTimeContext = new CompileTimeContext();
		Operation op = StringExists.getInstance(null, StringProperty
				.getInstance("some/template"));
		op.execute(compileTimeContext);

	}

	@Test(expected = SyntaxException.class)
	public void testTooFewArguments() throws SyntaxException {
		StringExists.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void testTooManyArguments() throws SyntaxException {
		StringExists.getInstance(null, StringProperty.getInstance("OK"),
				StringProperty.getInstance("OK"));
	}

}
