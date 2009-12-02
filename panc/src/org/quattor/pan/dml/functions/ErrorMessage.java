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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/ErrorMessage.java $
 $Id: ErrorMessage.java 2869 2008-02-09 17:27:33Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_ONE_STRING_ARG_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_RESTRICTED_CONTEXT;

import java.io.File;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Implements the <code>error</code> function that prints its argument to the
 * standard error stream and then aborts the compilation of the object profile.
 * 
 * @author loomis
 * 
 */
final public class ErrorMessage extends BuiltInFunction {

	private static final long serialVersionUID = -2617127833778210110L;

	private ErrorMessage(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("error", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Ensure that there is exactly one argument. Since the parser does
		// little argument checking for function calls, this explicit check is
		// needed.
		if (operations.length != 1) {
			throw SyntaxException.create(sourceRange, MSG_ONE_STRING_ARG_REQ,
					"error");
		}

		return new ErrorMessage(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 1);

		Element result = ops[0].execute(context);

		File objectFile = (context.getObjectTemplate() != null) ? context
				.getObjectTemplate().source : null;

		try {
			StringProperty sp = (StringProperty) result;
			throw new EvaluationException("user-initiated error: "
					+ sp.getValue(), getSourceRange(), objectFile, context
					.getTraceback(getSourceRange()));
		} catch (ClassCastException cce) {
			throw new EvaluationException(
					"user-initiated error thrown with non-string argument",
					getSourceRange(), objectFile, context
							.getTraceback(getSourceRange()));
		}
	}

	@Override
	public void checkRestrictedContext() throws SyntaxException {
		super.checkRestrictedContext();
		throw SyntaxException.create(sourceRange, MSG_RESTRICTED_CONTEXT, name);
	}

}
