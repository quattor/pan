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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/DML.java $
 $Id: DML.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.dml;

import java.util.LinkedList;
import java.util.List;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * This operation contains a list of other DML operations and acts like a
 * procedure. When executed, a DML object just pushes all of the operations onto
 * the operation stack.
 * 
 * @author loomis
 * 
 */
public class DML extends AbstractOperation {

	/**
	 * Create new DML instance from variable list of arguments (as operations).
	 * 
	 * @param sourceRange
	 *            source location of this operation and its arguments
	 * @param operations
	 *            variable list of arguments
	 */
	protected DML(SourceRange sourceRange, Operation... operations) {
		super(sourceRange, operations);
	}

	/**
	 * Factory method to create a new DML block, although this may return
	 * another Operation because of optimization.
	 * 
	 * @param sourceRange
	 *            location of this block in the source file
	 * @param operations
	 *            the operations that make up this block
	 * 
	 * @return optimized Operation representing DML block
	 */
	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) {

		Operation result = null;

		// Build a duplicate list to "flatten" the DML block.
		List<Operation> list = new LinkedList<Operation>();

		// Inline operations from referenced DML blocks.
		for (Operation op : operations) {
			if (op instanceof DML) {
				DML dml = (DML) op;
				for (Operation dmlOp : dml.ops) {
					list.add(dmlOp);
				}
			} else {
				list.add(op);
			}
		}

		// If the DML block contains only a single operation, then just return
		// that operation. Otherwise create a new DML block and return that.
		if (list.size() == 1) {
			result = list.get(0);
		} else {
			Operation[] dmlOps = list.toArray(new Operation[list.size()]);
			result = new DML(sourceRange, dmlOps);
		}

		return result;
	}

	/**
	 * Execution of a DML block consists simply of executing the block's
	 * arguments in order. If no arguments are run, then the value of the block
	 * is <code>Undef</code>.
	 */
	@Override
	public Element execute(Context context) {
		Element result = Undef.VALUE;
		for (Operation op : ops) {
			try {
				result = op.execute(context);
			} catch (EvaluationException ee) {
				throw ee.addExceptionInfo(sourceRange, context);
			}
		}
		return result;
	}

	/**
	 * String representation of a DML block is a list of the operations within
	 * braces.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Operation op : ops) {
			sb.append(" ");
			sb.append(op.toString());
			sb.append(" ");
		}
		sb.append("}");
		return sb.toString();
	}

}
