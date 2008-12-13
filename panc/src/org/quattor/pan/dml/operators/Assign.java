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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/Assign.java $
 $Id: Assign.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_OPERATION_IN_ASSIGN;

import org.quattor.pan.dml.AbstractOperation;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Implements the assignment operation within a DML block. This operation
 * actually calculates the result, then uses a series of SetValue operations to
 * assign this value to one or more variables.
 * 
 * @author loomis
 * 
 */
final public class Assign extends AbstractOperation {

	private static final long serialVersionUID = -1998355450850171964L;

	public Assign(SourceRange sourceRange, Operation... operations) {
		super(sourceRange, operations);
		assert (operations.length >= 2);

		// Sanity check. All operations but the first must be SetValue
		// operations.
		for (int i = 1; i < operations.length; i++) {
			if (!(operations[i] instanceof SetValue)) {
				throw CompilerError.create(
						MSG_INVALID_OPERATION_IN_ASSIGN, operations[i]
								.getClass());
			}
		}
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length >= 2);

		Element result = ops[0].execute(context);

		for (int i = 1; i < ops.length; i++) {
			SetValue op = (SetValue) ops[i];
			result = op.execute(context, result);
		}

		return result;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + (ops.length - 1) + ")";
	}

}
