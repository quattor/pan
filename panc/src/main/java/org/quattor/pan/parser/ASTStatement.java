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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/parser/ASTStatement.java $
 $Id: ASTStatement.java 3515 2008-07-31 13:20:05Z loomis $
 */

package org.quattor.pan.parser;

public class ASTStatement extends SimpleNode {

	static public enum StatementType {
		NOOP, BIND, ASSIGN, VARIABLE, TYPE, FUNCTION, INCLUDE, PREFIX
	}

	private StatementType type = null;
	private String identifier = null;
	private boolean conditionalFlag = false;
	private boolean finalFlag = false;

	public ASTStatement(int id) {
		super(id);
	}

	public ASTStatement(PanParser p, int id) {
		super(p, id);
	}

	@Override
	public Object getSubtype() {
		return type;
	}

	public void setStatementType(StatementType type) {
		this.type = type;
	}

	public StatementType getStatementType() {
		return type;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setConditionalFlag(boolean flag) {
		conditionalFlag = flag;
	}

	public boolean getConditionalFlag() {
		return conditionalFlag;
	}

	public void setFinalFlag(boolean flag) {
		finalFlag = flag;
	}

	public boolean getFinalFlag() {
		return finalFlag;
	}

	@Override
	public String toString() {
		String s = type.toString();
		if (identifier != null) {
			s = s + "(" + identifier + ")";
		}
		return s;
	}

}
