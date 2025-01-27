/*
 Copyright (c) 2025 Charles A. Loomis, Jr, Cedric Duprilot,
 Centre National de la Recherche Scientifique (CNRS),
 James Adams and UK Research and Innovation (UKRI).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_TWO_ARGS_REQ;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.NumberProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;


/**
 * Return the smallest value from arguments
 *
 * @author jrha
 *
 */
final public class Min extends BuiltInFunction {

	private Min(SourceRange sourceRange, Operation... operations) throws SyntaxException {
		super("min", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange, Operation... operations) throws SyntaxException {
		// Ensure that there are at exactly two arguments.
		if (operations.length != 2) {
			throw SyntaxException.create(sourceRange, MSG_TWO_ARGS_REQ, "min");
		}
		return new Min(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 2);

		Element[] args = calculateArgs(context);

		assert (args[0] != null);
		assert (args[1] != null);

		Element result = null;

		if ((args[0] instanceof LongProperty) && (args[1] instanceof LongProperty)) {
			// Both args are Long, so return a Long

			long l1 = ((LongProperty) args[0]).getValue();
			long l2 = ((LongProperty) args[1]).getValue();

			result = LongProperty.getInstance(Math.min(l1, l2));

		} else if ((args[0] instanceof NumberProperty) && (args[1] instanceof NumberProperty)) {
			// One or more args is a Double, so return a Double

			double d1 = ((NumberProperty) args[0]).doubleValue();
			double d2 = ((NumberProperty) args[1]).doubleValue();

			result = DoubleProperty.getInstance(Math.min(d1, d2));

		} else {
			throw new EvaluationException("all arguments to min must be longs or doubles", getSourceRange(), context);
		}

		return result;
	}
}
