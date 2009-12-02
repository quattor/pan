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
 $Id: Debug.java 3616 2008-08-21 09:51:51Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_ONE_STRING_ARG_REQ;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Prints the argument to the standard error stream. This function must be
 * explicitly enabled when invoking the compiler. If the function is enabled,
 * the result of this function is the argument; otherwise it is undef.
 * 
 * @author loomis
 * 
 */
final public class Debug extends BuiltInFunction {

	private static final long serialVersionUID = 206676840856467241L;

	private Debug(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("debug", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Ensure that there is exactly one argument. Since the parser does
		// little argument checking for function calls, this explicit check is
		// needed.
		if (operations.length != 1) {
			throw SyntaxException.create(sourceRange, MSG_ONE_STRING_ARG_REQ,
					"debug");
		}
		if (operations[0] instanceof Element) {
			if (!(operations[0] instanceof StringProperty)) {
				throw SyntaxException.create(sourceRange,
						MSG_ONE_STRING_ARG_REQ, "debug");
			}
		}

		return new Debug(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 1);

		// Do not allow debug to be used in a compile time context to avoid
		// it being optimized away.
		throwExceptionIfCompileTimeContext(context);

		// Always print the debugging output. If the debugging was turned
		// off, then the parser generated a different function call.
		Element result = ops[0].execute(context);
		try {
			StringProperty sp = (StringProperty) result;
			System.err.println(sp.getValue());
		} catch (ClassCastException cce) {
			throw EvaluationException.create(sourceRange, context,
					MSG_ONE_STRING_ARG_REQ, name);
		}
		return result;

	}

}
