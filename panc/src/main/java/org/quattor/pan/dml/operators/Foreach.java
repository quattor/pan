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
 $Id: Foreach.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.quattor.pan.utils.MessageUtils.MSG_CONCURRENT_MODIFICATION;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_FOREACH_RESOURCE;

import java.util.ConcurrentModificationException;

import org.quattor.pan.dml.AbstractOperation;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.MessageUtils;

/**
 * Implements a foreach loop that allows iteration over all elements in a
 * resource without having to explicitly create an iterator. Structural changes
 * to the resource during the iteration are not permitted.
 * 
 * @author loomis
 * 
 */
final public class Foreach extends AbstractOperation {

	public Foreach(SourceRange sourceRange, Operation... ops) {
		super(sourceRange, ops);
		assert (ops.length == 4);
		assert (ops[0] instanceof SetValue && ops[1] instanceof SetValue);
	}

	@Override
	public Element execute(Context context) {

		SetValue keyVariable = (SetValue) ops[0];
		SetValue valueVariable = (SetValue) ops[1];
		Operation resourceDml = ops[2];
		Operation body = ops[3];

		// If nothing is executed, then the result is undef.
		Element result = Undef.VALUE;

		// Get the resource to iterate.
		Resource resource = null;
		try {
			resource = (Resource) resourceDml.execute(context);
		} catch (ClassCastException cce) {
			throw new EvaluationException(MessageUtils
					.format(MSG_INVALID_FOREACH_RESOURCE), sourceRange);
		}

		// Set the iteration variables to undef before starting. This is done to
		// ensure that the iteration variables are in a known state even if the
		// resource has no entries.
		keyVariable.execute(context, Undef.VALUE);
		valueVariable.execute(context, Undef.VALUE);

		try {

			for (Resource.Entry entry : resource) {

				// Set the key.
				keyVariable.execute(context, entry.getKey());

				// Set the value. Always set to undef first to allow iteration
				// over resources with different types of children.
				valueVariable.execute(context, Undef.VALUE);
				valueVariable.execute(context, entry.getValue());

				// Execute the body of the loop.
				result = body.execute(context);
			}

		} catch (ConcurrentModificationException cme) {
			throw new EvaluationException(MessageUtils
					.format(MSG_CONCURRENT_MODIFICATION), sourceRange);
		}

		return result;
	}
}
