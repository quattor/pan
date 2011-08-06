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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Length.java $
 $Id: Length.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_NULL_RESULT_FROM_OPERATION;
import static org.quattor.pan.utils.MessageUtils.MSG_ONE_ARG_REQ;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Returns the length of a string or the size of a resource.
 * 
 * @author loomis
 * 
 */
final public class Length extends BuiltInFunction {

	private Length(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("length", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Ensure that there is exactly one argument. Since the parser does
		// little argument checking for function calls, this explicit check is
		// needed.
		if (operations.length != 1) {
			throw SyntaxException
					.create(sourceRange, MSG_ONE_ARG_REQ, "length");
		}

		return new Length(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 1);

		Element result = ops[0].execute(context);
		int length = 0;
		if (result instanceof StringProperty) {
			length = ((StringProperty) result).getValue().length();
		} else if (result instanceof Resource) {
			length = ((Resource) result).size();
		} else if (result instanceof Undef) {
			length = 0;
		} else {
			if (result != null) {
				throw new EvaluationException(
						"argument to length must be undef, string, or resource; element of type "
								+ result.getTypeAsString() + " is not valid",
						getSourceRange(), context);
			} else {
				throw CompilerError.create(MSG_NULL_RESULT_FROM_OPERATION);
			}
		}
		return LongProperty.getInstance(length);
	}
}
