package org.quattor.pan.template;

import static org.quattor.pan.utils.MessageUtils.MSG_DUPLICATE_FUNCTION;

import java.util.HashMap;
import java.util.Map;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.utils.FunctionDefinition;
import org.quattor.pan.utils.MessageUtils;

public class FunctionMap {

	private final Map<String, FunctionDefinition> functions = new HashMap<String, FunctionDefinition>();

	/**
	 * Return the function definition associated with the given name or null if
	 * it doesn't exist. It will always return null if the argument is null.
	 * 
	 * @param name
	 *            name of the function to retrieve
	 * 
	 * @return FunctionDefinition associated with the given name or null if it
	 *         doesn't exist
	 */
	public FunctionDefinition get(String name) {
		return functions.get(name);
	}

	/**
	 * Define the given DML block as a function with the given name in this
	 * context. Note that functions may not be redefined; attempting to do so
	 * will throw an EvaluationException.
	 * 
	 * @param name
	 *            name for the function
	 * @param function
	 *            code for the function as a DML block
	 * @param template
	 *            template in which this function is defined (used for error
	 *            handling)
	 * @param sourceRange
	 *            location in the template where this function is defined (used
	 *            for error handling)
	 * 
	 * @throws EvaluationException
	 *             if a function with the given name already exists
	 */
	public void put(String name, Operation function, Template template,
			SourceRange sourceRange) throws EvaluationException {

		assert (name != null);
		assert (function != null);

		// Create the new function definition.
		FunctionDefinition defn = new FunctionDefinition(template, sourceRange,
				function);

		// Actually set this in the functions hash.
		FunctionDefinition previous = functions.put(name, defn);

		// Oops, the function was already defined. Create a descriptive error
		// message and abort the processing.
		if (previous != null) {
			String msg = MessageUtils.format(MSG_DUPLICATE_FUNCTION, name,
					previous.template.source, previous.sourceRange);
			throw new EvaluationException(msg);
		}

	}

}
