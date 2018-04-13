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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/LogicalNE.java $
 $Id: LogicalNE.java 3515 2008-07-31 13:20:05Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ARGS_NE;
import static org.quattor.pan.utils.MessageUtils.MSG_MISMATCHED_ARGS_NE;

import org.quattor.pan.dml.AbstractOperation;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.NumberProperty;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.utils.MessageUtils;

/**
 * Implements a not-equal operation for longs, doubles, booleans and strings.
 * 
 * @author loomis
 * 
 */
final public class LogicalNE extends AbstractOperation {

	private LogicalNE(SourceRange sourceRange, Operation... operations) {
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
				Property a = (Property) ops[0];
				Property b = (Property) ops[1];
				result = execute(sourceRange, a, b);
			} catch (ClassCastException cce) {
				throw new EvaluationException(MessageUtils
						.format(MSG_INVALID_ARGS_NE), sourceRange);
			} catch (EvaluationException ee) {
				throw SyntaxException.create(sourceRange, ee);
			}

		} else {
			result = new LogicalNE(sourceRange, ops);
		}

		return result;
	}

	@Override
	public Element execute(Context context) {

		try {
			Element[] args = calculateArgs(context);
			Property a = (Property) args[0];
			Property b = (Property) args[1];
			return execute(sourceRange, a, b);
		} catch (ClassCastException cce) {
			throw new EvaluationException(MessageUtils
					.format(MSG_INVALID_ARGS_NE), sourceRange);
		}

	}

	private static Element execute(SourceRange sourceRange, Property a,
			Property b) {

		Element result = null;

		if ((a instanceof NumberProperty) && (b instanceof NumberProperty)) {

			double d1 = ((NumberProperty) a).doubleValue();
			double d2 = ((NumberProperty) b).doubleValue();

			result = BooleanProperty.getInstance(d1 != d2);

		} else if ((a instanceof StringProperty)
				&& (b instanceof StringProperty)) {

			String s1 = (String) a.getValue();
			String s2 = (String) b.getValue();

			result = BooleanProperty.getInstance(!s1.equals(s2));

		} else if ((a instanceof BooleanProperty) && (b instanceof BooleanProperty)) {

			Boolean b1 = (Boolean) a.getValue();
			Boolean b2 = (Boolean) b.getValue();

			result = BooleanProperty.getInstance(!b1.equals(b2));

		} else {
			throw new EvaluationException(MessageUtils
					.format(MSG_MISMATCHED_ARGS_NE), sourceRange);
		}

		return result;
	}
}
