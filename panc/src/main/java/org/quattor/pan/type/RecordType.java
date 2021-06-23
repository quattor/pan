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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/type/RecordType.java $
 $Id: RecordType.java 3601 2008-08-18 14:16:29Z loomis $
 */

package org.quattor.pan.type;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_KEY_OR_INDEX;
import static org.quattor.pan.utils.MessageUtils.MSG_MISMATCHED_TYPES;
import static org.quattor.pan.utils.MessageUtils.MSG_MISSING_FIELD;
import static org.quattor.pan.utils.MessageUtils.MSG_NONEXISTANT_REFERENCED_TYPE;
import static org.quattor.pan.utils.MessageUtils.MSG_NONRECORD_TYPE_REF;
import static org.quattor.pan.utils.MessageUtils.MSG_UNEXPECTED_FIELDS;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.ttemplate.TypeMap;
import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.Range;
import org.quattor.pan.utils.Term;

/**
 * Implements the pan record type.
 *
 * @author loomis
 *
 */
public class RecordType extends BaseType {

	private final boolean extensible;

	private final Range range;

	private final String[] includes;

	private final Term[] reqKeys;

	private final FullType[] reqTypes;

	private final Term[] optKeys;

	private final FullType[] optTypes;

	public RecordType(String source, SourceRange sourceRange,
			boolean extensible, Range range, List<String> includes,
			SortedMap<Term, FullType> reqFields,
			SortedMap<Term, FullType> optFields) {
		super(source, sourceRange);
		this.extensible = extensible;
		this.range = range;

		String[] temp = (String[]) Array.newInstance(String.class, includes
				.size());
		this.includes = includes.toArray(temp);

		// Add the required field information.
		int index = 0;
		int size = reqFields.size();
		reqKeys = new Term[size];
		reqTypes = new FullType[size];
		for (Map.Entry<Term, FullType> entry : reqFields.entrySet()) {
			reqKeys[index] = entry.getKey();
			reqTypes[index] = entry.getValue();
			index++;
		}

		// Add the required field information.
		index = 0;
		size = optFields.size();
		optKeys = new Term[size];
		optTypes = new FullType[size];
		for (Map.Entry<Term, FullType> entry : optFields.entrySet()) {
			optKeys[index] = entry.getKey();
			optTypes[index] = entry.getValue();
			index++;
		}
	}

	@Override
	public void verifySubtypesDefined(TypeMap types) {

		// Check that the included types exist.
		for (String s : includes) {

			FullType fullType = types.get(s);

			if (fullType != null) {

				// Pull out the type and base type of the included type.
				// Ensure that the base type is a record definition.
				BaseType baseType = fullType.getBaseType();
				if (!(baseType instanceof RecordType)) {
					throw new EvaluationException(MessageUtils.format(
							MSG_NONRECORD_TYPE_REF, s));
				}

			} else {
				throw new EvaluationException(MessageUtils.format(
						MSG_NONEXISTANT_REFERENCED_TYPE, s));
			}
		}

		for (FullType t : reqTypes) {
			t.verifySubtypesDefined(types);
		}
		for (FullType t : optTypes) {
			t.verifySubtypesDefined(types);
		}
	}

	@Override
	public Element findDefault(Context context) throws EvaluationException {

		Element defaultValue = null;

		try {

			// Loop over all of the includes.
			for (String s : includes) {
				try {
					FullType type = context.getFullType(s);
					defaultValue = type.findDefault(context);
					if (defaultValue != null) {
						break;
					}
				} catch (NullPointerException npe) {
					throw CompilerError.create(MSG_NONEXISTANT_REFERENCED_TYPE,
							s);
				}
			}

		} catch (ClassCastException cce) {
			// Ignore. Exception will be dealt with during the validation stage.
		}

		return defaultValue;
	}

	@Override
	public Element setDefaults(Context context, Element self)
			throws EvaluationException {

		assert (self != null);

		HashResource dict = null;

		try {

			// This value is used to keep the current value of the dict. If
			// this
			// ends up being replaced, then the modified value needs to be
			// returned to the caller. Check this at the end of the method.
			dict = (HashResource) self;

			// Loop over all of the includes.
			for (String s : includes) {
				FullType type = context.getFullType(s);
				try {
					HashResource replacement = (HashResource) type.setDefaults(
							context, dict);
					if (replacement != null) {
						dict = replacement;
					}
				} catch (NullPointerException npe) {
					throw CompilerError.create(MSG_NONEXISTANT_REFERENCED_TYPE,
							s);
				}
			}

			// Check all of the required fields.
			for (int i = 0; i < reqKeys.length; i++) {

				// Get name of field and type information.
				Term term = reqKeys[i];
				FullType fullType = reqTypes[i];

				try {
					Element child = dict.get(term);
					if (child != null && !(child instanceof Undef)) {

						// No need to set the default for the current element.
						// May need to do it for subtypes though.
						Element newValue = fullType.setDefaults(context, child);
						if (newValue != null) {
							if (dict.isProtected()) {
								dict = (HashResource) dict.writableCopy();
							}
							dict.put(term, newValue);
						}

					} else {

						// Do need to set the default value. Determine if there
						// is a default value for this field.
						Element defaultValue = fullType.findDefault(context);

						// If there was one, set it and run the setDefaults
						// method on it.
						if (defaultValue != null) {
							if (dict.isProtected()) {
								dict = (HashResource) dict.writableCopy();
							}
							dict.put(term, defaultValue);

							Element newValue = fullType.setDefaults(context,
									defaultValue);
							if (newValue != null) {
								if (dict.isProtected()) {
									dict = (HashResource) dict.writableCopy();
								}
								dict.put(term, newValue);
							}
						}

					}
				} catch (InvalidTermException ite) {
					// This exception should never be encountered because the
					// terms should have been checked by the compiler before
					// validation.
					throw CompilerError.create(MSG_INVALID_KEY_OR_INDEX);
				}

			}

			// Same for all of the optional fields.
			for (int i = 0; i < optKeys.length; i++) {

				// Get name of field and type information.
				Term term = optKeys[i];
				FullType fullType = optTypes[i];

				try {
					// Only if an optional child exists, try to set the
					// defaults. A non-existing, optional child should not be
					// created.
					Element child = dict.get(term);
					if (child != null) {

						// If the child has an 'undef' value, then replace it
						// with the given default.
						if (child instanceof Undef) {
							Element defaultValue = fullType
									.findDefault(context);
							if (defaultValue != null) {
								if (dict.isProtected()) {
									dict = (HashResource) dict.writableCopy();
								}
								dict.put(term, defaultValue);
								child = defaultValue;
							}
						}

						// Recursively set any defaults. (Child could be a
						// resource.)
						Element newValue = fullType.setDefaults(context, child);
						if (newValue != null) {
							if (dict.isProtected()) {
								dict = (HashResource) dict.writableCopy();
							}
							dict.put(term, newValue);
						}
					}
				} catch (InvalidTermException ite) {
					// This exception should never be encountered because the
					// terms should have been checked by the compiler before.
					throw CompilerError.create(MSG_INVALID_KEY_OR_INDEX);
				}

			}

		} catch (ClassCastException consumed) {
			// Ignore. Exception will be dealt with during the validation stage.
		}

		return (self == dict) ? null : dict;
	}

	/**
	 * This method will loop through all of the fields defined in this record
	 * and remove them from the given list. This is used in the validation of
	 * the fields.
	 *
	 * @param context
	 *            ObjectContext to use to look up included type definitions
	 * @param undefinedFields
	 *            List containing the field names to check; defined fields are
	 *            removed directly from the list
	 */
	private void removeDefinedFields(Context context, List<Term> undefinedFields)
			throws ValidationException {

		// Loop through all of the required and optional fields removing each
		// from the list of undefined fields.
		for (Term term : reqKeys) {
			undefinedFields.remove(term);
		}
		for (Term term : optKeys) {
			undefinedFields.remove(term);
		}

		// Now we must apply this method to any included types as well.
		for (String s : includes) {
			try {

				// Pull out the type and base type of the included type.
				// Ensure that the base type is a record definition.
				FullType fullType = context.getFullType(s);
				BaseType baseType = fullType.getBaseType();
				RecordType recordType = (RecordType) baseType;
				recordType.removeDefinedFields(context, undefinedFields);

			} catch (ClassCastException cce) {

				// Should have been checked when the type was defined.
				throw CompilerError.create(MSG_NONRECORD_TYPE_REF, s);

			} catch (NullPointerException npe) {

				// Should have been checked when the type was defined.
				throw CompilerError.create(MSG_NONEXISTANT_REFERENCED_TYPE, s);

			}
		}
	}

	@Override
	public Object validate(final Context context, final Element self)
			throws ValidationException {

		// If this is not an extensible record, then we must verify that
		// each field is defined either directly here or indirectly through
		// one of the included types.
		if (!extensible) {

			try {

				HashResource dict = (HashResource) self;

				// Copy all of the field names into a list.
				List<Term> undefinedFields = new LinkedList<Term>();
				for (Term term : dict.keySet()) {
					undefinedFields.add(term);
				}

				// Now call the method which will remove any fields which are
				// defined either directly or indirectly.
				removeDefinedFields(context, undefinedFields);

				// If there is anything left in the list, then there are
				// undefined fields and we must stop the processing with a
				// validation error.
				if (undefinedFields.size() > 0) {
					StringBuilder sb = new StringBuilder();
					for (Term term : undefinedFields) {
						sb.append(term.toString());
						sb.append(" ");
					}
					throw ValidationException.create(MSG_UNEXPECTED_FIELDS, sb
							.toString());
				}

			} catch (ClassCastException cce) {
				throw ValidationException.createv(self, MSG_MISMATCHED_TYPES,
                                                  "dict", self.getTypeAsString());
			}
		}

		// In all cases call the "included type" validation which does all the
		// rest of the checks.
		validateAsIncludedType(context, self);

        return null;
	}

	@Override
	public void validateAsIncludedType(Context context, Element self)
			throws ValidationException {

		try {

			HashResource dict = (HashResource) self;

			// If this type has an associated range, then check that the number
			// of entries is within that range.
			if (range != null) {
				dict.checkRange(range);
			}

			// Loop over all of the included types, validating each one. Use the
			// restricted validation that does not check for undefined fields.
			// If this was necessary it was already done.
			for (String s : includes) {

				FullType fullType = context.getFullType(s);

				try {

					// Pull out the base type of the included type.
					// Ensure that the base type is a record definition.
					if (!(fullType.getBaseType() instanceof RecordType)) {

						// Should have been checked when type was defined.
						throw CompilerError.create(MSG_NONRECORD_TYPE_REF, s);

					}

					// Run the validation on the type.
					fullType.validateAsIncludedType(context, self);

				} catch (ValidationException ve) {
					throw ve.addTypeToStack(s, fullType);

				} catch (NullPointerException npe) {
					throw CompilerError.create(MSG_NONEXISTANT_REFERENCED_TYPE,
							s);
				}
			}

			// Check all of the required fields.
			for (int i = 0; i < reqKeys.length; i++) {

				// Get name of field and type information.
				Term term = reqKeys[i];
				FullType fullType = reqTypes[i];

				try {
					// Validate each field. Throw an exception if field doesn't
					// exist.
					Element child = dict.get(term);
					if (child != null) {
						try {
							fullType.validate(context, child);
						} catch (ValidationException ve) {
							throw ve.addTerm(term);
						}
					} else {
						throw ValidationException.create(MSG_MISSING_FIELD,
								term);
					}
				} catch (InvalidTermException ite) {
					// This exception should never be encountered because the
					// terms should have been checked before by the compiler.
					throw CompilerError.create(MSG_INVALID_KEY_OR_INDEX);
				}

			}

			// Same for all of the optional fields.
			for (int i = 0; i < optKeys.length; i++) {

				// Get name of field and type information.
				Term term = optKeys[i];
				FullType fullType = optTypes[i];

				try {

					// Validate each existing optional field.
					Element child = dict.get(term);
					if (child != null) {
						try {
							fullType.validate(context, child);
						} catch (ValidationException ve) {
							throw ve.addTerm(term);
						}
					}
				} catch (InvalidTermException ite) {
					// This exception should never be encountered because the
					// terms should have been checked before by the compiler.
					throw CompilerError.create(MSG_INVALID_KEY_OR_INDEX);
				}

			}

		} catch (ClassCastException cce) {
			throw ValidationException.createv(self, MSG_MISMATCHED_TYPES,
                                              "dict", self.getTypeAsString());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (extensible) {
			sb.append("extensible ");
		}
		sb.append("{\n");

		for (String s : includes) {
			sb.append("\tinclude ");
			sb.append(s);
			sb.append("\n");
		}

		for (int i = 0; i < reqKeys.length; i++) {
			sb.append("\n" + reqKeys[i] + " : " + reqTypes[i] + "\n");
		}

		for (int i = 0; i < optKeys.length; i++) {
			sb.append("\n" + optKeys[i] + " ? " + optTypes[i] + "\n");
		}

		sb.append("}");
		if (range != null) {
			sb.append(" (");
			sb.append(range.toString());
			sb.append(")");
		}
		sb.append("\n");

		return sb.toString();
	}
}
