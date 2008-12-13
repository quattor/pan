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
 $Id: Substr.java 2799 2008-01-26 17:10:23Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_2_OR_3_ARGS;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Extract a substring from a string.
 * 
 * @author loomis
 * 
 */
final public class Substr extends BuiltInFunction {

	private static final long serialVersionUID = 7217886681703368679L;

	private Substr(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super(sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// There can be two or three arguments.
		if (operations.length < 2 || operations.length > 3) {
			throw SyntaxException
					.create(sourceRange, MSG_2_OR_3_ARGS, "substr");
		}

		return new Substr(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 2 || ops.length == 3);

		String string = null;
		try {
			string = ((StringProperty) ops[0].execute(context)).getValue();

		} catch (ClassCastException cce) {
			throw new EvaluationException(
					"first argument to substr() must be a string",
					getSourceRange(), context);
		}

		int start = 0;
		try {

			// Extract the value and make sure it can be safely converted to an
			// integer.
			long slong = ((LongProperty) ops[1].execute(context)).getValue()
					.longValue();
			if (slong > (long) Integer.MAX_VALUE
					|| slong < (long) Integer.MIN_VALUE) {
				throw new EvaluationException(
						"second argument to substr() cannot be converted to int: "
								+ slong, getSourceRange(), context);
			}

			// Get the starting index. Negative values indicate number of
			// characters from the end of the string.
			start = (int) slong;
			if (start < 0) {
				start = string.length() + start;
			}

			// Check that the index is (still) valid.
			if (start > string.length()) {
				throw new EvaluationException(
						"start index in substr() is greater than the string length",
						getSourceRange(), context);
			}
			if (start < 0) {
				throw new EvaluationException(
						"start index in substr() is less than zero",
						getSourceRange(), context);
			}

		} catch (ClassCastException cce) {
			throw new EvaluationException(
					"second argument to substr() must be a long",
					getSourceRange(), context);
		}

		int end = string.length();
		if (ops.length == 3) {
			try {
				long slong = ((LongProperty) ops[2].execute(context))
						.getValue().longValue();
				if (slong > (long) Integer.MAX_VALUE
						|| slong < (long) Integer.MIN_VALUE) {
					throw new EvaluationException(
							"third argument to substr() cannot be converted to int: "
									+ slong, getSourceRange(), context);
				}

				int temp = (int) slong;
				if (temp >= 0) {
					end = start + temp;
				} else {
					end = string.length() + temp;
				}

				// Check that the index is (still) valid.
				if (end > string.length()) {
					throw new EvaluationException(
							"end index in substr() is greater than the string length",
							getSourceRange(), context);
				}
				if (end < 0) {
					throw new EvaluationException(
							"end index in substr() is less than zero",
							getSourceRange(), context);
				}

			} catch (ClassCastException cce) {
				throw new EvaluationException(
						"second and optional third arguments to substr() must be longs",
						getSourceRange(), context);
			}

			// Check the arguments.
			if (end < start) {
				throw new EvaluationException(
						"start index is after ending index in substr()",
						getSourceRange(), context);
			}

		}
		return StringProperty.getInstance(string.substring(start, end));
	}
}
