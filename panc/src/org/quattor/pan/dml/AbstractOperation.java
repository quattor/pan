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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/AbstractOperation.java $
 $Id: AbstractOperation.java 3506 2008-07-30 18:09:38Z loomis $
 */

package org.quattor.pan.dml;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

/**
 * Abstract class which implements the Operation interface and provides some
 * functionality common to all DML components.
 * 
 * @author loomis
 * 
 */
abstract public class AbstractOperation implements Operation {

	private static final long serialVersionUID = -5184177657624540883L;

	/**
	 * Source location of this operation and its arguments.
	 */
	final protected SourceRange sourceRange;

	/**
	 * Array of arguments (as operations) for this operation.
	 */
	final protected Operation[] ops;

	/**
	 * Set the source location information and arguments for this operation.
	 * 
	 * @param sourceRange
	 *            source location of this operation and arguments
	 * @param operations
	 *            list of arguments (as operations) for this operation
	 */
	public AbstractOperation(SourceRange sourceRange, Operation... operations) {
		this.sourceRange = sourceRange;
		this.ops = operations.clone();
	}

	// Description will be taken from interface.
	abstract public Element execute(Context context) throws EvaluationException;

	/**
	 * Default implementation recursively calls this method on all of the
	 * contained operations (arguments). Subclasses that cannot appear in a
	 * restricted context (array indices or function arguments) should override
	 * this method and throw a SyntaxException.
	 * 
	 * @throws SyntaxException
	 *             if operation cannot appear in restricted context
	 */
	public void checkRestrictedContext() throws SyntaxException {
		for (Operation op : ops) {
			op.checkRestrictedContext();
		}
	}

	/**
	 * A utility method which calls <code>execute</code> on each of this
	 * operation's arguments and returns an array of the results.
	 * 
	 * @param context
	 *            evaluation context to use
	 * 
	 * @return array of the results of executing the arguments
	 */
	protected Element[] calculateArgs(Context context)
			throws EvaluationException {

		Element[] results = new Element[ops.length];
		for (int i = 0; i < ops.length; i++) {
			results[i] = ops[i].execute(context);
		}

		return results;
	}

	/**
	 * A utility method that creates a list of terms from the given arguments.
	 * 
	 * @param context
	 *            evaluation context to use
	 * 
	 * @return array of terms calculated from the operations
	 * 
	 * @throws EvaluationException
	 *             if any error occurs when evaluating the arguments or if the
	 *             resulting value is not a valid Term
	 */
	protected Term[] calculateTerms(Context context) throws EvaluationException {

		Term[] terms = new Term[ops.length];
		for (int i = 0; i < ops.length; i++) {
			terms[i] = TermFactory.create(ops[i].execute(context));
		}

		return terms;
	}

	/**
	 * Retrieve the source information from this operation. The SourceRange is
	 * available to subclasses as a protected field, so subclasses can access
	 * the value directly.
	 * 
	 * @return SourceRange giving location of this operation and its arguments
	 */
	public SourceRange getSourceRange() {
		return sourceRange;
	}

	/**
	 * Allow a copy of the raw operations to be retrieved.
	 */
	public Operation[] getOperations() {
		return ops.clone();
	}

	/**
	 * Default string representation of an operation is the class' simple name.
	 * 
	 * @return string of class' simple name
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
