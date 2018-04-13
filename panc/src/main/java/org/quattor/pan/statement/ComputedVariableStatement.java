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

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SelfHolder;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.ttemplate.VariableSelfHolder;
import org.quattor.pan.utils.GlobalVariable;

/**
 * Sets a global variable to a constant or computed value.
 *
 * @author loomis
 *
 */
public class ComputedVariableStatement extends VariableStatement {

	protected final Operation dml;

	/**
	 * Creates a VariableStatement which assign a global variable to the result
	 * of a DML block.
	 *
	 * @param sourceRange
	 *            source location of this statement
	 * @param name
	 *            name of the global variable
	 * @param dml
	 *            DML block to evaluate
	 * @param conditional
	 *            flag indicating if this is a conditional assignment
	 * @param modifiable
	 *            flag indicating if the variable can be modified after this
	 *            statement executes (i.e. final functionality)
	 */
	protected ComputedVariableStatement(SourceRange sourceRange, String name,
			Operation dml, boolean conditional, boolean modifiable)
			throws SyntaxException {

		super(sourceRange, name, conditional, modifiable);

		this.dml = dml;
	}

	@Override
	public Element execute(Context context) {

		try {

			// Get the value to use for the SELF variable. Will need this in all
			// cases anyway. Use the method that doesn't duplicate the value.
			// Children of the SELF can be set directly.
			GlobalVariable variable = context.retrieveGlobalVariable(this.name);
			SelfHolder selfHolder = new VariableSelfHolder(variable);
			context.initializeSelfHolder(selfHolder);

			Element currentValue = variable.getValue();
			assert (currentValue != null);

			if (!conditional || currentValue instanceof Undef) {

				Element result = context.executeDmlBlock(dml);

				variable.setValue(result);
				variable.setFinalFlag(!modifiable);

			} else {

				if (conditional && !modifiable) {

					// Strange case where real value exists, but need to
					// make the value immutable.
					variable.setFinalFlag(!modifiable);

				}
			}

			// Clear out the self reference to make sure it does not leak into
			// another calculation.
			context.clearSelf();

		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(getSourceRange(), context);
		}
        return null;
	}

	/**
	 * Return a reasonable string representation of this statement.
	 *
	 * @return String representation of this VariableStatement
	 */
	@Override
	public String toString() {
		return "VARIABLE: " + name + ", " + conditional + ", " + modifiable
				+ ", " + dml;
	}

}
