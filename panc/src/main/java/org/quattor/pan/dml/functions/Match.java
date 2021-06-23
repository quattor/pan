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
 $Id: Match.java 3090 2008-03-22 11:33:34Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_ALL_STRING_ARGS_REQ;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;

/**
 * Determine whether a string matches a given regular expression, using optional
 * matching flags.
 * 
 * @author loomis
 * 
 */
final public class Match extends AbstractVariableMatcher {

	private Match(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super(sourceRange, "match", operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Optimize the operation if compile time constants are given for the
		// regular expression and match flags.
		if (operations.length > 1
				&& operations[1] instanceof StringProperty
				&& (operations.length < 3 || operations[2] instanceof StringProperty)) {
			return StaticMatch.getInstance(sourceRange, operations);
		} else {
			return new Match(sourceRange, operations);
		}

	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 2 || ops.length == 3);

		Element[] args = calculateArgs(context);

		try {
			String s = ((StringProperty) args[0]).getValue();

			int flags = 0;
			if (ops.length == 3) {
				flags = convertMatchFlags(args[2]);
			}

			Pattern pattern = compilePattern(args[1], flags);
			Matcher matcher = pattern.matcher(s);

			return BooleanProperty.getInstance(matcher.find());

		} catch (ClassCastException cce) {

			String badtype = "INTERNAL ERROR";
			for (Element e : args) {
				if (!(e instanceof StringProperty)) {
					badtype = e.getTypeAsString();
				}
			}
			throw EvaluationException.create(sourceRange, context,
					MSG_ALL_STRING_ARGS_REQ, name, badtype);

		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(sourceRange, context);
		}
	}
}
