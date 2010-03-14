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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/parser/ASTBaseTypeSpec.java $
 $Id: ASTBaseTypeSpec.java 3515 2008-07-31 13:20:05Z loomis $
 */

package org.quattor.pan.parser;

import org.quattor.pan.utils.Range;

public class ASTBaseTypeSpec extends SimpleNode {

	private Range range = null;
	
	private boolean extensible = false;
	
	private String identifier = null;

	public ASTBaseTypeSpec(int id) {
		super(id);
	}

	public ASTBaseTypeSpec(PanParser p, int id) {
		super(p, id);
	}

	public void setExtensible(boolean extensible) {
		this.extensible = extensible;
	}

	public boolean isExtensible() {
		return extensible;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setRange(Range range) {
		this.range = range;
	}
	
	public Range getRange() {
		return range;
	}

	@Override
	public String toString() {
		String s = "";
		if (identifier!=null) {
			s = s + identifier+" ";
		} else {
			if (extensible) {
				s = s + "extensible ";
			}
			s = s + "record ";
		}
		if (range!=null) {
			s = s + range;
		}
		return s;
	}

}
