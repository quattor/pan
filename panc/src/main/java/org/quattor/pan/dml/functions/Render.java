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

  $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Format.java $
  $Id: Format.java 2799 2008-01-26 17:10:23Z loomis $
*/

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_FIRST_STRING_ARG_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_FORMAT_REQUIRES_PROPERTIES;
import static org.quattor.pan.utils.MessageUtils.MSG_ILLEGAL_FORMAT;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_FIRST_ARG_FORMAT;

import java.util.IllegalFormatException;
import java.util.Map;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

import org.stringtemplate.v4.ST;

/**
 * Produces a formatted string based on the given format specification and
 * dictionary of arguments
 *
 * @author Luis Fernando Muñoz Mejías
 *
 */
final public class Render extends BuiltInFunction {

        private Render(SourceRange sourceRange, Operation... operations)
                throws SyntaxException {
                super("render", sourceRange, operations);

                // There must be at least one argument.
                if (operations.length == 0) {
                        throw SyntaxException.create(sourceRange, MSG_FIRST_STRING_ARG_REQ,
                                                     name);
                }
        }

        public static Operation getInstance(SourceRange sourceRange,
                                            Operation... operations) throws SyntaxException {
                return new Render(sourceRange, operations);
        }

        @Override
        public Element execute(Context context) {

                assert (ops.length == 2);

                // Calculate arguments.
                Element[] args = calculateArgs(context);

                // Pull out the format string.
                String format = null;
                try {
                        format = ((StringProperty) args[0]).getValue();
                } catch (ClassCastException cce) {
                        throw EvaluationException.create(sourceRange, context,
                                                         MSG_INVALID_FIRST_ARG_FORMAT);
                }

                // Now pull out all of the arguments for formatting.
                ST template = new ST(format);

                for (Resource.Entry entry : ((Resource) args[1])) {
                        try {
                                template.add(((StringProperty) entry.getKey())
                                             .getValue(),
                                             ((StringProperty) entry.getValue())
                                             .getValue());
                        } catch (ClassCastException cce) {
                                throw EvaluationException.create(sourceRange, context,
                                                                 MSG_ILLEGAL_FORMAT);}
                }

                StringProperty result = null;
                try {
                        result = StringProperty.getInstance(template.render());
                } catch (IllegalFormatException ife) {
                        throw EvaluationException.create(sourceRange, context,
                                                         MSG_ILLEGAL_FORMAT, ife.getLocalizedMessage());
                }

                // Return the value. It should never be null if we
                // reach this part of the code.
                assert (result != null);
                return result;

        }
}
