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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Exists.java $
 $Id: Exists.java 2861 2008-02-06 08:40:49Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_ONE_ARG_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_ONE_STRING_ARG_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_RELATIVE_PATH_NOT_ALLOWED;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.Path;

/**
 * Tests whether or not the given path exists. Only can be applied to an
 * absolute or external path.
 * 
 * @author loomis
 * 
 */
final public class PathExists extends BuiltInFunction {

	private static final long serialVersionUID = -6558349830495601454L;

	protected PathExists(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super(sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Ensure that there is exactly one argument. Since the parser does
		// little argument checking for function calls, this explicit check is
		// needed.
		if (operations.length != 1) {
			throw SyntaxException.create(sourceRange, MSG_ONE_ARG_REQ,
					"path_exists");
		}
		assert (operations.length == 1);

		// If there is a constant argument, then make sure it is a path.
		Operation op = operations[0];
		if (op instanceof Element) {
			try {
				String s = ((StringProperty) op).getValue();
				Path path = new Path(s);
				if (path.isRelative()) {
					throw SyntaxException.create(sourceRange,
							MSG_RELATIVE_PATH_NOT_ALLOWED);
				}
			} catch (ClassCastException cce) {
				throw SyntaxException.create(sourceRange,
						MSG_ONE_STRING_ARG_REQ);
			}
		}

		// Create a new operation and pass it back.
		return new PathExists(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		// Quickly check to see if this is a compile-time context. This function
		// cannot be evaluated in such a context.
		if (context.isCompileTimeContext()) {
			throw new EvaluationException(
					MessageUtils.MSG_CANNOT_RUN_IN_COMPILE_TIME_CONTEXT);
		}

		Element result = ops[0].execute(context);

		boolean exists = false;

		try {

			String s = ((StringProperty) result).getValue();

			Path path = new Path(s);

			if (!path.isRelative()) {

				// Absolute or external paths are OK.
				exists = (context.getElement(path, false) != null);
			} else {

				// Relative paths are not.
				throw EvaluationException.create(sourceRange, context,
						MSG_RELATIVE_PATH_NOT_ALLOWED);
			}

			// Send back the result.
			return exists ? BooleanProperty.TRUE : BooleanProperty.FALSE;

		} catch (SyntaxException se) {
			throw new EvaluationException(se.getMessage(), sourceRange, context);
		} catch (ClassCastException cce) {
			throw EvaluationException.create(sourceRange, context,
					MSG_ONE_STRING_ARG_REQ);
		}
	}
}
