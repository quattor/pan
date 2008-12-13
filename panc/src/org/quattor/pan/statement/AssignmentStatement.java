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

import static org.quattor.pan.utils.MessageUtils.MSG_NO_ASSIGNMENT_TO_EXTERNAL_PATH;
import static org.quattor.pan.utils.MessageUtils.MSG_REACHED_IMPOSSIBLE_BRANCH;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.CompileTimeContext;
import org.quattor.pan.template.Context;
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
abstract public class AssignmentStatement extends Statement {

	private static final long serialVersionUID = -3066427950732309886L;

	protected final Path path;

	protected final boolean conditional;

	protected final boolean modifiable;

	protected AssignmentStatement(SourceRange sourceRange, Path path,
			boolean conditional, boolean modifiable) throws SyntaxException {

		super(sourceRange);

		// Check that the arguments are acceptable.
		assert (path != null);

		// External paths are not acceptable for assignment statements.
		if (path.isExternal()) {
			throw SyntaxException.create(getSourceRange(),
					MSG_NO_ASSIGNMENT_TO_EXTERNAL_PATH, path);
		}

		// Copy in the information.
		this.path = path;
		this.conditional = conditional;
		this.modifiable = modifiable;
	}

	public boolean isAbsolute() {
		return path.isAbsolute();
	}

	public boolean isRelative() {
		return path.isRelative();
	}

	public static AssignmentStatement createAssignmentStatement(
			SourceRange sourceRange, Path path, Element value,
			boolean conditional, boolean modifiable) throws SyntaxException {

		AssignmentStatement stmt = null;

		if (value != null) {
			if (!(value instanceof Null)) {
				stmt = new ConstantAssignmentStatement(sourceRange, path,
						value, conditional, modifiable);
			} else {
				stmt = new DeleteAssignmentStatement(sourceRange, path,
						conditional, modifiable);
			}
		}

		return stmt;
	}

	public static AssignmentStatement createAssignmentStatement(
			SourceRange sourceRange, Path path, Operation dml,
			boolean conditional, boolean modifiable) throws SyntaxException {

		// Run the DML block to see if it evaluates to a compile-time constant.
		// If so, use the value to produce an optimized statement.
		Element value = null;
		try {
			Context context = new CompileTimeContext();
			value = context.executeDmlBlock(dml);
		} catch (EvaluationException consumed) {
			// Ignore the exception and allow it to be caught at run time.
		}

		if (value != null) {
			return AssignmentStatement.createAssignmentStatement(sourceRange,
					path, value, conditional, modifiable);
		} else if (path.isAbsolute()) {
			return new AbsoluteAssignmentStatement(sourceRange, path, dml,
					conditional, modifiable);
		} else if (path.isRelative()) {
			return new RelativeAssignmentStatement(sourceRange, path, dml,
					conditional, modifiable);
		} else {
			throw CompilerError.create(MSG_REACHED_IMPOSSIBLE_BRANCH);
		}
	}

}
