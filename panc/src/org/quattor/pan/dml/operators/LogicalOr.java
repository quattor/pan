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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/LogicalOr.java $
 $Id: LogicalOr.java 3515 2008-07-31 13:20:05Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ARGS_L_OR;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_FIRST_ARG_L_OR;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_SECOND_ARG_L_OR;

import org.quattor.pan.dml.AbstractOperation;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.MessageUtils;

/**
 * Implements an or operation for boolean arguments.
 * 
 * @author loomis
 * 
 */
final public class LogicalOr extends AbstractOperation {

	private static final long serialVersionUID = 2511688109545695762L;

	private LogicalOr(SourceRange sourceRange, Operation... operations) {
		super(sourceRange, operations);
		assert (operations.length == 2);
	}

	public static Operation newOperation(SourceRange sourceRange,
			Operation... ops) throws SyntaxException {

		assert (ops.length == 2);

		Operation result = null;

		// Because of the short-circuit logic only a few discrete cases can be
		// optimized.

		// Check if the first argument is true. If so, result is true. Check
		// for type errors at the same time.
		if (ops[0] instanceof Element) {
			try {
				BooleanProperty a = (BooleanProperty) ops[0];
				if (a.getValue().booleanValue()) {
					result = BooleanProperty.TRUE;
				}
			} catch (ClassCastException cce) {
				throw SyntaxException.create(sourceRange,
						MSG_INVALID_FIRST_ARG_L_OR);
			}
		}

		// If the second argument is already an Element, check that it is a
		// boolean.
		if (result == null && ops[1] instanceof Element
				&& !(ops[1] instanceof BooleanProperty)) {
			throw SyntaxException.create(sourceRange,
					MSG_INVALID_SECOND_ARG_L_OR);
		}

		// Other trivial case, is where both arguments are booleans.
		if (result == null && ops[0] instanceof Element
				&& ops[1] instanceof Element) {
			try {
				BooleanProperty a = (BooleanProperty) ops[0];
				BooleanProperty b = (BooleanProperty) ops[1];
				boolean r = a.getValue().booleanValue()
						|| b.getValue().booleanValue();
				result = BooleanProperty.getInstance(r);
			} catch (ClassCastException cce) {
				throw SyntaxException
						.create(sourceRange, MSG_INVALID_ARGS_L_OR);
			}
		}

		// Other cases require that the operation be evaluated to catch cases
		// where the computed values might not be booleans.
		if (result == null) {
			result = new LogicalOr(sourceRange, ops);
		}

		return result;
	}

	@Override
	public Element execute(Context context) {

		// Get the result of the first argument.
		BooleanProperty a = null;
		try {
			a = (BooleanProperty) ops[0].execute(context);
		} catch (ClassCastException cce) {
			throw new EvaluationException(MessageUtils
					.format(MSG_INVALID_FIRST_ARG_L_OR), sourceRange);
		}

		// If we already know that the first value is true, there is no need to
		// evaluate the second argument.
		if (a.getValue().booleanValue()) {
			return a;
		}

		// Get the result of the second argument.
		BooleanProperty b = null;
		try {
			b = (BooleanProperty) ops[1].execute(context);
		} catch (ClassCastException cce) {
			throw new EvaluationException(MessageUtils
					.format(MSG_INVALID_SECOND_ARG_L_OR), sourceRange);
		}

		// The value of the expression must be the same as the second argument.
		return b;
	}

}
