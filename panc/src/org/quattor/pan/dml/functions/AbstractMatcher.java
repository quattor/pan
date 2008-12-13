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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Match.java $
 $Id: Match.java 2799 2008-01-26 17:10:23Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_ALL_STRING_ARGS_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_REGEXP;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_REGEXP_FLAG;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.SourceRange;

/**
 * Determine whether a string matches a given regular expression, using optional
 * matching flags.
 * 
 * @author loomis
 * 
 */
abstract public class AbstractMatcher extends BuiltInFunction {

	private static final long serialVersionUID = -4891330766942615516L;

	final protected String functionName;

	protected AbstractMatcher(SourceRange sourceRange, String functionName,
			Operation... operations) throws SyntaxException {
		super(sourceRange, operations);
		assert (functionName != null);
		this.functionName = functionName;

		// Check that static arguments are strings.
		for (Operation op : operations) {
			if (op instanceof Element && !(op instanceof StringProperty)) {
				String badtype = ((Element) op).getTypeAsString();
				throw SyntaxException.create(sourceRange,
						MSG_ALL_STRING_ARGS_REQ, functionName, badtype);
			}
		}

	}

	/**
	 * A utility function to convert a string containing match options to the
	 * associated integer with the appropriate bits set.
	 * 
	 * @param opts
	 *            string containing matching flags
	 * @return integer with appropriate bits set
	 */
	protected int convertMatchFlags(Element opts) {
		int flags = 0;

		String sopts = ((StringProperty) opts).getValue();

		for (int i = 0; i < sopts.length(); i++) {
			char c = sopts.charAt(i);
			switch (c) {
			case 'i':
				flags |= Pattern.CASE_INSENSITIVE;
				break;
			case 's':
				flags |= Pattern.DOTALL;
				break;
			case 'm':
				flags |= Pattern.MULTILINE;
				break;
			case 'u':
				flags |= Pattern.UNICODE_CASE;
				break;
			case 'x':
				flags |= Pattern.COMMENTS;
				break;
			default:
				throw EvaluationException.create(sourceRange,
						MSG_INVALID_REGEXP_FLAG, c);
			}
		}

		return flags;
	}

	/**
	 * Generate a Pattern from the given string and flags.
	 * 
	 * @param regex
	 *            regular expression to compile
	 * @param flags
	 *            matching flags to use for pattern
	 * 
	 * @return Pattern corresponding to the given regular expression and
	 *         matching flags
	 */
	protected Pattern compilePattern(Element regex, int flags) {

		Pattern p = null;
		try {
			String re = ((StringProperty) regex).getValue();
			p = Pattern.compile(re, flags);
		} catch (PatternSyntaxException pse) {
			throw EvaluationException.create(sourceRange, MSG_INVALID_REGEXP,
					pse.getLocalizedMessage());
		}

		return p;
	}

}