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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/parser/ASTTypeClause.java $
 $Id: ASTTypeClause.java 3515 2008-07-31 13:20:05Z loomis $
 */

package org.quattor.pan.parser;

import org.quattor.pan.utils.Range;

public class ASTTypeClause extends SimpleNode {

	static public enum ClauseType {
		LIST, HASH, LINK
	}

	private ClauseType clauseType = null;

	private Range range = null;

	public ASTTypeClause(int id) {
		super(id);
	}

	public ASTTypeClause(PanParser p, int id) {
		super(p, id);
	}

	public void setClauseType(ClauseType clauseType) {
		this.clauseType = clauseType;
	}

	public ClauseType getClauseType() {
		return clauseType;
	}

	public void setRange(Range range) {
		this.range = range;
	}

	public Range getRange() {
		return range;
	}

	@Override
	public String toString() {
		return "ClauseType (" + clauseType + ", " + range + ")";
	}

}
