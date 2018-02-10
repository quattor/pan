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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/type/FullType.java $
 $Id: FullType.java 3601 2008-08-18 14:16:29Z loomis $
 */

package org.quattor.pan.type;

import static org.quattor.pan.utils.MessageUtils.MSG_NONEXISTANT_ELEMENT;
import static org.quattor.pan.utils.MessageUtils.MSG_USER_VALIDATION_FAILED;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.ttemplate.TypeMap;

/**
 * Represents a complete type specification in the pan language, which can have
 * a default value and/or a validation block defined.
 *
 * @author loomis
 *
 */
public class FullType extends Type {

	private final BaseType baseType;

	private final Element defaultValue;

	private final Operation dml;

	/**
	 * This contructor is intended for built-in types that have neither source
	 * locations or default values.
	 *
	 * @param baseType
	 */
	public FullType(BaseType baseType) {
		this(null, null, baseType, null, null);
	}

	public FullType(String source, SourceRange sourceRange, BaseType baseType,
			Element defaultValue, Operation dml) {

		super(source, sourceRange);

		assert (baseType != null);

		this.baseType = baseType;
		if (defaultValue != null) {
			this.defaultValue = defaultValue.protect();
		} else {
			this.defaultValue = null;
		}
		this.dml = dml;
	}

	@Override
	public Element findDefault(Context context) {

		return (defaultValue != null) ? defaultValue : baseType
				.findDefault(context);
	}

	@Override
	public Object validate(final Context context, final Element self)
			throws ValidationException {

		// Self cannot be null; if it is, the self element doesn't exist and a
		// validation exception should be thrown. The caller will fill in the
		// necessary details.
		if (self == null) {
			throw ValidationException.create(MSG_NONEXISTANT_ELEMENT);
		}

		// First allow the base type to validate the element.
		baseType.validate(context, self);

		// Now actually run the dml block if it exists.
		if (dml != null) {
			if (!context.executeDmlValidationBlock(dml, self)) {
				ValidationException ve = ValidationException
						.create(MSG_USER_VALIDATION_FAILED);
				if (self instanceof Property) {
					ve.setValue((Property) self);
				}
				throw ve;
			}
		}
        return null;
	}

	/**
	 * A FullType defers the validation to the referenced BaseType, but also
	 * runs the DML validation block if defined.
	 */
	@Override
	public void validateAsIncludedType(Context context, Element self)
			throws ValidationException {

		// Self cannot be null; if it is, the self element doesn't exist and a
		// validation exception should be thrown. The caller will fill in the
		// necessary details.
		if (self == null) {
			throw ValidationException.create(MSG_NONEXISTANT_ELEMENT);
		}

		// First allow the base type to validate the element.
		baseType.validateAsIncludedType(context, self);

		// Now actually run the dml block if it exists.
		if (dml != null) {
			if (!context.executeDmlValidationBlock(dml, self)) {
				ValidationException ve = ValidationException
						.create(MSG_USER_VALIDATION_FAILED);
				if (self instanceof Property) {
					ve.setValue((Property) self);
				}
				throw ve;
			}
		}

	}

	/**
	 * This will call the setDefaults method of the base type with the given
	 * argument. NOTE: this method cannot be called with self equal to null. If
	 * the element doesn't exist, then the caller must extract the default value
	 * from this type as set the value. Afterwards, this method can be called.
	 * This is necessary because we cannot lookup the path on a null value.
	 */
	@Override
	public Element setDefaults(Context context, Element self)
			throws EvaluationException {

		assert (context != null);
		assert (self != null);

		return baseType.setDefaults(context, self);
	}

	@Override
	public void verifySubtypesDefined(TypeMap types) {
		baseType.verifySubtypesDefined(types);
	}

	public BaseType getBaseType() {
		return baseType;
	}

	public String getTypeName() {
		return baseType.toString();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + baseType
				+ ")\ndefault value=" + defaultValue + "\nwith=" + dml + "\n";
	}

}
