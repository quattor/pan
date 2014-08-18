/*
  Copyright (c) 2014 Centre National de la Recherche Scientifique (CNRS)
  and UGent.

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

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

import java.util.IllegalFormatException;

import static org.quattor.pan.utils.MessageUtils.MSG_ILLEGAL_FORMAT;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_FIRST_ARG_SUBSTITUTE;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_SECOND_ARG_SUBSTITUTE;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_SUBSTITUTE_VARIABLE;
import static org.quattor.pan.utils.MessageUtils.MSG_ONE_OR_TWO_ARGS_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_SUBSTITUTE_VARIABLE_UNDEFINED;
import static org.quattor.pan.utils.MessageUtils.MSG_TWO_ARGS_REQ;

/**
 * Produces a formatted string based on the given format specification and dictionary of arguments
 *
 * @author Luis Fernando Munoz Mejias
 */
final public class Substitute extends BuiltInFunction {

    private Substitute(SourceRange sourceRange, Operation... operations) throws SyntaxException {
        super("substitute", sourceRange, operations);
    }

    public static Operation getInstance(SourceRange sourceRange, Operation... operations) throws SyntaxException {

        if (operations.length == 1 || operations.length == 2) {
            return new Substitute(sourceRange, operations);
        } else {
            throw SyntaxException.create(sourceRange, MSG_ONE_OR_TWO_ARGS_REQ, "substitute");
        }

    }

    @Override
    public Element execute(Context context) {

        assert (ops.length == 1 || ops.length == 2);

        // Calculate arguments.
        Element[] args = calculateArgs(context);

        // Pull out the template string.
        String template = null;
        try {
            template = ((StringProperty) args[0]).getValue();
        } catch (ClassCastException cce) {
            throw EvaluationException.create(sourceRange, context, MSG_INVALID_FIRST_ARG_SUBSTITUTE);
        }

        // Pull out the value map.
        HashResource valueMap = null;
        if (ops.length == 2) {
            try {
                valueMap = ((HashResource) args[1]);
            } catch (ClassCastException cce) {
                throw EvaluationException.create(sourceRange, context, MSG_INVALID_SECOND_ARG_SUBSTITUTE);
            }
        }

        StringProperty result = null;
        try {
            Resolver resolver = new Resolver(valueMap, context);
            StrSubstitutor sub = new StrSubstitutor(resolver);
            result = StringProperty.getInstance(sub.replace(template));
        } catch (IllegalFormatException ife) {
            throw EvaluationException.create(sourceRange, context, MSG_ILLEGAL_FORMAT, ife.getLocalizedMessage());
        }

        assert (result != null);
        return result;

    }

    private class Resolver extends StrLookup<String> {

        private final HashResource valueMap;

        private final Context context;

        public Resolver(HashResource valueMap, Context context) {
            this.valueMap = valueMap;
            this.context = context;
        }

        public String lookup(String key) {
            try {
                if (valueMap != null) {
                    StringProperty k = StringProperty.getInstance(key);
                    return valueMap.get(k).toString();
                } else {
                    return context.getVariable(key).toString();
                }
            } catch (InvalidTermException e) {
                throw EvaluationException.create(sourceRange, context, MSG_INVALID_SUBSTITUTE_VARIABLE, key);
            } catch (NullPointerException e) {
                throw EvaluationException.create(sourceRange, context, MSG_SUBSTITUTE_VARIABLE_UNDEFINED, key);
            }
        }

    }
}
