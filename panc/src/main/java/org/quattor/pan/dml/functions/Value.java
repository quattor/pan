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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Value.java $
 $Id: Value.java 2862 2008-02-07 13:22:08Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_ONE_OR_TWO_ARGS_REQ;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.Path;

/**
 * Extract a value from the configuration tree based on a given path.
 *
 * @author loomis
 *
 */
final public class Value extends BuiltInFunction {

	private Value(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("value", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// This must have either one or two arguments.
		if (operations.length < 1 || operations.length > 2) {
			throw SyntaxException.create(sourceRange, MSG_ONE_OR_TWO_ARGS_REQ,
					"value");
		}
		assert (operations.length == 1 || operations.length == 2);

		return new Value(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		// Retrieve the values of the arguments.
		Element[] args = calculateArgs(context);

		assert (ops.length == 1 || ops.length == 2);

		throwExceptionIfCompileTimeContext(context);

		Element result = null;
		Path p = null;
		try {
			String s = ((StringProperty) args[0]).getValue();
			try {
				p = new Path(s);
                // errorIfNotFound=false; missing path is handled w optional default below
				result = context.getElement(p, false);
			} catch (SyntaxException se) {
				throw new EvaluationException(se.getSimpleMessage(),
						sourceRange, context);
			}
		} catch (ClassCastException cce) {
			throw new EvaluationException(
					"value() requires one string argument; "
							+ args[0].getTypeAsString() + " found",
					getSourceRange(), context);
		}


        if (result == null) {
            if (ops.length == 2) {
                // 2nd arg is default value
                result = args[1];
            } else {
                throw new EvaluationException("referenced path (" + p
                                              + ") doesn't exist", getSourceRange(), context);
            }
        } else if (result instanceof Undef && ops.length == 2) {
            result = args[1];
        }

		// Return the result. ALWAYS duplicate the value to ensure that any
		// changes to returned resources do not inadvertently change the
		// referenced part of the configuration.
        return result.duplicate();
	}
}
