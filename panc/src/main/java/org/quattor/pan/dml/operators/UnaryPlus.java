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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/UnaryPlus.java $
 $Id: UnaryPlus.java 3516 2008-07-31 13:38:22Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ARGS_UPLUS;

import org.quattor.pan.dml.AbstractOperation;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.NumberProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.utils.MessageUtils;

/**
 * Implements a unary plus operation for symmetry with the unary minus. Note
 * that this effectively has the same effect as using the <code>is_number</code>
 * function, except that the result is the unchanged value.
 * 
 * @author loomis
 * 
 */
final public class UnaryPlus extends AbstractOperation {

	private UnaryPlus(SourceRange sourceRange, Operation... operations) {
		super(sourceRange, operations);
		assert (operations.length == 1);
	}

	public static Operation newOperation(SourceRange sourceRange,
			Operation... ops) throws SyntaxException {

		assert (ops.length == 1);

		Operation result = null;

		// Attempt to optimize this operation.
		if (ops[0] instanceof Element) {

			try {
				NumberProperty number = (NumberProperty) ops[0];
				result = number;
			} catch (ClassCastException cce) {
				throw SyntaxException.create(sourceRange,
						MessageUtils.MSG_INVALID_ARGS_UPLUS);
			} catch (EvaluationException ee) {
				throw SyntaxException.create(sourceRange, ee);
			}

		} else {
			result = new UnaryPlus(sourceRange, ops);
		}

		return result;
	}

	@Override
	public Element execute(Context context) {

		try {
			Element[] args = calculateArgs(context);
			return args[0];
		} catch (ClassCastException cce) {
			throw new EvaluationException(MessageUtils
					.format(MSG_INVALID_ARGS_UPLUS), sourceRange);
		}

	}

}
