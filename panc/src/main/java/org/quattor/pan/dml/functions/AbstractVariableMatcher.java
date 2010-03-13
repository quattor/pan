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

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.SourceRange;

/**
 * Abstract class to bring together checks done for both match() and matches().
 * 
 * @author loomis
 * 
 */
abstract public class AbstractVariableMatcher extends AbstractMatcher {

	protected AbstractVariableMatcher(SourceRange sourceRange,
			String functionName, Operation... operations)
			throws SyntaxException {
		super(functionName, sourceRange, operations);

		// The match() and matches() functions take exactly 2 or 3 arguments.
		if (operations.length < 2 || operations.length > 3) {
			throw SyntaxException.create(sourceRange, MSG_2_OR_3_ARGS,
					functionName);
		}

		try {

			// Try to compile the pattern.
			if (operations[1] instanceof StringProperty) {
				compilePattern((StringProperty) operations[1], 0);
			}

			// Check the match flags.
			if (operations.length == 3
					&& (operations[2] instanceof StringProperty)) {
				convertMatchFlags((Element) operations[2]);
			}

		} catch (EvaluationException ee) {
			throw SyntaxException.create(sourceRange, ee);
		}

	}
}