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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Delete.java $
 $Id: Delete.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_FIRST_ARG_VARIABLE_REF;
import static org.quattor.pan.utils.MessageUtils.MSG_ONE_ARG_REQ;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.dml.operators.SetValue;
import org.quattor.pan.dml.operators.Variable;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Deletes the variable referenced in the function's argument.
 * 
 * @author loomis
 * 
 */
final public class Delete extends BuiltInFunction {

	private static final long serialVersionUID = 2929895183002177658L;

	private Delete(SourceRange sourceRange, Operation... operations) {
		super(sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Must be exactly one argument.
		if (operations.length != 1) {
			throw SyntaxException
					.create(sourceRange, MSG_ONE_ARG_REQ, "delete");
		}

		// The argument must be a variable reference.
		if (!(operations[0] instanceof Variable)) {
			throw SyntaxException.create(sourceRange,
					MSG_FIRST_ARG_VARIABLE_REF, "delete");
		}

		// Convert the variable reference to a SetValue operation.
		operations[0] = SetValue.getInstance((Variable) operations[0]);

		return new Delete(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {
		assert (ops[0] instanceof SetValue);
		((SetValue) ops[0]).execute(context, null);
		return Undef.VALUE;
	}

	@Override
	public String toString() {
		return "delete()";
	}

}
