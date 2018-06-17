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

import java.util.List;
import java.util.Map;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.type.FullType;
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

        // check for binding default
        // code based on Buildtask setDefaults
        // Retrieve the type bindings.
        Map<Path, List<FullType>> bindings = context.getBindings();
        // cannot get here with p=null
        // this is a list, multiple types are allowed
        // (first default wins; more binds/defaults can be set after the value call, but those won't matter)
        List<FullType> types = bindings.get(p);

        if (types != null && (result == null || result instanceof Undef)) {
            // 1st setDefault pass : obvious choice, check for missing element path or undef value

            // There can be more than one binding per path. Loop over
            // all of them, but stop at the first one which defines a
            // default value.
            for (FullType type : types) {

                // Determine if the type has a default value.
                Element defaultValue = type.findDefault(context);

                // If something was found, set the value, then break out
                // of the loop.
                if (defaultValue != null) {
                    result = defaultValue;
                    break;
                }
            }
        }

        // ALWAYS duplicate the value to ensure that any
		// changes to returned resources do not inadvertently change the
		// referenced part of the configuration.
        if (result == null) {
            if (ops.length == 2) {
                // 2nd arg is default value
                result = args[1].duplicate();
            } else {
                throw new EvaluationException("referenced path (" + p
                                              + ") doesn't exist", getSourceRange(), context);
            }
        } else if (result instanceof Undef) {
            if (ops.length == 2) {
                result = args[1].duplicate();
            }
        } else if (types != null) {
            // 2nd setDefaults pass: check all children for default values
            // There can be more than one binding per path.

            // duplicate the result before passing to setDefaults
            // setDefaults might modify inplace
            // (unless protected, in which case replacement is not null)
            result = result.duplicate();

            for (FullType type : types) {
                Element replacement = type.setDefaults(context, result);
                if (replacement != null) {
                    result = replacement;
                }
            }
        }

		// Return the result.
        // Make sure this is a duplicated value.
        return result;
	}
}
