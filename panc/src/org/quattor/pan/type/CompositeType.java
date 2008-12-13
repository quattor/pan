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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/Sources/panc/trunk/src/org/quattor/pan/type/BuiltInType.java $
 $Id: BuiltInType.java 1174 2007-01-27 14:28:27Z loomis $
 */

package org.quattor.pan.type;

import org.quattor.pan.template.SourceRange;
import org.quattor.pan.template.TypeMap;
import org.quattor.pan.utils.Range;

/**
 * Common abstract class for all pan language composite types. These types
 * include collections and links.
 * 
 * @author loomis
 * 
 */
public abstract class CompositeType extends BaseType {

	private static final long serialVersionUID = 5605846304150260905L;

	protected final BaseType baseType;

	protected final Range range;

	public CompositeType(String source, SourceRange sourceRange,
			BaseType baseType, Range range) {
		super(source, sourceRange);

		assert (baseType != null);

		this.baseType = baseType;
		this.range = range;
	}

	@Override
	public void verifySubtypesDefined(TypeMap types) {
		baseType.verifySubtypesDefined(types);
	}

}
