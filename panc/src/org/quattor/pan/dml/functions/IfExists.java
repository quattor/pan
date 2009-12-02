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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/Sources/panc/trunk/src/org/quattor/pan/dml/functions/Exists.java $
 $Id: Exists.java 1378 2007-03-01 13:19:50Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_ONE_STRING_ARG_REQ;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.template.Template;

/**
 * This tests if the given template exists. If so, it returns the template name;
 * undef otherwise.
 * 
 * @author loomis
 * 
 */
final public class IfExists extends BuiltInFunction {

	private static final long serialVersionUID = 1723787654494815387L;

	private IfExists(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("if_exists", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Ensure that there is exactly one argument. Since the parser does
		// little argument checking for function calls, this explicit check is
		// needed.
		if (operations.length != 1) {
			throw SyntaxException.create(sourceRange, MSG_ONE_STRING_ARG_REQ,
					"if_exists");
		}

		return new IfExists(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 1);

		boolean exists = false;

		throwExceptionIfCompileTimeContext(context);

		Element element = ops[0].execute(context);

		try {

			StringProperty result = (StringProperty) element;
			String s = result.getValue();

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
					// template was found. Return true!
					exists = true;
				}

			} else {
				throw new EvaluationException("invalid template name: " + s,
						getSourceRange(), context);
			}

			// If the template exists, just return the original name. Presumably
			// it will be used in an include statement. If not, return Undef so
			// that the include does nothing.
			return exists ? result : Undef.VALUE;

		} catch (ClassCastException cce) {
			throw new EvaluationException(
					"if_exists argument is not a string: "
							+ element.getTypeAsString(), sourceRange, context);
		}
	}
}
