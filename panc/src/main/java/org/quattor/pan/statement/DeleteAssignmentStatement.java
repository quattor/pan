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
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.utils.Path;

/**
 * Assigns either a constant or computed value to an absolute path. If the value
 * is Null, then the path is deleted.
 *
 * @author loomis
 *
 */
public class DeleteAssignmentStatement extends AssignmentStatement {

	/**
	 * This constructor creates a new AbsoluteAssignmentStatement that assigns a
	 * constant value (Element) to the associated path.
	 *
	 * @param sourceRange
	 *            source location of this statement
	 * @param path
	 *            machine configuration path (non-external) to modify
	 * @param conditional
	 *            flag indicating if this is a conditional assignment (i.e. if
	 *            the value already exists, don't do anything)
	 * @param modifiable
	 *            flag indicating if the path can be further modified (i.e.
	 *            'final' functionality)
	 */
	protected DeleteAssignmentStatement(SourceRange sourceRange, Path path,
			boolean conditional, boolean modifiable) throws SyntaxException {

		super(sourceRange, path, conditional, modifiable);
	}

	@Override
	public Element execute(Context context) throws EvaluationException {

		assert (context != null);

		try {

			if (!conditional) {

				// Check that the path isn't marked as final.
				if (path.isAbsolute() && context.isFinal(path)) {
					throw new EvaluationException(context.getFinalReason(path),
							getSourceRange());
				}

				// Delete the value.
				context.putElement(path, null);
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
        return null;
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
		sb.append("null;");
		return sb.toString();
	}

}
