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
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.MessageUtils;

/**
 * Looks up a simple variable in the execution context.
 * 
 * @author loomis
 * 
 */
public class SimpleVariable extends Variable {

	private static final long serialVersionUID = 7924181632989560156L;

	final private static Operation[] emptyOps = new Operation[] {};

	public SimpleVariable(SourceRange sourceRange, String identifier,
			boolean lookupOnly) {
		super(sourceRange, identifier, lookupOnly, emptyOps);
		assert (identifier != null);
	}

	@Override
	public Element execute(Context context) {

		// Look up the variable.
		Element result = context.getVariable(identifier);

		// Return an error if the variable doesn't exist.
		if (result == null && !lookupOnly) {
			throw new EvaluationException(MessageUtils.format(
					MSG_UNDEFINED_VAR, identifier), sourceRange, context);
		}

		return result;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + identifier + ","
				+ ops.length + ")";
	}
}
