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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/VariableStatement.java $
 $Id: VariableStatement.java 2728 2008-01-17 20:44:12Z loomis $
 */

package org.quattor.pan.statement;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.utils.GlobalVariable;

/**
 * Sets a global variable to a constant or computed value.
 *
 * @author loomis
 *
 */
public class ConstantVariableStatement extends VariableStatement {

	protected final Element value;

	protected ConstantVariableStatement(SourceRange sourceRange, String name,
			Element value, boolean conditional, boolean modifiable)
			throws SyntaxException {

		super(sourceRange, name, conditional, modifiable);

		// The value must be protected! Otherwise compile time constants can be
		// modified and then erroneously passed to other configurations.
		this.value = value.protect();
	}

	@Override
	public Element execute(Context context) {

		try {

			// Get the value to use for the SELF variable. Will need this in all
			// cases anyway. Use the method that doesn't duplicate the value.
			// Children of the SELF can be set directly.
			GlobalVariable variable = context.retrieveGlobalVariable(name);

			Element currentValue = variable.getValue();
			assert (currentValue != null);

			if (!conditional || currentValue instanceof Undef
					|| currentValue instanceof Null) {

				variable.setValue(value);
				variable.setFinalFlag(!modifiable);

			} else {

				if (conditional && !modifiable) {

					// Strange case where real value exists, but need to
					// make the value immutable.
					variable.setFinalFlag(!modifiable);

				}
			}

		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(getSourceRange(), context);
		}
        return null;
	}

	@Override
	public String toString() {
		return "VARIABLE: " + name + ", " + conditional + ", " + modifiable
				+ ", " + value;
	}

}
