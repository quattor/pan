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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/ListResource.java $
 $Id: ListResource.java 4005 2008-12-01 16:45:04Z loomis $
 */

package org.quattor.pan.dml.data;

import static org.quattor.pan.utils.MessageUtils.MSG_CONCURRENT_MODIFICATION;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_LIST_INDEX;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_REPLACEMENT;
import static org.quattor.pan.utils.MessageUtils.MSG_LIST_SIZE_OUTSIDE_RANGE;
import static org.quattor.pan.utils.MessageUtils.MSG_NONEXISTANT_LIST_ELEMENT;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.Range;
import org.quattor.pan.utils.Term;

/**
 * Represents an ordered list of elements.
 * 
 * @author loomis
 * 
 */
public class ListResource extends Resource {

	private static final long serialVersionUID = 9153878005017517633L;

	private ArrayList<Element> list;

	public ListResource() {
		list = new ArrayList<Element>();
	}

	public ListResource(Element[] args) {

		list = new ArrayList<Element>();
		for (Element arg : args) {
			list.add(arg);
		}
	}

	private ListResource(List<Element> childrenList) {

		// Cloning of a hash should only take place within a single thread or
		// with something from another machine configuration tree that is
		// frozen. Consequently, this cloning shouldn't need to be synchronized.
		try {
			list = new ArrayList<Element>(childrenList.size());
			for (Element element : childrenList) {
				Element clone = element.duplicate();
				list.add(clone);
			}
		} catch (StackOverflowError e) {
			throw new EvaluationException(
					"stack overflow; check for circular data structure");
		}
	}

	/**
	 * This method creates a shallow copy of the given ListResource. The
	 * children are not cloned, but they are protected.
	 * 
	 * @param source
	 *            ListResource to copy
	 */
	protected ListResource(ListResource source) {
		list = new ArrayList<Element>(source.list.size());
		for (Element e : source.list) {
			list.add(e.protect());
		}
		list.trimToSize();
	}

	@Override
	public Element duplicate() {
		return new ListResource(list);
	}

	@Override
	public Element get(Term term) throws InvalidTermException {
		assert (term != null);

		Element value = null;

		int index = 0;
		try {
			index = term.getIndex().intValue();
			value = list.get(index);
		} catch (IndexOutOfBoundsException ioobe) {
			// OK, just return a null value.
		}
		return value;
	}

	@Override
	public Element put(Term term, Element value) throws InvalidTermException {

		assert (term != null);

		return put(term.getIndex().intValue(), value);
	}

	/**
	 * This is an optimized version of the put method which doesn't require
	 * creating a Term object.
	 * 
	 * @param index
	 *            index of the element to insert
	 * @param newValue
	 *            value to insert into the list
	 * 
	 * @return old value if it existed
	 */
	public Element put(int index, Element newValue) {

		Element oldValue = null;

		if (index < 0) {
			throw new EvaluationException(MessageUtils.format(
					MSG_INVALID_LIST_INDEX, Integer.valueOf(index).toString()));
		}

		try {
			if ((newValue != null) && !(newValue instanceof Null)) {
				int size = list.size();
				if (index >= size) {
					for (int i = 0; i < index - size; i++) {
						list.add(Undef.VALUE);
					}
					list.add(newValue);
				} else {
					oldValue = list.set(index, newValue);
					if (oldValue != null) {
						oldValue.checkValidReplacement(newValue);
					}
				}
			} else {
				try {
					oldValue = list.remove(index);
				} catch (IndexOutOfBoundsException ioobe) {
					// Ignore this error; removing non-existant element is OK.
				}
			}
		} catch (IndexOutOfBoundsException ioobe) {
			throw new EvaluationException(MessageUtils.format(
					MSG_NONEXISTANT_LIST_ELEMENT, Integer.valueOf(index)
							.toString()));
		}
		return oldValue;
	}

	/**
	 * Specialized method for a ListResource to append an element to the end of
	 * the list. This is used in the append() function implementation.
	 * 
	 * @param e
	 *            element to append to the end of the list; this may not be null
	 */
	public void append(Element e) {
		assert (e != null);
		list.add(e);
	}

	/**
	 * Specialized method for a ListResource to prepend an element at the
	 * beginning of a list. This is used in the prepend() function
	 * implementation.
	 * 
	 * @param e
	 *            element to prepend to beginning of list; this may not be null
	 */
	public void prepend(Element e) {
		assert (e != null);
		list.add(0, e);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public Resource.Iterator iterator() {
		return new ListResourceIterator(list);
	}

	@Override
	public String locateUndefinedElement() {

		// Loop over all of my children to see if there is an undefined element.
		int nchild = list.size();
		for (int i = 0; i < nchild; i++) {
			String rpath = list.get(i).locateUndefinedElement();
			if (rpath != null) {
				return (!"".equals(rpath)) ? i + "/" + rpath : Integer
						.toString(i);
			}
		}

		// Nothing found; return null to indicate this.
		return null;
	}

	@Override
	public void checkRange(Range range) throws ValidationException {
		if (!range.isInRange(list.size())) {
			throw ValidationException.create(MSG_LIST_SIZE_OUTSIDE_RANGE, list
					.size(), range.toString());
		}
	}

	@Override
	public void checkValidReplacement(Element newValue)
			throws EvaluationException {

		// Undef, null, or lists can replace these types of resources.
		if (!(newValue instanceof Undef) && !(newValue instanceof Null)
				&& !(newValue instanceof ListResource)) {

			throw new EvaluationException(MessageUtils.format(
					MSG_INVALID_REPLACEMENT, this.getTypeAsString(), newValue
							.getTypeAsString()));

		}

	}

	@Override
	public Element protect() {
		return new ProtectedListResource(this);
	}

	@Override
	public String getTypeAsString() {
		return "list";
	}

	@Override
	public int hashCode() {
		return list.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return list.equals(o);
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append("[ ");

		String separator = "";
		for (Resource.Entry entry : this) {
			sb.append(separator);
			sb.append(entry.getValue().toString());
			separator = ", ";
		}

		sb.append(" ]");

		return sb.toString();
	}

	private static class ListResourceIterator implements Resource.Iterator {

		private volatile int index;

		private final List<Element> backingList;

		public ListResourceIterator(List<Element> backingList) {
			assert (backingList != null);
			this.backingList = backingList;
			index = 0;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"ListResourceIterator does not support remove()");
		}

		public boolean hasNext() {
			return (index < backingList.size());
		}

		public Resource.Entry next() {
			Resource.Entry entry = null;
			try {
				entry = new ListResourceEntry(LongProperty.getInstance(index),
						backingList.get(index));
				index++;
			} catch (NoSuchElementException nsee) {
				throw new EvaluationException(MessageUtils
						.format(MSG_CONCURRENT_MODIFICATION), null);
			}
			return entry;
		}

	}

	private static class ListResourceEntry implements Resource.Entry {

		private final Property key;

		private final Element value;

		public ListResourceEntry(Property key, Element value) {
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
					"ListResourceEntry does not support setValue()");
		}

		@Override
		public int hashCode() {
			return key.hashCode() ^ value.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof ListResourceEntry) {
				ListResourceEntry other = (ListResourceEntry) o;
				return this.key.equals(other.key)
						&& this.value.equals(other.value);
			} else {
				return false;
			}
		}

	}

}
