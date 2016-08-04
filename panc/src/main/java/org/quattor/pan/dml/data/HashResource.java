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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/HashResource.java $
 $Id: HashResource.java 4005 2008-12-01 16:45:04Z loomis $
 */

package org.quattor.pan.dml.data;

import static org.quattor.pan.utils.MessageUtils.MSG_CONCURRENT_MODIFICATION;
import static org.quattor.pan.utils.MessageUtils.MSG_HASH_SIZE_OUTSIDE_RANGE;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_REPLACEMENT;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.Range;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

/**
 * Represents an dict (nlist or hash) that associates a string value (key) to
 * another element. The key must be a string the is a valid term in a pan path.
 *
 * @author loomis
 *
 */
public class HashResource extends Resource {

	private Map<String, Element> map;

	public HashResource() {
		map = new TreeMap<String, Element>();
	}

	private HashResource(Map<String, Element> childrenMap) {

		// Cloning of a hash should only take place within a single thread or
		// with something from another machine configuration tree that is
		// frozen. Consequently, this cloning shouldn't need to be synchronized.
		try {
			map = new TreeMap<String, Element>();
			for (Map.Entry<String, Element> entry : childrenMap.entrySet()) {
				map.put(entry.getKey(), entry.getValue().duplicate());
			}
		} catch (StackOverflowError e) {
			throw new EvaluationException(
					"stack overflow; check for circular data structure");
		}
	}

	/**
	 * This constructor creates a shallow copy of the given HashResource. The
	 * children of the referenced HashResource are not cloned, but they are
	 * protected.
	 *
	 * @param source
	 *            HashResource to copy
	 */
	protected HashResource(HashResource source) {
		map = new TreeMap<String, Element>();
		for (Map.Entry<String, Element> entry : source.map.entrySet()) {
			map.put(entry.getKey(), entry.getValue().protect());
		}
	}

	@Override
	public Element duplicate() {
		return new HashResource(map);
	}

	@Override
	public Element get(Term term) throws InvalidTermException {
		return map.get(term.getKey());
	}

	@Override
	public Element put(Term term, Element newValue) throws InvalidTermException {

		Element oldValue = null;

		if ((newValue != null) && !(newValue instanceof Null)) {
			oldValue = map.put(term.getKey(), newValue);
			if (oldValue != null) {
				oldValue.checkValidReplacement(newValue);
			}
		} else {
			oldValue = map.remove(term.getKey());
		}
		return oldValue;
	}

	@Override
	public int size() {
		return map.size();
	}

	public Set<Term> keySet() {
		TreeSet<Term> terms = new TreeSet<Term>();
		for (String s : map.keySet()) {
			Term term = TermFactory.create(s);
			terms.add(term);
		}
		return terms;
	}

	@Override
	public void checkRange(Range range) throws ValidationException {
		if (!range.isInRange(map.size())) {
			throw ValidationException.create(MSG_HASH_SIZE_OUTSIDE_RANGE,
					map.size(), range.toString());
		}
	}

	@Override
	public void checkValidReplacement(Element newValue)
			throws EvaluationException {

		// Undef, null, or hashes can replace these types of resources.
		if (!(newValue instanceof Undef) && !(newValue instanceof Null)
				&& !(newValue instanceof HashResource)) {

			throw new EvaluationException(MessageUtils.format(
					MSG_INVALID_REPLACEMENT, this.getTypeAsString(),
					newValue.getTypeAsString()));

		}

	}

	@Override
	public Element protect() {
		return new ProtectedHashResource(this);
	}

	@Override
	public String getTypeAsString() {
		// This must remain as "nlist" until we decide to change the
		// name of the resource in downstream clients.
		return "nlist";
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof HashResource) {
			return map.equals(((HashResource) o).getBackingMap());
		} else {
			return false;
		}
	}

	/**
	 * This method is used to access the underlying map used to store the hash
	 * information. This method is used in the equals method to determine if one
	 * HashResource is equivalent to another. The map must not be modified by
	 * the caller.
	 *
	 * @return backing map for hash resource
	 */
	protected Map<String, Element> getBackingMap() {
		return map;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append("{ ");

		String separator = "";
		for (Resource.Entry entry : this) {
			sb.append(separator);
			sb.append(entry.getKey().toString());
			sb.append(", ");
			sb.append(entry.getValue().toString());
			separator = ", ";
		}

		sb.append(" }");

		return sb.toString();
	}

	@Override
	public Resource.Iterator iterator() {
		return new HashResourceIterator(map, false);
	}

	public Resource.Iterator protectedIterator() {
		return new HashResourceIterator(map, true);
	}

	private static class HashResourceIterator implements Resource.Iterator {

		private final java.util.Iterator<String> iterator;

		private final Map<String, Element> backingHash;

		private final boolean isProtected;

		public HashResourceIterator(Map<String, Element> backingHash, boolean isProtected) {
			assert (backingHash != null);
			this.backingHash = backingHash;
			this.isProtected = isProtected;
			iterator = backingHash.keySet().iterator();
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"HashResourceIterator does not support remove()");
		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public Resource.Entry next() {
			Resource.Entry entry = null;
			try {
				StringProperty key = StringProperty
						.getInstance(iterator.next());
				Element value = backingHash.get(key.getValue());
				if (value == null) {
					throw new EvaluationException(
							MessageUtils.format(MSG_CONCURRENT_MODIFICATION),
							null);
				}
				if (isProtected) {
					value = value.protect();
				}
				entry = new HashResourceEntry(key, value);
			} catch (NoSuchElementException nsee) {
				throw new EvaluationException(
						MessageUtils.format(MSG_CONCURRENT_MODIFICATION), null);
			}
			return entry;
		}

	}

	private static class HashResourceEntry implements Resource.Entry {

		private final Property key;

		private final Element value;

		public HashResourceEntry(Property key, Element value) {
			assert (key != null);
			assert (value != null);
			this.key = key;
			this.value = value;
		}

		public Property getKey() {
			return key;
		}

		public Element getValue() {
			return value;
		}

		public Element setValue(Element value) {
			throw new UnsupportedOperationException(
					"HashResourceEntry does not support setValue()");
		}

		@Override
		public int hashCode() {
			return key.hashCode() ^ value.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof HashResourceEntry) {
				HashResourceEntry other = (HashResourceEntry) o;
				return this.key.equals(other.key)
						&& this.value.equals(other.value);
			} else {
				return false;
			}
		}

	}

}
