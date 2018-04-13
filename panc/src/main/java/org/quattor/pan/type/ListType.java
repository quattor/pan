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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/type/ListType.java $
 $Id: ListType.java 3585 2008-08-16 15:53:11Z loomis $
 */

package org.quattor.pan.type;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_KEY_OR_INDEX;
import static org.quattor.pan.utils.MessageUtils.MSG_MISMATCHED_TYPES;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.utils.Range;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

/**
 * Implements a list type for the pan language.
 *
 * @author loomis
 *
 */
public class ListType extends CompositeType {

	/**
	 * Constructor for LinkType takes another BaseType as the type of the list.
	 * The method does not check for circular references. Be careful not to
	 * create them.
	 *
	 * @param source
	 *            source where this type is defined
	 * @param sourceRange
	 *            location in source where type is defined
	 * @param baseType
	 *            base type of the list
	 * @param range
	 *            range associated with this type
	 */
	public ListType(String source, SourceRange sourceRange, BaseType baseType,
			Range range) {
		super(source, sourceRange, baseType, range);
	}

	@Override
	public Element findDefault(Context context) {

		// The base type for a list refers to the children's type, not the
		// list's type. Thus it can never have a default value for the type
		// which references it. Always return null.
		return null;
	}

	@Override
	public Element setDefaults(Context context, Element self)
			throws EvaluationException {

		ListResource replacement = null;

		try {

			ListResource list = (ListResource) self;

			// Loop over all of the children and apply the base type validation.
			for (int i = 0; i < list.size(); i++) {
				Term t = TermFactory.create(i);
				Element child = list.get(t);

                // See also similar code in RecordType
                // If the child has an 'undef' value, then replace it
                // with the given default.
                // Make sure we run setDefaults on the defaults
                if (child instanceof Undef) {
                    Element defaultValue = baseType.findDefault(context);
                    if (defaultValue != null) {
                        // to run setDefaults on the default value
                        child = defaultValue;
                        if (replacement == null) {
                            replacement = (ListResource) list.writableCopy();
                        }
                        // add it here, in case setDefaults() on defaultValue
                        // requires no changes and returns newValue=null below
                        replacement.put(t, defaultValue);
                    }
                }

				Element newValue = baseType.setDefaults(context, child);

				// If there is a replacement value, then add it to the current
				// dict. Make sure that the current dict is not a protected
				// resource.
				if (newValue != null) {
					if (replacement == null) {
						replacement = (ListResource) list.writableCopy();
					}
					replacement.put(t, newValue);
				}
			}

		} catch (ClassCastException cce) {
			// Ignore the exception. This will be dealt with during the
			// validation stage.
		} catch (InvalidTermException ite) {
			// This exception should never be encountered because the terms are
			// extracted directly from the existing list.
			throw CompilerError.create(MSG_INVALID_KEY_OR_INDEX);
		}

		return replacement;
	}

	@Override
	public Object validate(final Context context, final Element self)
			throws ValidationException {

		try {

			ListResource list = (ListResource) self;

			if (range != null) {
				list.checkRange(range);
			}

			// Loop over all of the children and apply the base type validation.
			for (int i = 0; i < list.size(); i++) {
				Term t = TermFactory.create(i);
				try {
					Element child = list.get(t);
					baseType.validate(context, child);
				} catch (ValidationException ve) {
					throw ve.addTerm(t);
				} catch (InvalidTermException ite) {
					// This exception should never be encountered because the
					// terms are
					// extracted directly from the existing list.
					throw CompilerError.create(MSG_INVALID_KEY_OR_INDEX);
				}
			}

		} catch (ClassCastException cce) {
			throw ValidationException.create(MSG_MISMATCHED_TYPES, "list", self
					.getTypeAsString());
		}
        return null;
	}

	@Override
	public String toString() {
		if (range == null) {
			return baseType.toString() + "[]";
		} else {
			return baseType.toString() + "[" + range + "]";
		}
	}

}
