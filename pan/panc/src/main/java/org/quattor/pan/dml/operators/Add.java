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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/Add.java $
 $Id: Add.java 3515 2008-07-31 13:20:05Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ARGS_ADD;
import static org.quattor.pan.utils.MessageUtils.MSG_MISMATCHED_ARGS_ADD;

import org.quattor.pan.dml.AbstractOperation;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.NumberProperty;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.MessageUtils;

/**
 * Implements an addition operation for longs, doubles, and strings. If this is
 * a numeric operation and one of the arguments is a double, the result is a
 * double; otherwise, the result is a long.
 * 
 * @author loomis
 * 
 */
final public class Add extends AbstractOperation {

	/**
	 * Create a new add operation. The source range must be supplied.
	 * 
	 * @param sourceRange
	 */
	private Add(SourceRange sourceRange, Operation... ops) {
		super(sourceRange, ops);
		assert (ops.length == 2);
	}

	/**
	 * Factory class for creating a new Add operation. Because of optimization,
	 * this class may actually return an element if the result can be determined
	 * at compile time.
	 * 
	 * @param sourceRange
	 *            location of this operation in the source
	 * @param ops
	 *            arguments for this operation
	 * 
	 * @return optimized Operation for this Add expression
	 */
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
						.format(MSG_INVALID_ARGS_ADD), sourceRange);
			} catch (EvaluationException ee) {
				throw SyntaxException.create(sourceRange, ee);
			}

		} else {
			result = new Add(sourceRange, ops);
		}

		return result;
	}

	/**
	 * Perform the addition of the two top values on the data stack in the given
	 * DMLContext. This method will handle long, double, and string values.
	 * Exceptions are thrown if the types of the arguments do not match or they
	 * are another type.
	 */
	@Override
	public Element execute(Context context) {

		try {
			Element[] args = calculateArgs(context);
			Property a = (Property) args[0];
			Property b = (Property) args[1];
			return execute(sourceRange, a, b);
		} catch (ClassCastException cce) {
			throw new EvaluationException(MessageUtils
					.format(MSG_INVALID_ARGS_ADD), sourceRange);
		}
	}

	/**
	 * Do the actual addition.
	 */
	private static Element execute(SourceRange sourceRange, Property a,
			Property b) {

		assert (a != null);
		assert (b != null);

		Element result = null;

		if ((a instanceof LongProperty) && (b instanceof LongProperty)) {

			long l1 = ((Long) a.getValue()).longValue();
			long l2 = ((Long) b.getValue()).longValue();

			result = LongProperty.getInstance(l1 + l2);

		} else if ((a instanceof NumberProperty)
				&& (b instanceof NumberProperty)) {

			double d1 = ((NumberProperty) a).doubleValue();
			double d2 = ((NumberProperty) b).doubleValue();

			result = DoubleProperty.getInstance(d1 + d2);

		} else if ((a instanceof StringProperty)
				&& (b instanceof StringProperty)) {

			String s1 = (String) a.getValue();
			String s2 = (String) b.getValue();

			result = StringProperty.getInstance(s1 + s2);

		} else {
			throw new EvaluationException(MessageUtils
					.format(MSG_MISMATCHED_ARGS_ADD), sourceRange);
		}

		return result;
	}

}
