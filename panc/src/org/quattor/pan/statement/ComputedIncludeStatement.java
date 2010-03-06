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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/ComputedIncludeStatement.java $
 $Id: ComputedIncludeStatement.java 1858 2007-06-16 16:58:18Z loomis $
 */

package org.quattor.pan.statement;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.InvalidSelfHolder;
import org.quattor.pan.template.SelfHolder;
import org.quattor.pan.template.SourceRange;

/**
 * Evaluates a DML expression and then executes the named template. If the DML
 * block evaluates to Undef or Null, nothing is done.
 * 
 * @author loomis
 * 
 */
public class ComputedIncludeStatement extends IncludeStatement {

	private final Operation dml;

	/**
	 * Constructor to create a ComputedIncludeStatement which will execute a
	 * template named by the result of the DML block. If the result of the DML
	 * block is Undef or Null, then this statement does nothing. If the DML
	 * block evaluates to something other than a StringProperty, Undef, or Null
	 * an exception is thrown.
	 * 
	 * @param sourceRange
	 *            source location of this statement
	 * @param dml
	 *            DML block used to compute the template name
	 */
	public ComputedIncludeStatement(SourceRange sourceRange, Operation dml) {

		super(sourceRange);

		assert (dml != null);
		this.dml = dml;
	}

	@Override
	public void execute(Context context) {

		SelfHolder selfHolder = new InvalidSelfHolder();
		context.initializeSelfHolder(selfHolder);

		Element result = context.executeDmlBlock(dml);

		context.clearSelf();

		// If the result is null or undef, then no template is to be included.
		if (result instanceof Null || result instanceof Undef) {
			return;
		}

		// Check that the result is actually a string and set the name.
		String name = null;
		try {
			name = ((StringProperty) result).getValue();
		} catch (ClassCastException cce) {
			// FIXME: This should include source information if possible.
			throw new EvaluationException(
					"DML block for computed include statement didn't evaluate to undef, null, or a string",
					null);
		}

		// Check that the computed name is actually valid.
		if (!validIdentifier(name)) {
			throw new EvaluationException("invalid identifier for include: "
					+ name, this.getSourceRange());
		}

		executeWithNamedTemplate(context, name);
	}

	/**
	 * Return a reasonable string representation of this statement.
	 * 
	 * @return String representation of this ComputedIncludeStatement
	 */
	@Override
	public String toString() {
		return "COMPUTED INCLUDE: " + dml;
	}

}
