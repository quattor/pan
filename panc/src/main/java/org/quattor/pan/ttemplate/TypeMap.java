package org.quattor.pan.ttemplate;

import static org.quattor.pan.utils.MessageUtils.MSG_DUPLICATE_TYPE;

import java.util.HashMap;
import java.util.Map;

import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.type.BaseType;
import org.quattor.pan.type.FullType;
import org.quattor.pan.utils.MessageUtils;

public class TypeMap {

	private final Map<String, FullType> types;

	public TypeMap() {

		types = new HashMap<String, FullType>();

		// Define all of the built-in types which correspond to data elements.
		// Unique instances of these types are kept by the BuiltInType class.
		Map<String, FullType> builtins = BaseType.getBuiltinTypes();
		for (Map.Entry<String, FullType> entry : builtins.entrySet()) {
			put(entry.getKey(), entry.getValue(), null, null);
		}

	}

	/**
	 * Return the type associated with the given name or null if it doesn't
	 * exist. It will always return null if the argument is null.
	 * 
	 * @param name
	 *            name of the type to retrieve
	 * 
	 * @return FullType associated with this name or null if it doesn't exist
	 */
	public FullType get(String name) {
		return types.get(name);
	}

	/**
	 * Associate the given type with the given name within this ObjectContext.
	 * This will throw an EvaluationException if the type is already defined.
	 * 
	 * @param name
	 *            name to associate to the type
	 * @param fullType
	 *            data type to use for the definition
	 * @param template
	 *            template where the type is defined (used for error handling)
	 * @param sourceRange
	 *            location in the template where the type is defined (used for
	 *            error handling)
	 * 
	 * @throws EvaluationException
	 *             if there is already a type associated with the given name
	 */
	public void put(String name, FullType fullType, Template template,
			SourceRange sourceRange) throws EvaluationException {

		assert (name != null);
		assert (fullType != null);

		// Ensure that all referenced types are already defined. This must be
		// done before adding this type to the table to avoid type definition
		// loops.
		fullType.verifySubtypesDefined(this);

		// Set the type in the types hash.
		FullType previous = types.put(name, fullType);

		// Oops, the type was already defined. Create a descriptive error
		// message and abort the processing.
		if (previous != null) {
			String msg = MessageUtils.format(MSG_DUPLICATE_TYPE, name, previous
					.getSource(), previous.getSourceRange());
			throw new EvaluationException(msg);
		}

	}

}
