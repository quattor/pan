/*
 Copyright (c) 2013 Centre National de la Recherche Scientifique (CNRS).

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

import static org.quattor.pan.utils.MessageUtils.MSG_ARG_NOT_PROPERTY;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_RADIX;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_RADIX_TYPE;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_STRING_FOR_LONG;
import static org.quattor.pan.utils.MessageUtils.MSG_TWO_ARGS_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_UNKNOWN_PROPERTY_TYPE;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Convert the function's argument to a long value using the given radix.
 * 
 * @author loomis
 * 
 */
final public class ToLongRadix extends BuiltInFunction {

	private ToLongRadix(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("to_long", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		if (operations.length != 2) {
			throw SyntaxException.create(sourceRange, MSG_TWO_ARGS_REQ,
					"radix with to_long");
		}

		return new ToLongRadix(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 2);

		Element result = ops[0].execute(context);
		Element radixProperty = ops[1].execute(context);

		int radix = getRadix(context, radixProperty);

		try {
			Element value = null;

			Property property = (Property) result;
			if (property instanceof BooleanProperty) {
				long x = ((BooleanProperty) property).getValue().booleanValue() ? 1L
						: 0L;
				value = LongProperty.getInstance(x);
			} else if (property instanceof DoubleProperty) {
				double x = ((DoubleProperty) property).getValue().doubleValue();
				value = LongProperty.getInstance(Math.round(x));
			} else if (property instanceof StringProperty) {
				String x = ((StringProperty) property).getValue();
				try {
					value = LongProperty.getInstance(Long.parseLong(x, radix));
				} catch (NumberFormatException nfe) {
					throw EvaluationException.create(sourceRange, context,
							MSG_INVALID_STRING_FOR_LONG, x);
				}
			} else if (property instanceof LongProperty) {
				value = result;
			} else {
				throw CompilerError.create(MSG_UNKNOWN_PROPERTY_TYPE,
						property.getClass());
			}
			return value;
		} catch (ClassCastException cce) {
			throw EvaluationException.create(sourceRange, context,
					MSG_ARG_NOT_PROPERTY, result.getTypeAsString());
		}
	}

	private int getRadix(Context context, Element e) {
		try {
			long radix = 0L;

			Property property = (Property) e;
			if (property instanceof LongProperty) {
				radix = ((LongProperty) e).getValue();
				if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
					throw EvaluationException.create(sourceRange, context,
							MSG_INVALID_RADIX, radix, Character.MIN_RADIX,
							Character.MAX_RADIX);
				}
			} else {
				throw EvaluationException.create(MSG_INVALID_RADIX_TYPE,
						property.getClass());
			}
			return (int) radix;
		} catch (ClassCastException cce) {
			throw EvaluationException.create(sourceRange, context,
					MSG_ARG_NOT_PROPERTY, e.getTypeAsString());
		}

	}

}
