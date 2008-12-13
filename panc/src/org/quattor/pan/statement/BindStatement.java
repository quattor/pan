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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/BindStatement.java $
 $Id: BindStatement.java 2799 2008-01-26 17:10:23Z loomis $
 */

package org.quattor.pan.statement;

import static org.quattor.pan.utils.MessageUtils.MSG_ABSOLUTE_PATH_ONLY_FOR_BIND;

import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.type.FullType;
import org.quattor.pan.utils.Path;

/**
 * Associates a FullType (which may have a validation function) to a path.
 * 
 * @author loomis
 * 
 */
public class BindStatement extends Statement {

	private static final long serialVersionUID = -2990576311126608312L;

	private final Path path;

	private final FullType fullType;

	/**
	 * This constructor creates a new BindStatement which associates a FullType
	 * to a particular, absolute path.
	 * 
	 * @param sourceRange
	 *            source location of this statement
	 * @param path
	 *            absolute Path to associate with the FullType
	 * @param fullType
	 *            type to associate with the Path
	 */
	public BindStatement(SourceRange sourceRange, Path path, FullType fullType)
			throws SyntaxException {

		super(sourceRange);

		// Check that the arguments are acceptable.
		assert (path != null);
		assert (fullType != null);
		if (!path.isAbsolute()) {
			throw SyntaxException.create(sourceRange,
					MSG_ABSOLUTE_PATH_ONLY_FOR_BIND, path);
		}

		// Copy in the information.
		this.path = path;
		this.fullType = fullType;
	}

	@Override
	public void execute(Context context) {
		assert (context != null);
		context.setBinding(path, fullType, context.getCurrentTemplate(),
				getSourceRange());
	}

	/**
	 * Return a reasonable string representation of this statement.
	 * 
	 * @return String representation of this BindStatement
	 */
	@Override
	public String toString() {
		return "BIND: " + path + ", " + fullType;
	}

}
