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
 $Id: Exists.java 2861 2008-02-06 08:40:49Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_ONE_ARG_REQ;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.operators.Variable;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.SourceRange;

/**
 * Tests whether the variable referenced in the function's argument exists.
 * 
 * @author loomis
 * 
 */
abstract public class Exists extends BuiltInFunction {

	protected Exists(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("exists", sourceRange, operations);
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
		assert (operations.length == 1);

		// Check to see if there is only one variable argument. If so, create a
		// Variable operation with the lookupOnly flag set and insert this into
		// the operation list.
		Operation op = null;
		if (operations[0] instanceof Variable) {
			op = VariableExists.getInstance(sourceRange, operations);
		} else {
			op = StringExists.getInstance(sourceRange, operations);
		}

		return op;
	}

}
