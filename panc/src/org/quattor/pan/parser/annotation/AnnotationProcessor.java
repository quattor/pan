package org.quattor.pan.parser.annotation;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotationProcessor {

	private static final Pattern isKeyPairSyntax = Pattern.compile(
			"^(\\s*)([^=\\n\\r\\f]*)=(.*)", Pattern.DOTALL);

	private static final Pattern isAnnotationSyntax = Pattern.compile(
			"^\\s*@\\s*([\\{\\[\\(])(.*)([\\}\\]\\)])\\s*$", Pattern.DOTALL);

	private AnnotationProcessor() {

	}

	/**
	 * Method will process an annotation (in the form of a String) and will
	 * return an unmodifiable map representing the annotation information.
	 * 
	 * @param s
	 *            annotation information in the form of a String; the annotation
	 *            delimiters should already have been stripped off
	 * 
	 * @return unmodifiable map containing annotation information
	 * 
	 * @throws ParseException
	 *             if the annotation contains a syntax error
	 */
	public static Map<String, String> process(String s) throws ParseException {

		Map<String, String> map = null;
		String contents;

		Matcher m = isAnnotationSyntax.matcher(s);
		if (m.matches()) {
			contents = m.group(2);
		} else {
			throw new org.quattor.pan.parser.annotation.ParseException(
					"invalid annotation syntax");
		}

		// Try to treat this as a descriptive annotation.
		map = parseAsDesc(contents);

		// If not a description, then try as a set of key/value pairs.
		if (map == null) {
			AnnotationParser parser = new AnnotationParser(new StringReader(
					contents));
			map = parser.annotation();
		}

		return Collections.unmodifiableMap(map);
	}

	/**
	 * Method will examine the string to ensure that it is not consistent with a
	 * key/value pair syntax; if not, it will then create a map with a single
	 * "desc" key with the given string as the value.
	 * 
	 * A string is compatible with a key/value pair syntax if the first
	 * non-trivial line contains an equal sign. A "non-trivial" line is any line
	 * that contains something other than whitespace.
	 * 
	 * @param s
	 *            string to process into description map; may not be null
	 * 
	 * @return map containing "desc" key with the given value or null if the
	 *         syntax is compatible with a key/value pair syntax
	 * 
	 * @throws NullPointerException
	 *             if the argument is null
	 */
	public static Map<String, String> parseAsDesc(String s) {

		Map<String, String> map = null;

		// First check that the given String is appropriate for a description.
		// This means that the first non-trival line CANNOT contain an equals
		// sign.
		if (!isKeyPairSyntax.matcher(s).matches()) {
			map = new HashMap<String, String>();
			map.put("desc", s);
		}

		return map;
	}

}
