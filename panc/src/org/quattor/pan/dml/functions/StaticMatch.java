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

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Determine whether a string matches a given regular expression, using optional
 * matching flags where the regular expression and match flags are compile time
 * constants.
 * 
 * @author loomis
 * 
 */
final public class StaticMatch extends AbstractStaticMatcher {

	private StaticMatch(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super(sourceRange, "match", operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		return new StaticMatch(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 1);

		Element e = ops[0].execute(context);

		try {

			String s = ((StringProperty) e).getValue();
			return BooleanProperty.getInstance(pattern.matcher(s).find());

		} catch (ClassCastException cce) {

			throw EvaluationException.create(sourceRange, context,
					MSG_ALL_STRING_ARGS_REQ, name, e.getTypeAsString());

		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(sourceRange, context);
		}
	}
}
