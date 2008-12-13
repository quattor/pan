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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/BooleanProperty.java $
 $Id: BooleanProperty.java 3595 2008-08-17 07:35:14Z loomis $
 */

package org.quattor.pan.dml.data;

import java.io.ObjectStreamException;

import net.jcip.annotations.Immutable;

/**
 * Represents a boolean value. This implementation creates two static instances
 * representing TRUE and FALSE. Calls to the getInstance method simply return a
 * pointer to one of the static instances.
 * 
 * @author loomis
 * 
 */
@Immutable
final public class BooleanProperty extends Property {

	private static final long serialVersionUID = -2316815635657551116L;

	public static final BooleanProperty TRUE = new BooleanProperty(true);

	public static final BooleanProperty FALSE = new BooleanProperty(false);

	private BooleanProperty(boolean value) {
		super(value ? Boolean.TRUE : Boolean.FALSE);
	}

	public static BooleanProperty getInstance(boolean value) {
		return value ? TRUE : FALSE;
	}

	public static BooleanProperty getInstance(Boolean value) {
		assert (value != null);
		return value.booleanValue() ? TRUE : FALSE;
	}

	public static BooleanProperty getInstance(String value) {
		assert (value != null);
		return Boolean.parseBoolean(value) ? TRUE : FALSE;
	}

	public Object readResolve() throws ObjectStreamException {
		return ((Boolean) this.getValue()).booleanValue() ? TRUE : FALSE;
	}

	@Override
	public Boolean getValue() {
		return (Boolean) super.getValue();
	}

	@Override
	public String getTypeAsString() {
		return "boolean";
	}

}
