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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/Resource.java $
 $Id: Resource.java 3597 2008-08-17 09:08:57Z loomis $
 */

package org.quattor.pan.dml.data;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_PATH_INDEX;
import static org.quattor.pan.utils.MessageUtils.MSG_REFERENCED_VARIABLE_NOT_LIST;

import java.util.Map;

import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.utils.Term;

/**
 * A Resource is a container of Properties and other Resources. All resources
 * descend from PersistentElement which makes them eligible for inclusion in a
 * final machine configuration.
 * 
 * @author loomis
 * 
 */
abstract public class Resource extends PersistentElement implements
		Iterable<Resource.Entry> {

	/**
	 * Retrieve the Element associated with the given Term (either an index or
	 * string key).
	 * 
	 * @param term
	 *            Term which acts as the key for the desired Element
	 * 
	 * @return Element associated with the given Term
	 * @throws InvalidTermException
	 *             thrown if the term is not of the appropriate type for the
	 *             resource; the message should contain the string
	 *             representation of the term
	 */
	abstract public Element get(Term term) throws InvalidTermException;

	/**
	 * Define the mapping between the given Term and given Element. The method
	 * returns the previous value or null if there was none.
	 * 
	 * @param term
	 *            Term which acts as the key for the desired Element
	 * @param newValue
	 *            Element to associate with the given Term
	 * @return previous value if it existed, null otherwise
	 * @throws InvalidTermException
	 *             thrown if the term is not of the appropriate type for the
	 *             resource; the message should contain the string
	 *             representation of the term
	 */
	abstract public Element put(Term term, Element newValue)
			throws InvalidTermException;

	/**
	 * Get the number of elements in this Resource.
	 * 
	 * @return number of elements in this Resource
	 */
	abstract public int size();

	@Override
	public Element rget(Term[] terms, int index, boolean protect,
			boolean lookupOnly) throws InvalidTermException {

		// Set the initial value to this resource. This value will be used if
		// there are no terms or if the index is already greater than that of
		// the last element.
		Element rvalue = this;
		int remaining = terms.length - index - 1;

		if (remaining >= 0) {

			// Always pull out the referenced child.
			try {
				rvalue = get(terms[index]);
			} catch (InvalidTermException ite) {
				throw ite.setInfo(terms, index, getTypeAsString());
			}

			// More work to do if this isn't the last child.
			if (rvalue != null && remaining > 0) {
				boolean pflag = protect || rvalue.isProtected();
				rvalue = rvalue.rget(terms, index + 1, pflag, lookupOnly);
			}
		}

		return rvalue;
	}

	@Override
	public void rput(Term[] terms, int index, Element value)
			throws InvalidTermException {

		int remaining = terms.length - index - 1;

		// This is a problem with the compiler. An index was given that
		// exceeds the number of terms in the path.
		if (remaining < 0) {
			throw CompilerError.create(MSG_INVALID_PATH_INDEX, index,
					terms.length);
		}

		Term term = terms[index];

		if (remaining == 0) {

			// This is the last term, so just replace the indicated child.
			try {
				put(term, value);
			} catch (InvalidTermException ite) {
				throw ite.setInfo(terms, index, getTypeAsString());
			}

		} else if (remaining > 0) {

			// More to do. Pull out the given child.
			Element child = null;
			try {
				child = get(term);
			} catch (InvalidTermException ite) {
				throw ite.setInfo(terms, index, getTypeAsString());
			}

			// If the child is a protected resource, then create a shallow copy.
			if (child != null && child.isProtected()) {
				child = child.writableCopy();
				put(term, child);
			}

			// If the child does not exist or is undef, then we need to create a
			// new Resource.
			if (child == null || child instanceof Undef) {
				if (terms[index + 1].isKey()) {
					child = new HashResource();
				} else {
					child = new ListResource();
				}
				put(term, child);
			}

			// If the child is protected, then we need to create an unprotected
			// copy of the child and replace the entry in this resource.
			if (child.isProtected()) {
				Element replacement = child.writableCopy();
				put(term, replacement);
				child = replacement;
			}

			// Now recursively descend to put the value in the correct place.
			child.rput(terms, index + 1, value);

		}

	}

	@Override
	public ListResource rgetList(Term[] terms, int index)
			throws InvalidTermException {

		ListResource result = null;

		int remaining = terms.length - index - 1;

		// This is a problem with the compiler. An index was given that
		// exceeds the number of terms in the path.
		if (remaining < 0) {
			throw CompilerError.create(MSG_INVALID_PATH_INDEX, index,
					terms.length);
		}

		Term term = terms[index];

		if (remaining == 0) {

			// Pull out the referenced value. If the result was not a list, then
			// throw an exception.
			try {
				Element element = get(term);
				if (element instanceof ListResource) {
					result = (ListResource) element;
				} else if (element instanceof Undef || element == null) {
					result = new ListResource();
					put(term, result);
				} else {
					throw EvaluationException.create(
							MSG_REFERENCED_VARIABLE_NOT_LIST, element
									.getTypeAsString());
				}
			} catch (InvalidTermException ite) {
				throw ite.setInfo(terms, index, getTypeAsString());
			}

		} else if (remaining > 0) {

			// More to do. Pull out the given child.
			Element child = null;
			try {
				child = get(term);
			} catch (InvalidTermException ite) {
				throw ite.setInfo(terms, index, getTypeAsString());
			}

			// If the child does not exist or is undef, then we need to create a
			// new Resource.
			if (child == null || child instanceof Undef) {
				if (terms[index + 1].isKey()) {
					child = new HashResource();
				} else {
					child = new ListResource();
				}
				put(term, child);
			}

			// If the child is protected, then we need to create an unprotected
			// copy of the child and replace the entry in this resource.
			assert (child != null);
			if (child.isProtected()) {
				child = child.writableCopy();
				put(term, child);
			}

			// Now recursively descend to put the value in the correct place.
			result = child.rgetList(terms, index + 1);

		}

		return result;
	}

	/**
	 * Get an iterator which allows to run over the resources in the Resource.
	 * Note that concurrent modification of the Resource is not permitted while
	 * actively using the iterator. The effects of doing so are undefined.
	 * 
	 * @return Iterator which will allow access to the key, value pairs of the
	 *         Resource
	 */
	abstract public Resource.Iterator iterator();

	/**
	 * Interface is an alias to make the syntax for the Resource.Entry less
	 * cumbersome. There are no additional methods required in this interface.
	 * 
	 * @author loomis
	 * 
	 */
	public static interface Entry extends Map.Entry<Property, Element> {
	}

	/**
	 * Interface is simply an alias to make the syntax for the Resource.Iterator
	 * less cumbersome. There are no additional methods required of the
	 * iterator.
	 * 
	 * @author loomis
	 * 
	 */
	public static interface Iterator extends java.util.Iterator<Resource.Entry> {
	}
}
