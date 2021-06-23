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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/type/ConcretePrimitiveType.java $
 $Id: ConcretePrimitiveType.java 3585 2008-08-16 15:53:11Z loomis $
 */

package org.quattor.pan.type;

import static org.quattor.pan.utils.MessageUtils.MSG_MISMATCHED_TYPES;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.ttemplate.Context;

/**
 * Concrete implementations of primitive types like booleans, longs, etc. The
 * functionality for these types is identical and implemented in this class.
 *
 * @author loomis
 *
 */
public class ConcretePrimitiveType extends PrimitiveType {

	private final String name;

	private final Class<? extends Element> elementType;

	public ConcretePrimitiveType(String name,
			Class<? extends Element> elementType) {

		assert (name != null);
		assert (elementType != null);

		this.name = name;
		this.elementType = elementType;
	}

	@Override
	public Object validate(final Context context, final Element self)
			throws ValidationException {

		try {

			// To verify that the given element is of the correct type, force a
			// cast to this type. An exception will be thrown if it isn't the
			// correct type.
			elementType.cast(self);

		} catch (ClassCastException cce) {

            throw ValidationException.createv(self, MSG_MISMATCHED_TYPES,
                                              name, self.getTypeAsString());

		}
        return null;
	}
}
