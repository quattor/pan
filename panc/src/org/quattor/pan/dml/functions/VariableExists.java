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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Exists.java $
 $Id: Exists.java 2857 2008-02-06 07:23:49Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ARG_IN_CONSTRUCTOR;
import static org.quattor.pan.utils.MessageUtils.MSG_ONE_ARG_REQ;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.operators.Variable;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Tests whether a particular variable reference exists.
 * 
 * @author loomis
 * 
 */
final public class VariableExists extends Exists {

	private static final long serialVersionUID = 3669331052511806968L;

	private VariableExists(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super(sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Ensure that there is exactly one argument. Since the parser does
		// little argument checking for function calls, this explicit check is
		// needed.
		if (operations.length != 1) {
			throw SyntaxException
					.create(sourceRange, MSG_ONE_ARG_REQ, "exists");
		}

		// Check to see if there is only one variable argument. If so, create a
		// Variable operation with the lookupOnly flag set and insert this into
		// the operation list.
		assert (operations.length == 1);
		if (operations[0] instanceof Variable) {
			Variable var = (Variable) operations[0];
			Operation nvar = Variable.getInstance(var, true);
			operations[0] = nvar;
		} else {
			throw CompilerError.create(MSG_INVALID_ARG_IN_CONSTRUCTOR);
		}

		return new VariableExists(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		throwExceptionIfCompileTimeContext(context);

		assert (ops[0] instanceof Variable);

		try {

			Element result = ops[0].execute(context);
			return (result == null) ? BooleanProperty.FALSE
					: BooleanProperty.TRUE;

		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(getSourceRange(), context);
		}

	}
}
