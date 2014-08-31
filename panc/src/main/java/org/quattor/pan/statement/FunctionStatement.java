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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/FunctionStatement.java $
 $Id: FunctionStatement.java 1858 2007-06-16 16:58:18Z loomis $
 */

package org.quattor.pan.statement;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Associates a DML block to a given function name. A function may only be
 * defined once during the construction of any given machine profile.
 *
 * @author loomis
 *
 */
public class FunctionStatement extends Statement {

	private final String name;

	private final Operation function;

	/**
	 * Creates a FunctionStatement which associates a name with a given DML
	 * block.
	 *
	 * @param sourceRange
	 *            source location of this statement
	 * @param name
	 *            name of the function
	 * @param function
	 *            DML block for the function
	 */
	public FunctionStatement(SourceRange sourceRange, String name,
			Operation function) {

		super(sourceRange);

		// Copy in the information.
		assert (name != null);
		this.name = name;
		this.function = function;
	}

	/**
	 * Retrieve the name of the defined function. This information is used by
	 * Template for compile-time error checking.
	 *
	 * @return name of the defined function
	 */
	public String getName() {
		return name;
	}

	@Override
	public Element execute(Context context) {
		try {
			context.setFunction(name, function, context.getCurrentTemplate(),
					getSourceRange());
		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(getSourceRange(), context);
		}
        return null;
	}

	/**
	 * Return a reasonable string representation of this statement.
	 *
	 * @return String representation of this FunctionStatement
	 */
	@Override
	public String toString() {
		return "FUNCTION: " + name + ", " + function;
	}

}
