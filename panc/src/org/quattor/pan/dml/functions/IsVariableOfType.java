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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/IsDefined.java $
 $Id: IsDefined.java 2799 2008-01-26 17:10:23Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ARG_IN_CONSTRUCTOR;
import static org.quattor.pan.utils.MessageUtils.MSG_ONE_ARG_REQ;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.operators.Variable;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Tests if the given variable reference is of the given class.
 * 
 * @author loomis
 * 
 */
final public class IsVariableOfType extends IsOfType {

	private IsVariableOfType(SourceRange sourceRange,
			Class<? extends Element> type, String name, Operation... operations)
			throws SyntaxException {
		super(name, sourceRange, type, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Class<? extends Element> type, String name, Operation... operations)
			throws SyntaxException {

		// Ensure that there is exactly one argument. Since the parser does
		// little argument checking for function calls, this explicit check is
		// needed.
		if (operations.length != 1) {
			throw SyntaxException.create(sourceRange, MSG_ONE_ARG_REQ, name);
		}
		assert (operations.length == 1);

		// Check to see if there is only one variable argument. If so, create a
		// Variable operation with the lookupOnly flag set and insert this into
		// the operation list.
		assert (operations.length == 1);
		Operation newop = operations[0];
		if (operations[0] instanceof Variable) {
			Variable var = (Variable) operations[0];
			Operation nvar = Variable.getInstance(var, true);
			operations[0] = nvar;
			newop = nvar;
		} else {
			throw CompilerError.create(MSG_INVALID_ARG_IN_CONSTRUCTOR);
		}

		return new IsVariableOfType(sourceRange, type, name, newop);
	}

	@Override
	public Element execute(Context context) {

		throwExceptionIfCompileTimeContext(context);

		assert (ops.length == 1);

		Element arg = ops[0].execute(context);

		BooleanProperty result = BooleanProperty.FALSE;
		if (arg != null) {
			if (type.isAssignableFrom(arg.getClass())) {
				result = BooleanProperty.TRUE;
			}
		}
		return result;
	}

}
