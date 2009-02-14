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
import static org.quattor.pan.utils.MessageUtils.MSG_VALUE_CANNOT_BE_NULL;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.dml.operators.Variable;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Will append a value to the end of SELF. It will create the referenced list if
 * necessary.
 * 
 * @author loomis
 * 
 */
public class ListAppend extends Append {

	private static final long serialVersionUID = -3320401357533459385L;

	protected ListAppend(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super(sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		assert (operations.length == 2);

		// Value to add to list cannot be an explicit null.
		Operation value = operations[0];
		if (value instanceof Null) {
			throw SyntaxException.create(sourceRange, MSG_VALUE_CANNOT_BE_NULL,
					"append");
		}

		// More checking for two-argument version.
		Operation op = operations[0];
		if (operations.length == 2) {

			if (op instanceof Element) {

				// If the first argument is an element, then it must be a list.
				if (!(operations[0] instanceof ListResource)) {
					throw SyntaxException.create(sourceRange,
							MSG_FIRST_ARG_LIST_OR_VARIABLE_REF, "append");
				}

			} else {

				// If the first argument isn't an element, then it must be a
				// variable reference.
				if (!(op instanceof Variable)) {
					throw SyntaxException.create(sourceRange,
							MSG_FIRST_ARG_LIST_OR_VARIABLE_REF, "append");
				}
			}
		}

		return new ListAppend(sourceRange, operations);
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
		if (result.isProtected()) {
			result = (ListResource) result.writableCopy();
		}

		// Append the value to the end of the list.
		result.append(args[1]);

		return result;
	}

}
