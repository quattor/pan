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

import static org.quattor.pan.utils.MessageUtils.MSG_CANNOT_MODIFY_SELF;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_IN_COMPILE_TIME_CONTEXT;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_SELF_REF_IN_INCLUDE;
import static org.quattor.pan.utils.MessageUtils.MSG_SELF_IS_UNDEFINED;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.utils.Term;

/**
 * Looks up a nested variable in the execution context. There must be at least
 * one index (operation).
 * 
 * @author loomis
 * 
 */
public class SelfNestedListVariable extends NestedListVariable {

	public SelfNestedListVariable(SourceRange sourceRange,
			Operation... operations) {
		super(sourceRange, "SELF", operations);
		assert (operations.length > 0);
	}

	@Override
	public void checkInvalidSelfContext() throws SyntaxException {
		throw SyntaxException.create(sourceRange,
				MSG_INVALID_SELF_REF_IN_INCLUDE);
	}

	@Override
	public Element execute(Context context) {

		// Quickly check to see if this is a compile-time context. This function
		// cannot be evaluated in such a context.
		if (context.isCompileTimeContext()) {
			throw EvaluationException.create(sourceRange,
					MSG_INVALID_IN_COMPILE_TIME_CONTEXT, this.getClass()
							.getSimpleName());
		}

		// Create the array of terms to dereference the variable.
		Term[] terms = null;
		try {
			terms = calculateTerms(context);
		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(getSourceRange(), context);
		}

		// Check that SELF isn't fixed. This is true for a validation call.
		if (context.isSelfFinal()) {
			EvaluationException ee = EvaluationException
					.create(MSG_CANNOT_MODIFY_SELF);
			throw ee.addExceptionInfo(sourceRange, context);
		}

		// Pull out the value.
		Element result = context.getSelf();

		if (result == null) {

			// There's a problem because SELF isn't defined at all.
			throw CompilerError.create(MSG_SELF_IS_UNDEFINED);

		} else if ((result instanceof Undef) || (result instanceof Null)) {

			// Ok. Create an empty list and reset the value of SELF.
			result = new ListResource();
			context.resetSelf(result);

		} else if (result.isProtected()) {

			// The given value is protected. Replace it with a writable copy.
			result = result.writableCopy();
			context.resetSelf(result);

		}

		// Lookup (and create if necessary) all of the children.
		try {
			result = result.rgetList(terms, 0);
		} catch (InvalidTermException e) {
			throw new EvaluationException(e.formatVariableMessage(identifier,
					terms));
		}

		// Send the result back to the caller.
		return result;
	}
}
