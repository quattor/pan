package org.quattor.pan.ttemplate;

import java.util.Map;
import java.util.TreeMap;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;

/**
 * This class contains a map intended to hold the definition of local variables.
 * 
 * @author loomis
 * 
 */
public class LocalVariableMap {

	private final Map<String, Element> map;

	/**
	 * Create an instance that contains no local variable mappings initially.
	 */
	public LocalVariableMap() {
		map = new TreeMap<String, Element>();
	}

	/**
	 * Create an instance that contains mappings for the variables ARGV and
	 * ARGC. This is typical of a function call initialization. If this is
	 * called with a null argument, then ARGV and ARGC are not set.
	 * 
	 * @param argv
	 *            ListResource to use to set ARGV and ARGC variables
	 */
	public LocalVariableMap(ListResource argv) {
		this();

		if (argv != null) {
			LongProperty size = LongProperty.getInstance(argv.size());
			map.put("ARGV", argv);
			map.put("ARGC", size);
		}

	}

	/**
	 * Lookup the value associated with the given variable name. If the name
	 * isn't defined, then null is returned.
	 * 
	 * @param name
	 *            variable name to lookup
	 * 
	 * @return Element associated with the given name or null if it doesn't
	 *         exist
	 */
	public Element get(String name) {
		return map.get(name);
	}

	/**
	 * Assign the value to the given variable name. If the value is null, then
	 * the variable is undefined. If an old value existed, then this method will
	 * check that the new value is a valid replacement for the old one. If not,
	 * an exception will be thrown.
	 * 
	 * @param name
	 *            variable name to assign value to
	 * @param value
	 *            Element to assign to the given variable name; variable is
	 *            removed if the value is null
	 * 
	 * @return old value of the named variable or null if it wasn't defined
	 */
	public Element put(String name, Element value) {

		assert (name != null);

		Element oldValue = null;

		if (value != null) {

			// Set the value and ensure that the replacement can be done.
			oldValue = map.put(name, value);
			if (oldValue != null) {
				oldValue.checkValidReplacement(value);
			}

		} else {

			// Remove the referenced variable.
			oldValue = map.remove(name);
		}

		return oldValue;
	}

}
