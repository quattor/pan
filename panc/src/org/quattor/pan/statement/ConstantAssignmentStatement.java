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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/AbsoluteAssignmentStatement.java $
 $Id: AbsoluteAssignmentStatement.java 2659 2008-01-07 14:48:07Z loomis $
 */

package org.quattor.pan.statement;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.Path;

/**
 * Assigns either a constant or computed value to an absolute path. If the value
 * is Null, then the path is deleted.
 * 
 * @author loomis
 * 
 */
public class ConstantAssignmentStatement extends AssignmentStatement {

	private static final long serialVersionUID = 6876660264664417932L;

	protected final Element value;

	/**
	 * This constructor creates a new AbsoluteAssignmentStatement that assigns a
	 * constant value (Element) to the associated path.
	 * 
	 * @param sourceRange
	 *            source location of this statement
	 * @param path
	 *            machine configuration path (non-external) to modify
	 * @param value
	 *            constant Element value to assign to path
	 * @param conditional
	 *            flag indicating if this is a conditional assignment (i.e. if
	 *            the value already exists, don't do anything)
	 * @param modifiable
	 *            flag indicating if the path can be further modified (i.e.
	 *            'final' functionality)
	 */
	protected ConstantAssignmentStatement(SourceRange sourceRange,
			Path path, Element value, boolean conditional, boolean modifiable)
			throws SyntaxException {

		super(sourceRange, path, conditional, modifiable);

		assert (value != null);
		assert (!(value instanceof Null));
		this.value = value.protect();
	}

	@Override
	public void execute(Context context) throws EvaluationException {

		assert (context != null);

		try {

			boolean setValue = true;

			// Must retrieve the value to determine if the value can be set.
			// Set the value if it doesn't exist or is set to Undef.
			if (conditional) {
				Element self = context.getElement(path, false);
				setValue = (self == null) || (self instanceof Undef);
			}

			if (setValue) {

				// Check that the path isn't marked as final.
				if (path.isAbsolute() && context.isFinal(path)) {
					throw new EvaluationException(context.getFinalReason(path),
							getSourceRange());
				}

				// Insert the value into the tree. The value is always protected
				// when the statement is created, so there is no need to do this
				// again.
				context.putElement(path, value);
			}

			// If the path is marked as final, set the value. This should be
			// done even if the assignment was conditional and the value wasn't
			// used.
			if (path.isAbsolute() && !modifiable) {
				context.setFinal(path);
			}

		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(getSourceRange(), context);
		}
	}

	/**
	 * Return a reasonable string representation of this statement.
	 * 
	 * @return String representation of this statement
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (!modifiable) {
			sb.append("final ");
		}
		sb.append(path.toString());
		if (conditional) {
			sb.append(" ?= ");
		} else {
			sb.append(" = ");
		}
		sb.append(value.toString());
		sb.append(";");
		return sb.toString();
	}

}
