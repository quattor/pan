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
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.Term;

/**
 * Looks up a nested variable in the execution context. There must be at least
 * one index (operation).
 * 
 * @author loomis
 * 
 */
public class NestedVariable extends Variable {

	public NestedVariable(SourceRange sourceRange, String identifier,
			boolean lookupOnly, Operation... operations) {
		super(sourceRange, identifier, lookupOnly, operations);
		assert (operations.length > 0);
	}

	@Override
	public Element execute(Context context) {

		// Create the array of terms to dereference the variable.
		Term[] terms = null;
		try {
			terms = calculateTerms(context);
		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(getSourceRange(), context);
		}

		// Look up the variable.
		Element result = null;
		try {
			try {

				result = context.dereferenceVariable(identifier, lookupOnly,
						terms);

			} catch (InvalidTermException ite) {
				if (!lookupOnly) {
					throw new EvaluationException(ite.formatVariableMessage(
							identifier, terms));
				} else {
					result = null;
				}
			}
		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(sourceRange, context);
		}

		// If the result is null and this isn't a lookup request, throw an
		// error.
		if (result == null && !lookupOnly) {
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

		// Send the result back to the caller.
		return result;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + identifier + ","
				+ ops.length + ")";
	}
}
