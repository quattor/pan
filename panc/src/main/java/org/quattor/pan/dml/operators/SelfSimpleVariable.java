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

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_IN_COMPILE_TIME_CONTEXT;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_SELF_REF_IN_INCLUDE;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;

/**
 * Looks up a simple variable in the execution context.
 * 
 * @author loomis
 * 
 */
final public class SelfSimpleVariable extends SimpleVariable {

	public SelfSimpleVariable(SourceRange sourceRange, boolean lookupOnly) {
		super(sourceRange, "SELF", lookupOnly);
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

		// Look up the self value.
		return context.getSelf();
	}

}
