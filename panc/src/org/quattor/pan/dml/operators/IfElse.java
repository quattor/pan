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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/IfElse.java $
 $Id: IfElse.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_IF_ELSE_TEST;

import org.quattor.pan.dml.AbstractOperation;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.MessageUtils;

/**
 * Implements an if operation with an optional else clause.
 * 
 * @author loomis
 * 
 */
final public class IfElse extends AbstractOperation {

	private static final long serialVersionUID = -7433685902230120533L;

	private IfElse(SourceRange sourceRange, Operation... operations) {
		super(sourceRange, operations);
		assert (operations.length == 2 || operations.length == 3);
	}

	public static Operation newOperation(SourceRange sourceRange,
			Operation... ops) throws SyntaxException {

		assert (ops.length == 2 || ops.length == 3);

		Operation result = null;

		// Can only optimize if the first argument is an element.
		if (ops[0] instanceof Element) {

			try {
				BooleanProperty a = (BooleanProperty) ops[0];
				if (a.getValue().booleanValue()) {
					result = ops[1];
				} else if (ops.length == 3) {
					result = ops[2];
				} else {
					result = Undef.VALUE;
				}

			} catch (ClassCastException cce) {
				throw SyntaxException.create(sourceRange,
						MSG_INVALID_IF_ELSE_TEST);
			}

		} else {
			result = new IfElse(sourceRange, ops);
		}

		return result;
	}

	/**
	 * Perform the if statement. It will pop a boolean from the data stack. If
	 * that boolean is false the next operand if popped from the operand stack;
	 * if true, nothing is done.
	 */
	@Override
	public Element execute(Context context) {

		assert (ops.length == 2 || ops.length == 3);

		// Pop the condition from the data stack.
		boolean condition = false;
		try {
			condition = ((BooleanProperty) ops[0].execute(context)).getValue();
		} catch (ClassCastException cce) {
			throw new EvaluationException(MessageUtils
					.format(MSG_INVALID_IF_ELSE_TEST), sourceRange);
		}

		// Choose the correct operation and execute it.
		Operation op = null;
		if (condition) {
			op = ops[1];
		} else if (ops.length > 2) {
			op = ops[2];
		} else {
			op = Undef.VALUE;
		}
		return op.execute(context);
	}
}
