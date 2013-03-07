package org.quattor.pan.utils;

import org.quattor.pan.exceptions.EvaluationException;

/**
 * This class regroups methods used to escape and unescape string. The escape
 * method permits any string (well those containing only ASCII characters) to be
 * converted a valid dict key. The unescape method will invert the conversion.
 * 
 * @author loomis
 * 
 */
public class EscapeUtils {

	private EscapeUtils() {
	}

	public static String escape(String s) {

		StringBuilder sb = new StringBuilder();

		if ("".equals(s)) {

			// The empty string is encoded as a single underscore.
			sb.append("_");

		} else {

			// The normal case where we have to walk through the string and
			// convert characters as we go.
			for (int i = 0; i < s.length(); i++) {
				int c = s.codePointAt(i);

				// Really only ASCII characters can be converted because
				// the encoding format only allows two hex digits.
				if (c >= 0 && c < 256) {

					// No need to escape if the character is a letter or a
					// digit, excepting that the first character may not be
					// a digit. There is no ASCII letter methods; use the
					// digit method to accomplish the same thing.
					int digit = Character.digit(c, 36);
					if (digit > 9 || (i != 0 && digit >= 0)) {
						sb.appendCodePoint(c);
					} else {
						sb.append("_");
						if (c < 16) {
							sb.append("0");
						}
						sb.append(Integer.toHexString(c));
					}
				} else {
					throw new EvaluationException(
							"string contains character which cannot be escaped: "
									+ Integer.toHexString(c));
				}
			}
		}

		return sb.toString();
	}

	public static String unescape(String s) {

		StringBuilder sb = new StringBuilder();

		// A single underscore is the empty string, so we only need to do
		// something if the string is something else.
		if (!"_".equals(s)) {

			int underscore = "_".codePointAt(0);

			for (int i = 0; i < s.length(); i++) {
				int c = s.codePointAt(i);

				if (c != underscore) {

					// Normal character, just copy it.
					sb.appendCodePoint(c);
				} else {

					// Start of an escape sequence. Create the character and
					// append it.
					if (i + 2 < s.length()) {
						String hex = null;
						try {
							hex = s.substring(i + 1, i + 3);
							sb.appendCodePoint(Integer.parseInt(hex, 16));
							i += 2;
						} catch (NumberFormatException nfe) {
							throw new EvaluationException(
									"string contains invalid escape sequence: "
											+ hex);
						}

					} else {
						throw new EvaluationException(
								"string contains incomplete escape sequence");
					}
				}
			}
		}

		return sb.toString();
	}

}
