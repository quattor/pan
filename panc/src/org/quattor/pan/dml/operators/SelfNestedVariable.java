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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/Variable.java $
 $Id: Variable.java 1867 2007-06-17 17:01:45Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.quattor.pan.utils.MessageUtils.MSG_UNDEFINED_VAR;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.Term;

/**
 * Looks up a nested variable in the execution context. There must be at least
 * one index (operation).
 * 
 * @author loomis
 * 
 */
public class SelfNestedVariable extends NestedVariable {

	private static final long serialVersionUID = 8697587866473321966L;

	public SelfNestedVariable(SourceRange sourceRange, boolean lookupOnly,
			Operation... operations) {
		super(sourceRange, "SELF", lookupOnly, operations);
		assert (operations.length > 0);
	}

	@Override
	public Element execute(Context context) {

		// Quickly check to see if this is a compile-time context. This function
		// cannot be evaluated in such a context.
		if (context.isCompileTimeContext()) {
			throw new EvaluationException(
					"SELF[] cannot be evaluated in compile-time context");
		}

		// Look up the variable. This may be null if we're running at
		// compile-time.
		Element result = context.getSelf();
		assert (result != null);

		// If there are some operations in this Value operation, create a list
		// will all of the terms and do the recursive lookup of the value.
		Term[] terms = null;
		if (!(result instanceof Undef)) {
			try {
				terms = calculateTerms(context);
			} catch (EvaluationException ee) {
				throw ee.addExceptionInfo(getSourceRange(), context);
			}

			// Look up the result.
			// FIXME: determine if the protect flag needs to be set.
			try {
				try {
					result = result.rget(terms, 0, false, false);
				} catch (InvalidTermException ite) {
					if (!lookupOnly) {
						throw new EvaluationException(ite
								.formatVariableMessage(identifier, terms));
					} else {
						result = null;
					}
				}
			} catch (EvaluationException ee) {
				throw ee.addExceptionInfo(sourceRange, context);
			}
		} else {

			// If we need to look something up and the value is undef, then
			// the looked up value does not exist. Set the return value to
			// null and let the following code generate the exception.
			result = null;
		}

		// Must also check to see if the last looked up value was null. If it
		// was, it wasn't found. Throw an error if this wasn't a lookup request.
		if (result == null) {
			if (!lookupOnly) {
				StringBuilder sb = new StringBuilder(identifier);
				if (terms != null) {
					for (Term t : terms) {
						sb.append("[");
						sb.append(t.toString());
						sb.append("]");
					}
				}
				throw new EvaluationException(MessageUtils.format(
						MSG_UNDEFINED_VAR, sb.toString()), sourceRange, context);
			}
		}

		return result;
	}

}
