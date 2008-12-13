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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/Statement.java $
 $Id: Statement.java 3506 2008-07-30 18:09:38Z loomis $
 */

package org.quattor.pan.statement;

import java.io.Serializable;

import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Provides the superclass of all declarative statements in the pan
 * configuration language. All declarative pan statements operate on the Context
 * of a particular machine profile through the <code>execute</code> method.
 * The arguments and flags for each statement are expected to be different, but
 * this is not important for evaluating the configuration.
 * 
 * All statements must be immutable.
 * 
 * All statements must be serializable so that object file versions of the
 * template can be written to disk.
 * 
 * Subclasses are expected to throw an exception (SyntaxException or
 * EvaluationException) if illegal parameters are passed to the constructor.
 * Similarly, if an error is encountered during the execute() method, an
 * EvaluationException should be thrown.
 * 
 * @author loomis
 * 
 */
abstract public class Statement implements Serializable {

	private static final long serialVersionUID = 1276863681119794796L;

	private final SourceRange sourceRange;

	/**
	 * The base constructor for Statement takes a SourceRange object which
	 * indicates the location of the statement within the source file. All
	 * subclasses must call this constructor as part of their constructors.
	 * 
	 * @param sourceRange
	 *            the location of this statement within the source file
	 */
	public Statement(SourceRange sourceRange) {
		assert (sourceRange != null);
		this.sourceRange = sourceRange;
	}

	/**
	 * Retrieve the source location for this Statement.
	 * 
	 * @return SourceRange object indicating the source location
	 */
	public SourceRange getSourceRange() {
		return sourceRange;
	}

	/**
	 * Execute this Statement within the given context.
	 * 
	 * @param context
	 *            DML context to use for the evalution of this statement
	 * 
	 * @throws EvaluationException
	 */
	abstract public void execute(Context context)
			throws EvaluationException;

}
