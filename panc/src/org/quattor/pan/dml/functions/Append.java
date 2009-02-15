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

import static org.quattor.pan.utils.MessageUtils.MSG_FIRST_ARG_LIST_OR_VARIABLE_REF;
import static org.quattor.pan.utils.MessageUtils.MSG_ONE_OR_TWO_ARGS_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_VALUE_CANNOT_BE_NULL;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.dml.operators.ListVariable;
import org.quattor.pan.dml.operators.Variable;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Will append a value to the end of a list. It will create the referenced list
 * if necessary.
 * 
 * @author loomis
 * 
 */
final public class Append extends BuiltInFunction {

	private static final long serialVersionUID = 5803166359104659514L;

	protected Append(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super(sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// This must have either one or two arguments.
		if (operations.length < 1 || operations.length > 2) {
			throw SyntaxException.create(sourceRange, MSG_ONE_OR_TWO_ARGS_REQ,
					"append");
		}
		assert (operations.length == 1 || operations.length == 2);

		// Value to add to list cannot be an explicit null.
		Operation value = (operations.length == 1) ? operations[0]
				: operations[1];
		if (value instanceof Null) {
			throw SyntaxException.create(sourceRange, MSG_VALUE_CANNOT_BE_NULL,
					"append");
		}

		// For two-argument function call, the first argument must be a list.
		if (operations.length == 2) {
			if (operations[0] instanceof Element) {
				if (!(operations[0] instanceof ListResource)) {
					throw SyntaxException.create(sourceRange,
							MSG_FIRST_ARG_LIST_OR_VARIABLE_REF, "append");
				}
			}
		}

		// Create the appropriate list of operations. The raw operations cannot
		// be used because the variable must be turned into a ListVariable
		// operation.
		Operation[] modifiedOps = new Operation[2];
		if (operations.length == 1) {
			modifiedOps[0] = ListVariable.getInstance(sourceRange, "SELF");
			modifiedOps[1] = operations[0];
		} else if (operations.length == 2) {
			if (operations[0] instanceof Variable) {
				modifiedOps[0] = ListVariable
						.getInstance((Variable) operations[0]);
			} else {
				modifiedOps[0] = operations[0];
			}
			modifiedOps[1] = operations[1];
		}

		return new Append(sourceRange, modifiedOps);
	}

	@Override
	public Element execute(Context context) {

		ListResource result = null;

		// Retrieve the values of the arguments.
		Element[] args = calculateArgs(context);
		assert (ops.length == 2);

		// Check that the value argument is not an explicit null.
		if (args[1] instanceof Null) {
			throw EvaluationException.create(sourceRange,
					MSG_VALUE_CANNOT_BE_NULL, "append");
		}

		// Check that the first argument is a list.
		if (!(args[0] instanceof ListResource)) {
			throw EvaluationException.create(sourceRange,
					MSG_FIRST_ARG_LIST_OR_VARIABLE_REF, "append");
		}

		// The return value is the list argument.
		result = (ListResource) args[0];

		// Although, if the list is protected, then we'll have to create a copy.
		// This may happen if the argument is a constant or coming from
		// something like a value() call.
		if (result.isProtected()) {
			result = (ListResource) result.writableCopy();
		}

		// Append the value to the end of the list.
		result.append(args[1]);

		return result;
	}

}
