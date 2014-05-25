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

import org.apache.commons.lang3.text.StrSubstitutor;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;

import static org.quattor.pan.utils.MessageUtils.MSG_FIRST_STRING_ARG_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_ILLEGAL_FORMAT;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_FIRST_ARG_RENDER;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_SECOND_ARG_RENDER;

/**
 * Produces a formatted string based on the given format specification and dictionary of arguments
 *
 * @author Luis Fernando Munoz Mejias
 */
final public class Render extends BuiltInFunction {

    private Render(SourceRange sourceRange, Operation... operations) throws SyntaxException {
        super("render", sourceRange, operations);

        // There must be at least one argument.
        if (operations.length == 0) {
            throw SyntaxException.create(sourceRange, MSG_FIRST_STRING_ARG_REQ, name);
        }
    }

    public static Operation getInstance(SourceRange sourceRange, Operation... operations) throws SyntaxException {
        return new Render(sourceRange, operations);
    }

    @Override
    public Element execute(Context context) {

        assert (ops.length == 2);

        // Calculate arguments.
        Element[] args = calculateArgs(context);

        // Pull out the template string.
        String template = null;
        try {
            template = ((StringProperty) args[0]).getValue();
        } catch (ClassCastException cce) {
            throw EvaluationException.create(sourceRange, context, MSG_INVALID_FIRST_ARG_RENDER);
        }

        // Pull out the value map.
        HashResource valueMap = null;
        try {
            valueMap = ((HashResource) args[0]);
        } catch (ClassCastException cce) {
            throw EvaluationException.create(sourceRange, context, MSG_INVALID_SECOND_ARG_RENDER);
        }

        // Reformat valueMap to have only string values.
        Map<String, String> substitutionMap = new HashMap<String, String>();
        for (Resource.Entry entry : valueMap) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            substitutionMap.put(key, value);
        }

        StringProperty result = null;
        try {
            StrSubstitutor substitutor = new StrSubstitutor(substitutionMap);
            result = StringProperty.getInstance(substitutor.replace(template));
        } catch (IllegalFormatException ife) {
            throw EvaluationException.create(sourceRange, context, MSG_ILLEGAL_FORMAT, ife.getLocalizedMessage());
        }

        assert (result != null);
        return result;

    }
}
