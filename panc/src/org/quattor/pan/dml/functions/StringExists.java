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
 $Id: Exists.java 2857 2008-02-06 07:23:49Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ARG_IN_CONSTRUCTOR;
import static org.quattor.pan.utils.MessageUtils.MSG_ONE_ARG_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_ONE_STRING_OR_IDENTIFIER_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_PATH_OR_TPL_NAME_REQ;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.operators.Variable;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.template.Template;
import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.Path;

/**
 * Tests that the path or template name exists.
 * 
 * @author loomis
 * 
 */
final public class StringExists extends Exists {

	private static final long serialVersionUID = -6240697592303196473L;

	protected StringExists(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super(sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Ensure that there is exactly one argument. Since the parser does
		// little argument checking for function calls, this explicit check is
		// needed.
		if (operations.length != 1) {
			throw SyntaxException
					.create(sourceRange, MSG_ONE_ARG_REQ, "exists");
		}

		// Check to see if there is only one variable argument. If so, create a
		// Variable operation with the lookupOnly flag set and insert this into
		// the operation list.
		assert (operations.length == 1);
		if (operations[0] instanceof Variable) {
			throw CompilerError.create(MSG_INVALID_ARG_IN_CONSTRUCTOR);
		}

		// If it is a constant argument, check already that it is a string.
		if (operations[0] instanceof Element) {
			if (!(operations[0] instanceof StringProperty)) {
				throw SyntaxException.create(sourceRange,
						MSG_ONE_STRING_OR_IDENTIFIER_REQ, "exists");
			}
		}

		return new StringExists(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		throwExceptionIfCompileTimeContext(context);

		Element result = ops[0].execute(context);

		boolean exists = false;

		try {

			String s = ((StringProperty) result).getValue();

			// Try first to see if this is an external or absolute path.
			// Mask any exceptions.
			Path path = null;
			try {
				path = new Path(s);
			} catch (SyntaxException consumed) {
			} catch (EvaluationException consumed) {
			}

			if (path != null && !path.isRelative()) {

				// It was a path, so look it up for answer.
				exists = (context.getElement(path, false) != null);

			} else {

				// It was not an external or absolute path. Try treating it
				// as a template name.

				if (Template.isValidTemplateName(s)) {

					try {
						Template template = context.localAndGlobalLoad(s, true);
						exists = (template != null);
					} catch (EvaluationException consumed) {
						// The load will actually trigger a compilation of
						// the requested template, but it will not be added
						// to this template. Any exceptions will be replayed
						// later if this template is actually included.

						// If there was an exception thrown, then the
						// template was found. Return true! The exception
						// will be caught later if it is actually included.
						exists = true;
					}

				} else {
					throw new EvaluationException(MessageUtils.format(
							MSG_PATH_OR_TPL_NAME_REQ, "exists"),
							getSourceRange(), context);
				}

			}

			// Send back the result.
			return exists ? BooleanProperty.TRUE : BooleanProperty.FALSE;

		} catch (ClassCastException cce) {
			throw new EvaluationException(MessageUtils.format(
					MSG_ONE_STRING_OR_IDENTIFIER_REQ, "exists"), sourceRange,
					context);
		}
	}
}
