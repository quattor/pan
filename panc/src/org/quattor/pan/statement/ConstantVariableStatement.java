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
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Sets a global variable to a constant or computed value.
 * 
 * @author loomis
 * 
 */
public class ConstantVariableStatement extends VariableStatement {

	private static final long serialVersionUID = 5298022869411977030L;

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
	public void execute(Context context) {

		try {

			// Get the value to use for the SELF variable. Will need this in all
			// cases anyway. Use the method that doesn't duplicate the value.
			// Children of the SELF can be set directly.
			Element self = context.initializeSelf(name);
			assert (self != null);

			if (!conditional) {

				context.setGlobalVariable(name, value, !modifiable);

			} else {

				// Pull out the current value (if it exists) to determine if the
				// value should be set.
				Element currentValue = context.getGlobalVariable(name);

				if (currentValue == null || currentValue instanceof Undef
						|| currentValue instanceof Null) {

					context.setGlobalVariable(name, value, !modifiable);

				} else if (!modifiable) {

					// Strange case where real value exists, but need to
					// make the value immutable.
					context.setGlobalVariableAsFinal(name);

				}
			}

		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(getSourceRange(), context);
		}
	}

	@Override
	public String toString() {
		return "VARIABLE: " + name + ", " + conditional + ", " + modifiable
				+ ", " + value;
	}

}
