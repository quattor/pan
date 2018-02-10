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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/ToBoolean.java $
 $Id: ToBoolean.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_ONE_ARG_REQ;
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
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;

/**
 * Convert the function's argument to a boolean value.
 * 
 * @author loomis
 * 
 */
final public class ToBoolean extends BuiltInFunction {

	private ToBoolean(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("to_boolean", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Ensure that there is exactly one argument. Since the parser does
		// little argument checking for function calls, this explicit check is
		// needed.
		if (operations.length != 1) {
			throw SyntaxException.create(sourceRange, MSG_ONE_ARG_REQ,
					"to_boolean");
		}

		return new ToBoolean(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 1);

		Element result = ops[0].execute(context);

		try {
			Element value = null;

			Property property = (Property) result;
			if (property instanceof LongProperty) {
				long x = ((LongProperty) property).getValue().longValue();
				value = BooleanProperty.getInstance(x != 0);
			} else if (property instanceof DoubleProperty) {
				double x = ((DoubleProperty) property).getValue().doubleValue();
				value = BooleanProperty.getInstance(x != 0.0);
			} else if (property instanceof StringProperty) {
				String x = ((StringProperty) property).getValue();
				value = BooleanProperty.getInstance(!("".equals(x) || "false"
						.equalsIgnoreCase(x)));
			} else if (property instanceof BooleanProperty) {
				value = result;
			} else {
				throw CompilerError.create(MSG_UNKNOWN_PROPERTY_TYPE, property
						.getClass());
			}
			return value;
		} catch (ClassCastException cce) {
			throw new EvaluationException(
					"to_boolean argument is not a property: "
							+ result.getTypeAsString(), sourceRange, context);
		}
	}

}
