package org.quattor.pan.output;

import static org.quattor.pan.utils.MessageUtils.MSG_DUPLICATE_FORMATTER_KEY;

import java.util.HashMap;

import org.quattor.pan.exceptions.CompilerError;

public class FormatterUtils {

	private final static HashMap<String, Formatter> formatters = new HashMap<String, Formatter>();

	// Setup the map between a formatter name and the formatter instance. Since
	// the formatters are singletons, we can give the same instance to everyone
	// that asks.
	static {

		// If more formats are added, the instances should be added to this
		// array.
		Formatter[] instances = new Formatter[] { DotFormatter.getInstance(),
				PanFormatter.getInstance(), TxtFormatter.getInstance(),
				XmlDBFormatter.getInstance() };

		// Insert the values, letting the instances choose their key values.
		// Ensure that the values are in lowercase.
		for (Formatter f : instances) {
			String key = f.getFormatKey().toLowerCase();
			Formatter existingValue = formatters.put(key, f);
			if (existingValue != null) {
				throw CompilerError.create(MSG_DUPLICATE_FORMATTER_KEY);
			}
		}
	}

	private final static Formatter defaultFormatter = PanFormatter
			.getInstance();

	/**
	 * This method maps a formatter name to a <code>Formatter</code> instance.
	 * If the formatter name is unknown, then null will be returned. The name
	 * comparison ignores the case of the given name.
	 * 
	 * @param name
	 *            name of the formatter
	 * 
	 * @return <code>Formatter</code> instance corresponding to the name or null
	 *         if the formatter name is unknown
	 */
	public static Formatter getFormatterInstance(String name) {
		String key = (name != null) ? name.toLowerCase() : "";
		return formatters.get(key);
	}

	/**
	 * This method returns the default formatter to use if the user does not
	 * specify one explicitly.
	 * 
	 * @return default Formatter for output files
	 */
	public static Formatter getDefaultFormatterInstance() {
		return defaultFormatter;
	}

}
