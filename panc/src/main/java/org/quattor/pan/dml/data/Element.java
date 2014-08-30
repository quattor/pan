/*
 Copyright (c) 2014 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.quattor.pan.dml.data;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.template.Context;
import org.quattor.pan.utils.Range;
import org.quattor.pan.utils.Term;

/**
 * Represents the most general data element in the Data Manipulation Language (DML). This class adds some utility
 * methods which are useful for implementing some of the built-in functions.
 *
 * @author loomis
 */
public interface Element extends Operation {

    /**
     * Determine if the element satisfies the given range constraint. This is used in the validation of the element. By
     * default, this method with throw a ValidationException indicating that range checking of this element is not
     * appropriate.
     *
     * @param range Range to check Element against
     * @throws ValidationException if the element cannot be compared to a range or if the element doesn't meet the range
     *                             requirement
     */
    public void checkRange(Range range) throws ValidationException;

    /**
     * All Elements are Operations and can be executed, but each just returns a reference to itself. Subclasses should
     * not need to override this method.
     *
     * @return reference to the same Element
     */
    public Element execute(Context context) throws EvaluationException;

    /**
     * This method does a "clone" of the given Element. Immutable Elements may return a reference to the same object.
     * Mutable Elements (i.e. Resources) must provide an actual deep-copy of the Element. This method simply returns a
     * reference to the same object and is suitable only for immutable subclasses. Mutable subclasses (i.e. Resources)
     * must override this method to provide an appropriate deep copy.
     *
     * This is not actually named "clone" because it violates the conditions of that method for creating a duplicate of
     * the instance in all cases.
     *
     * @return deep-copy of Element for mutable elements, self-reference for immutable objects
     */
    public Element duplicate();

    /**
     * This method returns a writable copy of this Element. The default implementation just returns a reference to the
     * same Element. This is the correct behavior for any object that returns false for the isProtected method. Objects
     * that return true for isProtected, must override this method.
     *
     * @return returns a writable copy of this element; if the current element is not protected, then a reference to
     * this object is returned
     */
    public Element writableCopy();

    /**
     * This method indicates if the given Element is protected. A protected element may not be written to and concerns
     * just resources. The default implementation just returns false. This should be sufficient except for protected
     * resources.
     */
    public boolean isProtected();

    /**
     * Check that the newValue is a valid replacement for the this value. This implementation will check if the newValue
     * is assignable from the current value or that the newValue is either undef or null. If not, an evaluation
     * exception will be thrown. This implementation should be overridden if more liberal replacements are allowed.
     *
     * @param newValue the new value for the replacement
     * @throws EvaluationException if the new value is not a valid replacement of the existing value
     */
    public void checkValidReplacement(Element newValue) throws EvaluationException;

    /**
     * Return a protected version of this Element. The default implementation just returns to "this". Only unprotected
     * resources will have to override this implementation.
     *
     * @return protected version of the resource
     */
    public Element protect();

    /**
     * Dereference the Element to return the value of a child. Any resource should return the value of the given child.
     * The default implementation of this method will throw an EvaluationException indicating that this Element cannot
     * be dereferenced.
     *
     * @param terms      list of terms to use for dereference
     * @param index      the term to use in the given list of term
     * @param protect    flag to indicate that the return value should be a protected (if value is a resource)
     * @param lookupOnly indicates that only a lookup is required, return null if the element doesn't exist
     * @throws InvalidTermException thrown if an trying to dereference a list with a key or a hash with an index
     */
    public Element rget(Term[] terms, int index, boolean protect, boolean lookupOnly) throws InvalidTermException;

    /**
     * This is a special lookup function that will retrieve a list from the resource. If the resource does not exist, an
     * empty list will be created. All necessary parent resources are created. The returned list is guaranteed to be a
     * writable resource.
     *
     * @param terms list of terms to use for dereference
     * @param index the term to use in the given list of term
     * @return writable list
     */
    public ListResource rgetList(Term[] terms, int index) throws InvalidTermException;

    /**
     * Add the given child to this resource, creating intermediate resources as necessary. If this Element is not a
     * resource, then this will throw an InvalidTermException. The default implementation of this method throws such an
     * exception. This resource must be a writable resource, otherwise an exception will be thrown.
     *
     * @throws InvalidTermException thrown if an trying to dereference a list with a key or a hash with an index
     */
    public void rput(Term[] terms, int index, Element value) throws InvalidTermException;

    /**
     * All Elements must return a string describing its type. This value used in the serialization to machine profiles.
     *
     * @return String representation of Element's type
     */
    public String getTypeAsString();

    /**
     * Require that all Elements implement a reasonable toString method.
     */
    public String toString();

    /**
     * Require that all Elements implement the hashCode method.
     */
    public int hashCode();

    /**
     * Require that all Elements implement an equals method.
     */
    public boolean equals(Object o);

}
