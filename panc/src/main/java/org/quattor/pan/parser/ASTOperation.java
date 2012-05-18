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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/parser/ASTOperation.java $
 $Id: ASTOperation.java 3615 2008-08-20 20:00:02Z loomis $
 */

package org.quattor.pan.parser;

import org.quattor.pan.dml.Operation;

/**
 * This node implements the methods required for JJTree and also provides the
 * methods to associate an Operation with the node.
 * 
 * @author loomis
 * 
 */
public class ASTOperation extends SimpleNode {

	static public enum OperationType {
		DML, PLUS, MINUS, NOT, BIT_NOT, FUNCTION, ASSIGN, IF, WHILE, FOR, FOREACH, VARIABLE, WITH, DEFAULT, HEREDOC, LITERAL
	}

	private OperationType operationType = OperationType.DML;

	private Operation operation = null;

	public ASTOperation(int id) {
		super(id);
	}

	public ASTOperation(PanParser p, int id) {
		super(p, id);
	}

	/**
	 * Subclasses should call this method to set the Operation for this node.
	 */
	protected void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	@Override
	public Object getSubtype() {
		return operationType;
	}

	/**
	 * Return the associated Operation. Subclasses should not need to override
	 * this method.
	 */
	public OperationType getOperationType() {
		return operationType;
	}

	/**
	 * Subclasses should call this method to set the Operation for this node.
	 */
	protected void setOperation(Operation operation) {
		this.operation = operation;
	}

	/**
	 * Return the associated Operation. Subclasses should not need to override
	 * this method.
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * Provide a reasonable String representation of this node.
	 */
	@Override
	public String toString() {
		if (operationType != OperationType.LITERAL) {
			return operationType.toString();
		} else {
			return operationType.toString() + " (" + operation.toString() + ")";
		}
	}

}
