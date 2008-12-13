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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/Element.java $
 $Id: Element.java 3595 2008-08-17 07:35:14Z loomis $
 */

package org.quattor.pan.dml.data;

import static org.quattor.pan.utils.MessageUtils.MSG_CANNOT_ADD_CHILD;
import static org.quattor.pan.utils.MessageUtils.MSG_ILLEGAL_DEREFERENCE;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_RANGE_CHECK;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_REPLACEMENT;

import java.util.List;

import net.jcip.annotations.Immutable;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.template.Context;
import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.Range;
import org.quattor.pan.utils.Term;

/**
 * Represents the most general data element in the Data Manipulation Language
 * (DML). This class adds some utility methods which are useful for implementing
 * some of the built-in functions.
 * 
 * @author loomis
 * 
 */
@Immutable
abstract public class Element implements Operation {

	private static final long serialVersionUID = -9192717034633002369L;

	/**
	 * Returns true for all Element objects except Null.
	 * 
	 * @return true unless object is Null
	 */
	public boolean exists() {
		return !(this instanceof Null);
	}

	/**
	 * Determine if the given Element is defined. This will return true for all
	 * Elements except Null and Undef.
	 */
	public boolean defined() {
		return exists() && !(this instanceof Undef);
	}

	/**
	 * Determine if the given Element is a Property.
	 */
	public boolean isProperty() {
		return (this instanceof Property);
	}

	/**
	 * Determine if the given Element is a Long.
	 */
	public boolean isLong() {
		return (this instanceof LongProperty);
	}

	/**
	 * Determine if the given Element is a Double.
	 */
	public boolean isDouble() {
		return (this instanceof DoubleProperty);
	}

	/**
	 * Determine if the given Element is a Boolean.
	 */
	public boolean isBoolean() {
		return (this instanceof BooleanProperty);
	}

	/**
	 * Determine if the given Element is a String.
	 */
	public boolean isString() {
		return (this instanceof StringProperty);
	}

	/**
	 * Determine if the given Element is a Resource.
	 */
	public boolean isResource() {
		return (this instanceof Resource);
	}

	/**
	 * Determine if the given Element is an Nlist.
	 */
	public boolean isNlist() {
		return (this instanceof HashResource);
	}

	/**
	 * Determine if the given Element is a List.
	 */
	public boolean isList() {
		return (this instanceof ListResource);
	}

	/**
	 * Determine if the given Element is Persistent. Persistent Elements are
	 * valid entries in a final configuration tree.
	 */
	public boolean isPersistent() {
		return (this instanceof PersistentElement);
	}

	/**
	 * Determine if the given Element is Transient. Transient Elements are only
	 * valid while building the tree. They may not appear in the final
	 * configuration tree.
	 */
	public boolean isTransient() {
		return (this instanceof TransientElement);
	}

	/**
	 * Determine if the element contains any undefined (transient) elements. The
	 * call will return null if no undefined elements are found; it will return
	 * a string indicating the relative path if an undefined element is found.
	 * 
	 * Subclasses MUST override this method appropriately. This default method
	 * will work only for elements which are not containers and not a transient
	 * element.
	 * 
	 * @return String representation of the path of the undefined element, null
	 *         otherwise
	 */
	public String locateUndefinedElement() {
		return null;
	}

	/**
	 * Determine if the element satisfies the given range constraint. This is
	 * used in the validation of the element. By default, this method with throw
	 * a ValidationException indicating that range checking of this element is
	 * not appropriate.
	 * 
	 * @param range
	 *            Range to check Element against
	 * 
	 * @throws ValidationException
	 *             if the element cannot be compared to a range or if the
	 *             element doesn't meet the range requirement
	 */
	public void checkRange(Range range) throws ValidationException {
		throw ValidationException.create(MSG_INVALID_RANGE_CHECK,
				getTypeAsString());
	}

	/**
	 * All Elements are Operations and can be executed, but each just returns a
	 * reference to itself. Subclasses should not need to override this method.
	 * 
	 * @return reference to the same Element
	 */
	public Element execute(Context context) throws EvaluationException {
		return this;
	}

	/**
	 * This method does a "clone" of the given Element. Immutable Elements may
	 * return a reference to the same object. Mutable Elements (i.e. Resources)
	 * must provide an actual deep-copy of the Element. This method simply
	 * returns a reference to the same object and is suitable only for immutable
	 * subclasses. Mutable subclasses (i.e. Resources) must override this method
	 * to provide an appropriate deep copy.
	 * 
	 * This is not actually named "clone" because it violates the conditions of
	 * that method for creating a duplicate of the instance in all cases.
	 * 
	 * @return deep-copy of Element for mutable elements, self-reference for
	 *         immutable objects
	 */
	public Element duplicate() {
		return this;
	}

	/**
	 * This method returns a writable copy of this Element. The default
	 * implementation just returns a reference to the same Element. This is the
	 * correct behavior for any object that returns false for the isProtected
	 * method. Objects that return true for isProtected, must override this
	 * method.
	 * 
	 * @return returns a writable copy of this element; if the current element
	 *         is not protected, then a reference to this object is returned
	 */
	public Element writableCopy() {
		return this;
	}

	/**
	 * The default implementation does nothing. This should not need to be
	 * overridden as all data elements can appear anywhere within a DML block.
	 * (Although placement may cause other errors during evaluation.)
	 */
	public void checkRestrictedContext() throws SyntaxException {
	}

	/**
	 * This method indicates if the given Element is protected. A protected
	 * element may not be written to and concerns just resources. The default
	 * implementation just returns false. This should be sufficient except for
	 * protected resources.
	 */
	public boolean isProtected() {
		return false;
	}

	/**
	 * Check that the newValue is a valid replacement for the this value. This
	 * implementation will check if the newValue is assignable from the current
	 * value or that the newValue is either undef or null. If not, an evaluation
	 * exception will be thrown. This implementation should be overridden if
	 * more liberal replacements are allowed.
	 * 
	 * @param newValue
	 *            the new value for the replacement
	 * 
	 * @throws EvaluationException
	 *             if the new value is not a valid replacement of the existing
	 *             value
	 */
	public void checkValidReplacement(Element newValue)
			throws EvaluationException {

		// Only need to check if new values is not undef or null. Undef or null
		// can replace any value.
		if (!(newValue instanceof Undef) && !(newValue instanceof Null)) {

			if (!this.getClass().isAssignableFrom(newValue.getClass())) {
				throw new EvaluationException(MessageUtils.format(
						MSG_INVALID_REPLACEMENT, this.getTypeAsString(),
						newValue.getTypeAsString()));
			}

		}

	}

	/**
	 * Return a protected version of this Element. The default implementation
	 * just returns to "this". Only unprotected resources will have to override
	 * this implementation.
	 * 
	 * @return protected version of the resource
	 */
	public Element protect() {
		return this;
	}

	/**
	 * Dereference the Element to return the value of a child. Any resource
	 * should return the value of the given child. The default implementation of
	 * this method will throw an EvaluationException indicating that this
	 * Element cannot be dereferenced.
	 * 
	 * @param terms
	 *            list of terms to use for dereference
	 * @param index
	 *            the term to use in the given list of term
	 * @param protect
	 *            flag to indicate that the return value should be a protected
	 *            (if value is a resource)
	 * @param lookupOnly
	 *            indicates that only a lookup is required, return null if the
	 *            element doesn't exist
	 * @throws InvalidTermException
	 *             thrown if an trying to dereference a list with a key or a
	 *             hash with an index
	 */
	public Element rget(List<Term> terms, int index, boolean protect,
			boolean lookupOnly) throws InvalidTermException {
		if (!lookupOnly) {
			throw new EvaluationException(MessageUtils.format(
					MSG_ILLEGAL_DEREFERENCE, this.getTypeAsString()));
		}
		return null;
	}

	/**
	 * Add the given child to this resource, creating intermediate resources as
	 * necessary. If this Element is not a resource, then this will throw an
	 * EvaluationException. The default implementation of this method throws
	 * such an exception.
	 * 
	 * @throws InvalidTermException
	 *             thrown if an trying to dereference a list with a key or a
	 *             hash with an index
	 */
	public void rput(List<Term> terms, int index, Element value)
			throws InvalidTermException {
		throw new EvaluationException(MessageUtils.format(MSG_CANNOT_ADD_CHILD,
				this.getTypeAsString()));
	}

	/**
	 * All Elements must return a string describing its type. This value used in
	 * the serialization to machine profiles.
	 * 
	 * @return String representation of Element's type
	 */
	abstract public String getTypeAsString();

	/**
	 * Require that all Elements implement a reasonable toString method.
	 */
	@Override
	abstract public String toString();

	/**
	 * Require that all Elements implement the hashCode method.
	 */
	@Override
	abstract public int hashCode();

	/**
	 * Require that all Elements implement an equals method.
	 */
	@Override
	abstract public boolean equals(Object o);

}
