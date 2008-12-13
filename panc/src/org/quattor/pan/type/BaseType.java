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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/type/BaseType.java $
 $Id: BaseType.java 3506 2008-07-30 18:09:38Z loomis $
 */

package org.quattor.pan.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.NumberProperty;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.template.SourceRange;

/**
 * Common abstract class for all 'primitive' types in the pan language; that is,
 * types without default values or validation blocks.
 * 
 * @author loomis
 * 
 */
public abstract class BaseType extends Type {

	private static final long serialVersionUID = -1967404203740412112L;

	private static final Map<String, FullType> builtins;

	static {

		HashMap<String, FullType> types = new HashMap<String, FullType>();

		// Types based on concrete pan data classes.
		types.put("boolean", new FullType(new ConcretePrimitiveType("boolean",
				BooleanProperty.class)));
		types.put("double", new FullType(new ConcretePrimitiveType("double",
				DoubleProperty.class)));
		types.put("element", new FullType(new ConcretePrimitiveType("element",
				Element.class)));
		types.put("nlist", new FullType(new ConcretePrimitiveType("nlist",
				HashResource.class)));
		// Remove this unnecessary alias for the nlist type.
//		types.put("hash", new FullType(new ConcretePrimitiveType("hash",
//				HashResource.class)));
		types.put("list", new FullType(new ConcretePrimitiveType("list",
				ListResource.class)));
		types.put("long", new FullType(new ConcretePrimitiveType("long",
				LongProperty.class)));
		types.put("number", new FullType(new ConcretePrimitiveType("number",
				NumberProperty.class)));
		types.put("property", new FullType(new ConcretePrimitiveType(
				"property", Property.class)));
		types.put("resource", new FullType(new ConcretePrimitiveType(
				"resource", Resource.class)));
		types.put("string", new FullType(new ConcretePrimitiveType("string",
				StringProperty.class)));

		builtins = Collections.unmodifiableMap(types);
	}

	static public Map<String, FullType> getBuiltinTypes() {
		return builtins;
	}

	public BaseType(String source, SourceRange sourceRange) {
		super(source, sourceRange);
	}

}
