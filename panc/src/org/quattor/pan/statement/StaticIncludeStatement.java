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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/StaticIncludeStatement.java $
 $Id: StaticIncludeStatement.java 2799 2008-01-26 17:10:23Z loomis $
 */

package org.quattor.pan.statement;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_IDENTIFIER;

import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Executes a template identified by a constant string.
 * 
 * @author loomis
 * 
 */
public class StaticIncludeStatement extends IncludeStatement {

	private final String name;

	/**
	 * Constructs a StaticIncludeStatement from the given template name. If the
	 * name is invalid, a SyntaxException will be thrown.
	 * 
	 * @param sourceRange
	 *            source location of this statement
	 * @param name
	 *            name of the template to include
	 * @throws SyntaxException
	 */
	public StaticIncludeStatement(SourceRange sourceRange, String name)
			throws SyntaxException {

		super(sourceRange);

		// Check the template name.
		assert (name != null);
		if (!validIdentifier(name)) {
			throw SyntaxException.create(sourceRange, MSG_INVALID_IDENTIFIER,
					name);
		}

		// All OK. Copy the value.
		this.name = name;
	}

	@Override
	public void execute(Context context) {
		executeWithNamedTemplate(context, name);
	}

	/**
	 * Return a reasonable string representation of this statement.
	 * 
	 * @return String representation of this BindStatement
	 */
	@Override
	public String toString() {
		return "STATIC INCLUDE: " + name;
	}

}
