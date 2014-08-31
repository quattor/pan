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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/type/Type.java $
 $Id: Type.java 3601 2008-08-18 14:16:29Z loomis $
 */

package org.quattor.pan.type;

import static org.quattor.pan.utils.MessageUtils.MSG_CANNOT_INCLUDE_TYPE;
import static org.quattor.pan.utils.MessageUtils.MSG_OPERATION_WITHOUT_CONTEXT;

import clojure.lang.AFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.template.TypeMap;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Common superclass for all pan language types defining the required methods.
 *
 * @author loomis
 *
 */
public abstract class Type extends AFn implements IObj {

    private final AtomicReference<IPersistentMap> metadataRef = new AtomicReference<IPersistentMap>();

	protected final String source;

	protected final String sourceRange;

	public Type(String source, SourceRange sourceRange) {

		// If the source isn't defined, give default value.
		if (source != null) {
			this.source = source;
		} else {
			this.source = "?";
		}

		// If the location isn't defined, give default value.
		if (sourceRange != null) {
			this.sourceRange = sourceRange.toString();
		} else {
			this.sourceRange = "?";
		}
	}

    @Override
    public IPersistentMap meta() {
        return metadataRef.get();
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        metadataRef.set(iPersistentMap);
        return this;
    }

    public Object invoke(Object o1, Object o2) {
        try {
            return validate((Context) o1, (Element) o2);
        } catch (ClassCastException ex) {
            throw CompilerError.create(MSG_OPERATION_WITHOUT_CONTEXT);
        }
    }

	/**
	 * This method verifies that all of the types referenced from this type are
	 * already defined within the given context. If not, an EvaluationException
	 * will be thrown.
	 *
	 * @param types
	 *            TypeMap to use for looking up referenced types
	 *
	 * @throws EvaluationException
	 */
	abstract public void verifySubtypesDefined(TypeMap types)
			throws EvaluationException;

	/**
	 * This method will run this type's validation against the given element.
	 * The method may not modify either argument.
	 *
	 * @param context
	 *            ObjectContext for running the validation
	 * @param self
	 *            Element to validate
	 *
	 * @throws ValidationException
	 */
	abstract public Object validate(final Context context, final Element self)
			throws ValidationException;

	/**
	 * This method will run this type's validation (as an included type) against
	 * the given element. This is separated from the usual validate() method
	 * because the validation may be different when the type is included rather
	 * than referenced directly. This is the case for the RecordType. The
	 * default implementation of this method simply throws a ValidationException
	 * indicating that it cannot be the target of the type include statement.
	 *
	 * @param context
	 *            ObjectContext for running the validation
	 * @param self
	 *            Element to validate
	 *
	 * @throws ValidationException
	 */
	public void validateAsIncludedType(Context context, Element self)
			throws ValidationException {
		throw ValidationException.create(MSG_CANNOT_INCLUDE_TYPE);
	}

	/**
	 * This method will recursively set the default values on the given element.
	 * Note that self cannot be null. If the current element is null, then use
	 * the findDefault() method to retrieve the default value, set it, and then
	 * call this method to descend into referenced type definitions.
	 *
	 * This method will return a replacement element if necessary. This may be
	 * the case self was protected and default values had to be added. This
	 * method will return null if no replacement was necessary. It is the
	 * caller's responsibility to make the appropriate update to the parent
	 * element.
	 *
	 * @param context
	 * @param self
	 * @return replacement element or null if no replacement is necessary
	 * @throws EvaluationException
	 */
	abstract public Element setDefaults(Context context, Element self)
			throws EvaluationException;

	/**
	 * This method returns the default value for the type or null if no default
	 * value exists. While only FullType objects can directly have a default
	 * value, these can be referenced by other types like AliasType and
	 * RecordType.
	 *
	 * @param context
	 *            the context may be needed by a complex type to look up other
	 *            type definitions
	 *
	 * @return default value or null if one doesn't exist
	 */
	abstract public Element findDefault(Context context);

	/**
	 * Return a string representation of the source containing this type. If the
	 * source wasn't set at creation this returns the string "?".
	 *
	 * @return String source containing this type
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Retrieve the SourceRange for this type definition. If the location was
	 * not defined at creation this returns the string "?".
	 *
	 * @return String location of this type definition
	 */
	public String getSourceRange() {
		return sourceRange;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
