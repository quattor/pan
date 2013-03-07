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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Escape.java $
 $Id: Escape.java 3107 2008-04-07 07:03:42Z loomis $
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
import org.quattor.pan.utils.EscapeUtils;

/**
 * Escape an arbitrary string so that it can be used as a key for a dict.
 * 
 * @author loomis
 * 
 */
final public class Escape extends BuiltInFunction {

	private Escape(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("escape", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		Operation result = null;

		// Ensure that there is exactly one argument. Since the parser does
		// little argument checking for function calls, this explicit check is
		// needed.
		if (operations.length != 1) {
			throw SyntaxException.create(sourceRange, MSG_ONE_STRING_ARG_REQ,
					"escape");
		}

		// Optimize to a string value, if possible.
		if (operations[0] instanceof Element) {

			Element e = (Element) operations[0];

			try {
				try {
					StringProperty sp = (StringProperty) e;
					String value = EscapeUtils.escape(sp.getValue());
					result = StringProperty.getInstance(value);
				} catch (ClassCastException cce) {
					throw new EvaluationException(
							"escape argument is not a string: "
									+ e.getTypeAsString(), sourceRange);
				}
			} catch (EvaluationException ee) {
				throw SyntaxException.create(sourceRange, ee);
			}

		} else {
			result = new Escape(sourceRange, operations);
		}

		return result;
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 1);

		Element result = ops[0].execute(context);

		try {
			String s = ((StringProperty) result).getValue();
			return StringProperty.getInstance(EscapeUtils.escape(s));

		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(sourceRange, context);

		} catch (ClassCastException cce) {
			throw new EvaluationException("escape argument is not a string: "
					+ result.getTypeAsString(), sourceRange);
		}
	}

}
