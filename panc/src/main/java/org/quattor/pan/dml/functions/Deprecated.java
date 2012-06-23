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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Debug.java $
 $Id: Debug.java 3515 2008-07-31 13:20:05Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_FATAL_DEPRECATION_MSG;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_FIRST_ARG_DEPRECATED;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_SECOND_ARG_DEPRECATED;
import static org.quattor.pan.utils.MessageUtils.MSG_TWO_ARGS_REQ;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Prints the argument to the standard error stream if the given level is less
 * than or equal to the deprecation level option. The returned value is the
 * message, if the message is printed; undef, otherwise.
 * 
 * @author loomis
 * 
 */
// FIXME: This method should really only accept one argument now.
final public class Deprecated extends BuiltInFunction {

	private Deprecated(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("deprecated", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Must be two arguments. The first is the deprecation level to check;
		// the second is the message.
		if (operations.length != 2) {
			throw SyntaxException.create(sourceRange, MSG_TWO_ARGS_REQ,
					"deprecated");
		}

		// If the first argument is a constant, it must be a long value.
		Operation op = operations[0];
		if (op instanceof Element) {
			if (op instanceof LongProperty) {
				Long value = ((LongProperty) op).getValue();
				if (value < 0L) {
					throw SyntaxException.create(sourceRange,
							MSG_INVALID_FIRST_ARG_DEPRECATED);
				}

			} else {

				throw SyntaxException.create(sourceRange,
						MSG_INVALID_FIRST_ARG_DEPRECATED);
			}
		}
		op = operations[1];
		if (op instanceof Element && !(op instanceof StringProperty)) {
			throw SyntaxException.create(sourceRange,
					MSG_INVALID_SECOND_ARG_DEPRECATED);
		}

		return new Deprecated(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 2);

		Element value = Undef.VALUE;

		if (!context.isCompileTimeContext()) {

			Element[] args = calculateArgs(context);

			// If the first argument is a constant, it must be a long value.
			if (!(args[0] instanceof LongProperty)) {
				throw EvaluationException.create(sourceRange, context,
						MSG_INVALID_FIRST_ARG_DEPRECATED);
			}
			if (!(args[1] instanceof StringProperty)) {
				throw EvaluationException.create(sourceRange, context,
						MSG_INVALID_SECOND_ARG_DEPRECATED);
			}

			Long level = ((LongProperty) args[0]).getValue();
			StringProperty msg = (StringProperty) args[1];

			if (level < 0) {
				throw EvaluationException.create(sourceRange, context,
						MSG_INVALID_FIRST_ARG_DEPRECATED);

			}

			switch (context.getDeprecationWarnings()) {
			case ON:
				System.err.println(msg.getValue());
				break;
			case FATAL:
				throw EvaluationException.create(sourceRange, context,
						MSG_FATAL_DEPRECATION_MSG, msg.getValue());
			}

		}

		return value;
	}
}
