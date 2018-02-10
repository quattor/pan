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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/type/PrimitiveType.java $
 $Id: PrimitiveType.java 3601 2008-08-18 14:16:29Z loomis $
 */

package org.quattor.pan.type;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.ttemplate.TypeMap;

/**
 * Superclass of all pan language primitive types; these include all simple,
 * atomic data type like booleans, longs, etc.
 * 
 * @author loomis
 * 
 */
public abstract class PrimitiveType extends BaseType {

	public PrimitiveType() {
		super((String) null, (SourceRange) null);
	}

	@Override
	// A primitive type never references another type and has no default value.
	// Always return null from this method.
	public Element findDefault(Context context) {
		return null;
	}

	@Override
	// A primitive type never references another type and has no default value.
	// This method is a no-op.
	public Element setDefaults(Context context, Element self)
			throws EvaluationException {
		assert (context != null);
		assert (self != null);
		return null;
	}

	@Override
	// Primitive types never reference another type, so do nothing.
	public void verifySubtypesDefined(TypeMap types) {
		assert (types != null);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
