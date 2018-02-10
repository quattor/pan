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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/Sub.java $
 $Id: Sub.java 3515 2008-07-31 13:20:05Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ARGS_SUB;

import org.quattor.pan.dml.AbstractOperation;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.NumberProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.utils.MessageUtils;

/**
 * Implements a subtract operation for longs and doubles. If one of the
 * arguments is a double, then the result is a double. Otherwise, the result is
 * a long.
 * 
 * @author loomis
 * 
 */
final public class Sub extends AbstractOperation {

	private Sub(SourceRange sourceRange, Operation... operations) {
		super(sourceRange, operations);
		assert (operations.length == 2);
	}

	public static Operation newOperation(SourceRange sourceRange,
			Operation... ops) throws SyntaxException {

		assert (ops.length == 2);

		Operation result = null;

		// Attempt to optimize this operation.
		if (ops[0] instanceof Element && ops[1] instanceof Element) {

			try {
				NumberProperty a = (NumberProperty) ops[0];
				NumberProperty b = (NumberProperty) ops[1];
				result = execute(a, b);
			} catch (ClassCastException cce) {
				throw new EvaluationException(MessageUtils
						.format(MSG_INVALID_ARGS_SUB), sourceRange);
			} catch (EvaluationException ee) {
				throw SyntaxException.create(sourceRange, ee);
			}

		} else {
			result = new Sub(sourceRange, ops);
		}

		return result;
	}

	@Override
	public Element execute(Context context) {

		try {
			Element[] args = calculateArgs(context);
			NumberProperty a = (NumberProperty) args[0];
			NumberProperty b = (NumberProperty) args[1];
			return execute(a, b);
		} catch (ClassCastException cce) {
			throw new EvaluationException(MessageUtils
					.format(MSG_INVALID_ARGS_SUB), sourceRange);
		}

	}

	public static Element execute(NumberProperty a, NumberProperty b) {

		Element result = null;

		if ((a instanceof LongProperty) && (b instanceof LongProperty)) {

			long l1 = ((Long) a.getValue()).longValue();
			long l2 = ((Long) b.getValue()).longValue();

			result = LongProperty.getInstance(l1 - l2);

		} else {

			double d1 = a.doubleValue();
			double d2 = b.doubleValue();

			result = DoubleProperty.getInstance(d1 - d2);
		}

		return result;
	}

}
