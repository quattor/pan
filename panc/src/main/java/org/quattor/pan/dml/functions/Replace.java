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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Substr.java $
 $Id: Substr.java 2618 2007-12-08 16:32:02Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_3_ARGS_REQ;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Replace occurrences of a regular expression with a given string.
 * 
 * @author loomis
 * 
 */
final public class Replace extends BuiltInFunction {

	private Replace(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("replace", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// There must be three arguments.
		if (operations.length != 3) {
			throw SyntaxException
					.create(sourceRange, MSG_3_ARGS_REQ, "replace");
		}

		return new Replace(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 3);

		// Extract the first argument. This must be a string value and also a
		// valid regular expression.
		Pattern regex = null;
		try {

			String re = ((StringProperty) ops[0].execute(context)).getValue();
			regex = Pattern.compile(re);

		} catch (ClassCastException cce) {
			throw new EvaluationException(
					"first argument to replace() must be a regular expression string",
					getSourceRange(), context);
		} catch (PatternSyntaxException pse) {
			throw new EvaluationException("invalid regular expression: "
					+ pse.getLocalizedMessage(), getSourceRange(), context);
		}

		// Extract the second argument, the replacement string.
		String repl = null;
		try {

			repl = ((StringProperty) ops[1].execute(context)).getValue();

		} catch (ClassCastException cce) {
			throw new EvaluationException(
					"second argument to replace() must be a replacement string",
					getSourceRange(), context);
		}

		// Finally get the target string.
		String target = null;
		try {

			target = ((StringProperty) ops[2].execute(context)).getValue();

		} catch (ClassCastException cce) {
			throw new EvaluationException(
					"third argument to replace() must be a string",
					getSourceRange(), context);
		}

		String result = regex.matcher(target).replaceAll(repl);
		return StringProperty.getInstance(result);
	}
}
