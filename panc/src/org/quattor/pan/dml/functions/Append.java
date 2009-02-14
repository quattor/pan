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

import static org.quattor.pan.utils.MessageUtils.MSG_ONE_OR_TWO_ARGS_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_VALUE_CANNOT_BE_NULL;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.SourceRange;

/**
 * Will append a value to the end of a list. It will create the referenced list
 * if necessary.
 * 
 * @author loomis
 * 
 */
abstract public class Append extends BuiltInFunction {

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

		Operation op = null;
		if (operations.length == 1) {
			op = SelfAppend.getInstance(sourceRange, operations);
		} else if (operations.length == 2) {
			op = ListAppend.getInstance(sourceRange, operations);
		}

		return op;
	}

}
