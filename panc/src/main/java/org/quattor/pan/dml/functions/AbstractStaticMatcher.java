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

import static org.quattor.pan.utils.MessageUtils.MSG_2_OR_3_ARGS;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_CALL_TO_STATIC_MATCHER;

import java.util.regex.Pattern;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.SourceRange;

/**
 * Determine whether a string matches a given regular expression, using optional
 * matching flags.
 * 
 * @author loomis
 * 
 */
abstract public class AbstractStaticMatcher extends AbstractMatcher {

	final Pattern pattern;

	protected AbstractStaticMatcher(SourceRange sourceRange,
			String functionName, Operation... operations)
			throws SyntaxException {
		super(functionName, sourceRange, new Operation[] { operations[0] });

		// The match() and matches() functions take exactly 2 or 3 arguments.
		if (operations.length < 2 || operations.length > 3) {
			throw SyntaxException.create(sourceRange, MSG_2_OR_3_ARGS,
					functionName);
		}

		// This should only be called if arguments other than the first are
		// string constants.
		if (!(operations[1] instanceof StringProperty)
				|| (operations.length > 2 && !(operations[2] instanceof StringProperty))) {
			throw CompilerError.create(MSG_INVALID_CALL_TO_STATIC_MATCHER);
		}

		try {

			// Generate the appropriate flags.
			int flags = 0;
			if (operations.length == 3) {
				flags = convertMatchFlags((Element) operations[2]);
			}

			// Now the pattern itself.
			pattern = compilePattern((Element) operations[1], flags);

		} catch (EvaluationException ee) {

			// Recast any evaluation exceptions here as syntax exceptions.
			throw SyntaxException.create(sourceRange, ee);
		}

	}
}