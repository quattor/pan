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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Splice.java $
 $Id: Splice.java 3597 2008-08-17 09:08:57Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_3_OR_4_ARGS;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;

/**
 * Splice two resources or strings.
 * 
 * @author loomis
 * 
 */
final public class Splice extends BuiltInFunction {

	private Splice(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("splice", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// There can be three or four arguments.
		if (operations.length < 3 || operations.length > 4) {
			throw SyntaxException
					.create(sourceRange, MSG_3_OR_4_ARGS, "splice");
		}

		return new Splice(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		// Retrieve the values of the arguments.
		Element[] args = calculateArgs(context);
		assert (ops.length == 3 || ops.length == 4);

		// Declare the return value.
		Element result = null;

		// Start with extracting the second and third arguments which must both
		// be LongProperties.
		int start = 0;
		try {

			// Extract the value and make sure it can be safely converted to an
			// integer.
			long slong = ((LongProperty) args[1]).getValue().longValue();
			if (slong > (long) Integer.MAX_VALUE
					|| slong < (long) Integer.MIN_VALUE) {
				throw new EvaluationException(
						"second argument to splice() cannot be converted to int: "
								+ slong, getSourceRange(), context);
			}
			start = (int) slong;

		} catch (ClassCastException cce) {
			throw new EvaluationException(
					"second argument to splice() must be a long",
					getSourceRange(), context);
		}

		int length = 0;
		try {

			// Extract the value and make sure it can be safely converted to an
			// integer.
			long slong = ((LongProperty) args[2]).getValue().longValue();
			if (slong > (long) Integer.MAX_VALUE
					|| slong < (long) Integer.MIN_VALUE) {
				throw new EvaluationException(
						"third argument to splice() cannot be converted to int: "
								+ slong, getSourceRange(), context);
			}
			length = (int) slong;

		} catch (ClassCastException cce) {
			throw new EvaluationException(
					"third argument to splice() must be a long",
					getSourceRange(), context);
		}

		// Now there is a choice: the first and fourth arguments are either both
		// string or both lists. Anything else is an error.
		if (args[0] instanceof StringProperty) {

			if (args.length == 4 && !(args[3] instanceof StringProperty)) {
				throw new EvaluationException(
						"fourth argument of splice() must be of the same type as the first",
						getSourceRange(), context);
			}

			String s = ((StringProperty) args[0]).getValue();

			String middle = (args.length == 4) ? (((StringProperty) args[3])
					.getValue()) : "";

			if (start < 0) {
				start = s.length() + start;
			}

			int end = (length >= 0) ? start + length : s.length() + length;

			if (start < 0) {
				throw new EvaluationException(
						"start index in splice() is before beginning of string",
						getSourceRange(), context);
			}
			if (start > s.length()) {
				throw new EvaluationException(
						"start index in splice() is after end of string",
						getSourceRange(), context);
			}
			if (end < 0) {
				throw new EvaluationException(
						"end index in splice() is before beginning of string",
						getSourceRange(), context);
			}
			if (end > s.length()) {
				throw new EvaluationException(
						"end index in splice() is after end of string",
						getSourceRange(), context);
			}
			if (end < start) {
				throw new EvaluationException(
						"end index is before the start index",
						getSourceRange(), context);
			}

			String r = s.substring(0, start) + middle + s.substring(end);
			result = StringProperty.getInstance(r);

		} else if (args[0] instanceof ListResource) {

			if (args.length == 4 && !(args[3] instanceof ListResource)) {
				throw new EvaluationException(
						"fourth argument of splice() must be of the same type as the first",
						getSourceRange(), context);
			}

			ListResource slist = (ListResource) args[0];

			ListResource mlist = (args.length == 4) ? ((ListResource) args[3])
					: new ListResource();

			if (start < 0) {
				start = slist.size() + start;
			}

			int end = (length >= 0) ? start + length : slist.size() + length;

			if (start < 0) {
				throw new EvaluationException(
						"start index in splice() is before beginning of list",
						getSourceRange(), context);
			}
			if (start > slist.size()) {
				throw new EvaluationException(
						"start index in splice() is after end of list",
						getSourceRange(), context);
			}
			if (end < 0) {
				throw new EvaluationException(
						"end index in splice() is before beginning of list",
						getSourceRange(), context);
			}
			if (end > slist.size()) {
				throw new EvaluationException(
						"end index in splice() is after end of list",
						getSourceRange(), context);
			}
			if (end < start) {
				throw new EvaluationException(
						"end index is before the start index",
						getSourceRange(), context);
			}

			// Iterate over all of the items in the source list. Copy them into
			// the result as appropriate.
			ListResource rlist = new ListResource();
			result = rlist;

			// Copy the starting entries into the result.
			int index = 0;
			for (Resource.Entry entry : slist) {
				long i = ((LongProperty) entry.getKey()).getValue().longValue();

				if (i < start) {
					rlist.put(index++, entry.getValue());
				}
			}

			// Copy the replacement list into the result.
			for (Resource.Entry entry : mlist) {
				rlist.put(index++, entry.getValue());
			}

			// Copy any of the trailing entries into the result.
			for (Resource.Entry entry : slist) {
				long i = ((LongProperty) entry.getKey()).getValue().longValue();

				if (i >= end) {
					rlist.put(index++, entry.getValue());
				}
			}

		} else {
			throw new EvaluationException(
					"first argument of splice() must be a string or list",
					getSourceRange(), context);
		}

		return result;
	}
}
