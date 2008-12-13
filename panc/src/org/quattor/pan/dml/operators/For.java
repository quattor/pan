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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/Foreach.java $
 $Id: Foreach.java 3597 2008-08-17 09:08:57Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_LOOP_TEST;
import static org.quattor.pan.utils.MessageUtils.MSG_LOOP_ITERATION_LIMIT;

import org.quattor.pan.dml.AbstractOperation;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.MessageUtils;

/**
 * Implements a for loop that allows iteration over all elements in a resource
 * without having to explicitly create an iterator. Structural changes to the
 * resource during the iteration are not permitted.
 * 
 * @author loomis
 * 
 */
final public class For extends AbstractOperation {

	private static final long serialVersionUID = 5119297456292814496L;

	public For(SourceRange sourceRange, Operation... ops) {
		super(sourceRange, ops);
		assert (ops.length == 4);
	}

	@Override
	public Element execute(Context context) {

		Operation initialization = ops[0];
		Operation condition = ops[1];
		Operation increment = ops[2];
		Operation body = ops[3];

		// The initial result is set to the initialization value. If the check
		// fails immediately and no iterations are done, then this is the value
		// of the loop.
		Element result = initialization.execute(context);

		// Get the result of the condition.
		boolean test = false;
		try {
			test = ((BooleanProperty) condition.execute(context)).getValue()
					.booleanValue();
		} catch (ClassCastException cce) {
			throw new EvaluationException(MessageUtils
					.format(MSG_INVALID_LOOP_TEST), sourceRange, context);
		}

		// Retrieve the iteration limit from the context.
		int limit = context.getIterationLimit();

		// Now actually do the loop.
		int count = 0;
		while (test) {

			// Increment the counter and ensure that the iteration limit hasn't
			// been exceeded.
			count++;
			if (count > limit) {
				throw new EvaluationException(MessageUtils.format(
						MSG_LOOP_ITERATION_LIMIT, Long.valueOf(limit)),
						sourceRange, context);
			}

			// Execute the body of the loop.
			result = body.execute(context);

			// Execute the iteration DML block.
			increment.execute(context);

			// Re-evaluate the loop condition.
			try {
				test = ((BooleanProperty) condition.execute(context))
						.getValue().booleanValue();
			} catch (ClassCastException cce) {
				throw new EvaluationException(MessageUtils
						.format(MSG_INVALID_LOOP_TEST), sourceRange, context);
			}

		}

		return result;
	}
}
