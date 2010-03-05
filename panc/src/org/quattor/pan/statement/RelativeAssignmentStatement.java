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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/RelativeAssignmentStatement.java $
 $Id: RelativeAssignmentStatement.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.statement;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_PATH_ASSIGNMENT;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.PathSelfHolder;
import org.quattor.pan.template.SelfHolder;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.Path;

/**
 * Assigns a constant or computed value to a relative path. If the value is
 * Null, then the corresponding path will be removed.
 * 
 * @author loomis
 * 
 */
public class RelativeAssignmentStatement extends ComputedAssignmentStatement {

	private static final long serialVersionUID = -2100809273274847860L;

	/**
	 * This constructor creates a new AssignmentStatement which assigns a the
	 * result of a DML block to the associated path.
	 * 
	 * @param sourceRange
	 *            source location of this statement
	 * @param path
	 *            machine configuration path (non-external) to modify
	 * @param dml
	 *            DML block to evaluate for the path's value
	 * @param conditional
	 *            flag indicating if this is a conditional assignment (i.e. if
	 *            the value already exists, don't do anything)
	 * @param modifiable
	 *            flag indicating if the path can be further modified (i.e.
	 *            'final' functionality)
	 */
	protected RelativeAssignmentStatement(SourceRange sourceRange, Path path,
			Operation dml, boolean conditional, boolean modifiable)
			throws SyntaxException {

		super(sourceRange, path, dml, conditional, modifiable);

		// Check that the path is actually an absolute one.
		if (!path.isRelative()) {
			throw CompilerError.create(MSG_INVALID_PATH_ASSIGNMENT, path
					.toString());
		}
	}

	@Override
	public void execute(Context context) throws EvaluationException {

		assert (context != null);

		try {

			// Get the value to use for the self variable. Will need this in all
			// cases anyway. If the value doesn't exist, set self to undef.
			// NOTE: we must also put this into the tree so that we can maintain
			// backward compatability when value() is called with the path
			// currently being assigned.
			SelfHolder selfHolder = new PathSelfHolder(path, context);
			context.initializeSelfHolder(selfHolder);
			Element self = selfHolder.getElement();
			assert (self != null);

			// FIXME: Final flags are not properly processed for relative paths.

			if (!conditional || (self instanceof Undef)) {

				Element result = context.executeDmlBlock(dml);

				// Set the value.
				if (result != null) {

					// Duplicate the result ONLY if it isn't the same as self.
					// This is an important optimization as a common action is
					// to modify the contents of self and put the value back
					// into the tree. (E.g. all of the software package list
					// functions.)
					if (result != self) {
						result = result.duplicate();
					}

					// Insert the (possibly duplicated) element into the tree.
					context.putElement(path, result);

				} else {

					// Remove the element.
					context.putElement(path, null);

				}

			}

			// FIXME: Final flag processing for relative paths needs to be
			// implemented.

			// Clear out the self reference to make sure it does not leak into
			// another calculation.
			context.clearSelf();

		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(getSourceRange(), context);
		}
	}

}
