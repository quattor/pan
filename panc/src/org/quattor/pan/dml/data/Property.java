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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/Property.java $
 $Id: Property.java 3596 2008-08-17 08:35:06Z loomis $
 */

package org.quattor.pan.dml.data;

import java.io.IOException;

/**
 * Property represents a simple primitive type (boolean, long, or string) in the
 * pan language. All properties descend from PersistentElement which marks them
 * as being valid elements for a final machine configuration.
 * 
 * All Property instances must be immutable. This allows properties to be shared
 * between threads and between machine configurations consequently reducing
 * duplication and memory consumption.
 * 
 * The constructors of all Property classes should be protected or private. All
 * subclasses must implement a getInstance() method which returns an instance
 * (possibly cached) of the Property.
 * 
 * @author loomis
 * 
 */
abstract public class Property extends PersistentElement {

	private final Object value;

	private volatile int hashcode;

	/**
	 * This constructor should be used by subclasses to set the value of the
	 * property. The value should be a basic, immutable java type like Boolean,
	 * Long, or Double.
	 * 
	 * @param value
	 */
	protected Property(Object value) {
		assert (value != null);
		this.value = value;
		hashcode = value.hashCode();
	}

	/**
	 * Return the value of this property as an Object. Subclasses may override
	 * this method to provide a more specific return type. Subclasses will have
	 * to call this method to obtain the value because the value itself is
	 * private.
	 * 
	 * @return value of this property as an Object
	 */
	public Object getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && hashcode == o.hashCode()) {
			if (o instanceof Property) {
				return value.equals(((Property) o).value);
			}
		}
		return false;
	}

	/**
	 * This method is used to restore the volatile hashcode value when
	 * deserializing a Property object.
	 * 
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {

		in.defaultReadObject();
		hashcode = value.hashCode();
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
