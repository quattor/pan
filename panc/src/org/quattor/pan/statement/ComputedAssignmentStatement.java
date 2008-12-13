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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/Sources/panc/trunk/src/org/quattor/pan/statement/AbsoluteAssignmentStatement.java $
 $Id: AbsoluteAssignmentStatement.java 1026 2006-11-19 15:51:22Z loomis $
 */

package org.quattor.pan.statement;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.Path;

/**
 * Assigns a constant or computed value to a path. If the computed value is
 * Null, then the corresponding path will be removed. This is an abstract super
 * class for absolute and relative assignment statements.
 * 
 * @author loomis
 * 
 */
abstract public class ComputedAssignmentStatement extends AssignmentStatement {

	private static final long serialVersionUID = -6528529422555807437L;

	protected final Operation dml;

	/**
	 * This constructor creates a new AssignmentStatement which assigns a
	 * constant value (Element) to the associated path.
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
	protected ComputedAssignmentStatement(SourceRange sourceRange, Path path,
			Operation dml, boolean conditional, boolean modifiable)
			throws SyntaxException {

		super(sourceRange, path, conditional, modifiable);

		// Check that the arguments are acceptable.
		assert (dml != null);

		// Copy in the information.
		this.dml = dml;
	}

}
