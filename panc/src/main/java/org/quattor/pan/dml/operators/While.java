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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/While.java $
 $Id: While.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_LOOP_TEST;
import static org.quattor.pan.utils.MessageUtils.MSG_LOOP_ITERATION_LIMIT;

import org.quattor.pan.dml.AbstractOperation;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.utils.MessageUtils;

/**
 * Implements a while operation. If the body of the loop is never run, the
 * result is Undef; otherwise, the result is that of the last operation executed
 * in the body of the loop.
 * 
 * @author loomis
 * 
 */
final public class While extends AbstractOperation {

	private While(SourceRange sourceRange, Operation... operations) {
		super(sourceRange, operations);
		assert (operations.length == 2);
	}

	public static Operation newOperation(SourceRange sourceRange,
			Operation... ops) throws SyntaxException {

		assert (ops.length == 2);

		Operation result = null;

		// Can only optimize if condition is an element. Either raise an
		// exception or collapse while to undef if the condition is false.
		if (ops[0] instanceof Element) {

			try {
				BooleanProperty a = (BooleanProperty) ops[0];
				if (!a.getValue().booleanValue()) {
					result = Undef.VALUE;
				}

			} catch (ClassCastException cce) {
				throw SyntaxException
						.create(sourceRange, MSG_INVALID_LOOP_TEST);
			}
		}

		// Can't optimize, just create while operation.
		if (result == null) {
			result = new While(sourceRange, ops);
		}

		return result;
	}

	@Override
	public Element execute(Context context) {

		Operation condition = ops[0];
		Operation body = ops[1];

		Element result = Undef.VALUE;

		// Get the result of the condition.
		boolean test = false;
		try {
			test = ((BooleanProperty) condition.execute(context)).getValue()
					.booleanValue();
		} catch (ClassCastException cce) {
			throw new EvaluationException(MessageUtils
					.format(MSG_INVALID_LOOP_TEST), sourceRange, context);
		}

		// Retrieve the iteration limit from the context.
		int limit = context.getIterationLimit();

		int count = 0;
		while (test) {

			// Increment the counter and ensure that the iteration limit hasn't
			// been exceeded.
			count++;
			if (count > limit) {
				throw new EvaluationException(MessageUtils.format(
						MSG_LOOP_ITERATION_LIMIT, Long.valueOf(limit)),
						sourceRange, context);
			}

			// Execute the body of the loop.
			result = body.execute(context);

			// Re-evaluate the loop condition.
			try {
				test = ((BooleanProperty) condition.execute(context))
						.getValue().booleanValue();
			} catch (ClassCastException cce) {
				throw new EvaluationException(MessageUtils
						.format(MSG_INVALID_LOOP_TEST), sourceRange, context);
			}

		}

		return result;
	}
}
