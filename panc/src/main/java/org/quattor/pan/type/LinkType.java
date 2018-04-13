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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/type/LinkType.java $
 $Id: LinkType.java 3613 2008-08-20 14:57:07Z loomis $
 */

package org.quattor.pan.type;

import static org.quattor.pan.utils.MessageUtils.MSG_CONFLICTING_TYPES;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_LINK_PATH;
import static org.quattor.pan.utils.MessageUtils.MSG_LINK_ELEMENT_FAILED_VALIDATION;
import static org.quattor.pan.utils.MessageUtils.MSG_NONEXISTANT_LINK_ELEMENT;
import static org.quattor.pan.utils.MessageUtils.MSG_PATH_EVAL_ERROR;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.utils.Path;

/**
 * Implements the link type for the pan language.
 *
 * @author loomis
 *
 */
public class LinkType extends CompositeType {

	/**
	 * Constructor for LinkType takes another BaseType as the type of the link.
	 * The method does not check for circular references. Be careful not to
	 * create them.
	 *
	 * @param source
	 *            source where this type is defined
	 * @param sourceRange
	 *            location within the source where the type is defined
	 * @param baseType
	 *            base type for this link
	 */
	public LinkType(String source, SourceRange sourceRange, BaseType baseType) {
		super(source, sourceRange, baseType, null);
	}

	@Override
	public Element findDefault(Context context) {

		// The base type refers to the element pointed to by the link. It thus
		// can never have a default value for the type referencing this one.
		// Always return null.
		return null;
	}

	@Override
	public Element setDefaults(Context context, Element self)
			throws EvaluationException {

		try {

			StringProperty link = (StringProperty) self;

			try {

				// Ensure that the value is a valid path.
				Path path = new Path(link.getValue());

				// Follow the link and apply the base type to the referenced
				// element.
				Element linkedElement = context.getElement(path);
				Element linkedReplacement = null;
				if (linkedElement != null) {
					linkedReplacement = baseType.setDefaults(context,
							linkedElement);
				}

				// Make the replacement if necessary.
				if (linkedReplacement != null) {
					context.putElement(path, linkedReplacement);
				}

			} catch (SyntaxException consumed) {
				// Syntax problem will be handled during validation.

			} catch (EvaluationException consumed) {
				// Exception will be handled during validation.
			}

		} catch (ClassCastException consumed) {
			// Exception will be handled during validation.
		}

		// This type will never have a replacement value. The value had to exist
		// to follow the link and we no longer mark links with a separate data
		// class.
		return null;
	}

	@Override
	public Object validate(final Context context, final Element self)
			throws ValidationException {

		try {

			StringProperty link = (StringProperty) self;

			// Ensure that the value is a valid path.
			Path path = null;
			try {
				path = new Path(link.getValue());
			} catch (SyntaxException se) {
				throw ValidationException.create(MSG_INVALID_LINK_PATH, link
						.getValue());
			}

			// Ensure that the link exists.
			Element linkedElement = null;
			try {
				linkedElement = context.getElement(path);
				if (linkedElement == null) {
					throw ValidationException.create(
							MSG_NONEXISTANT_LINK_ELEMENT, path);
				}
			} catch (EvaluationException ee) {
				throw ValidationException.create(MSG_PATH_EVAL_ERROR, path, ee
						.getSimpleMessage());
			}

			// Validate the linked element against the base type.
			try {
				baseType.validate(context, linkedElement);
			} catch (ValidationException ve) {
				ValidationException envelope = ValidationException.create(
						MSG_LINK_ELEMENT_FAILED_VALIDATION, path);
				envelope.initCause(ve);
				throw envelope;
			}

		} catch (ClassCastException cce) {
			throw ValidationException.create(MSG_CONFLICTING_TYPES, "link",
					self.getTypeAsString());
		}
        return null;
	}

	@Override
	public String toString() {
		return baseType.toString() + "*";
	}

}
