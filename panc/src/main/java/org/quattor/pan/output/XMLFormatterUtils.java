package org.quattor.pan.output;

import java.nio.charset.Charset;

public class XMLFormatterUtils {

	/**
	 * Determine if the given character is a legal starting character for an XML
	 * name. Note that although a colon is a legal value its use is strongly
	 * discouraged and this method will return false for a colon.
	 *
	 * The list of valid characters can be found in the <a
	 * href="http://www.w3.org/TR/xml/0sec-common-syn">Common Syntactic
	 * Constructs section</a> of the XML specification.
	 *
	 * @param codepoint
	 *
	 * @return true if the character may appear as the first letter of an XML
	 *         name; false otherwise
	 */
	public static boolean isXMLNameStart(int codepoint) {

		if (codepoint >= Character.codePointAt("A", 0)
				&& codepoint <= Character.codePointAt("Z", 0)) {
			return true;
		} else if (codepoint == Character.codePointAt("_", 0)) {
			return true;
		} else if (codepoint >= Character.codePointAt("a", 0)
				&& codepoint <= Character.codePointAt("z", 0)) {
			return true;
		} else if (codepoint >= 0xC0 && codepoint <= 0xD6) {
			return true;
		} else if (codepoint >= 0xD8 && codepoint <= 0xF6) {
			return true;
		} else if (codepoint >= 0xF8 && codepoint <= 0x2FF) {
			return true;
		} else if (codepoint >= 0x370 && codepoint <= 0x37D) {
			return true;
		} else if (codepoint >= 0x37F && codepoint <= 0x1FFF) {
			return true;
		} else if (codepoint >= 0x200C && codepoint <= 0x200D) {
			return true;
		} else if (codepoint >= 0x2070 && codepoint <= 0x218F) {
			return true;
		} else if (codepoint >= 0x2C00 && codepoint <= 0x2FEF) {
			return true;
		} else if (codepoint >= 0x3001 && codepoint <= 0xD7FF) {
			return true;
		} else if (codepoint >= 0xF900 && codepoint <= 0xFDCF) {
			return true;
		} else if (codepoint >= 0xFDF0 && codepoint <= 0xFFFD) {
			return true;
		} else if (codepoint >= 0x10000 && codepoint <= 0xEFFFF) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determine if the given character is a legal non-starting character for an
	 * XML name. Note that although a colon is a legal value its use is strongly
	 * discouraged and this method will return false for a colon.
	 *
	 * The list of valid characters can be found in the <a
	 * href="http://www.w3.org/TR/xml/0sec-common-syn">Common Syntactic
	 * Constructs section</a> of the XML specification.
	 *
	 * @param codepoint
	 *
	 * @return true if the character may appear as a non-starting letter of an
	 *         XML name; false otherwise
	 */
	public static boolean isXMLNamePart(int codepoint) {

		if (isXMLNameStart(codepoint)) {
			return true;
		} else if (codepoint >= Character.codePointAt("0", 0)
				&& codepoint <= Character.codePointAt("9", 0)) {
			return true;
		} else if (codepoint == Character.codePointAt("-", 0)) {
			return true;
		} else if (codepoint == Character.codePointAt(".", 0)) {
			return true;
		} else if (codepoint == 0xB7) {
			return true;
		} else if (codepoint >= 0x0300 && codepoint <= 0x036F) {
			return true;
		} else if (codepoint >= 0x203F && codepoint <= 0x2040) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determine if a given UNICODE character can appear in a valid XML file.
	 * The allowed characters are 0x9, 0xA, 0xD, 0x20-0xD7FF, 0xE000-0xFFFD, and
	 * 0x100000-0x10FFFF; all other characters may not appear in an XML file.
	 *
	 * @param codepoint
	 *
	 * @return true if the character may appear in a valid XML file; false
	 *         otherwise
	 */
	public static boolean isValidXMLCharacter(int codepoint) {

		// Most of the time the character will be valid. Order the tests such
		// that most valid characters will be found in the minimum number of
		// tests.
		if (codepoint >= 0x20 && codepoint <= 0xD7FF) {
			return true;
		} else if (codepoint == 0x9 || codepoint == 0xA || codepoint == 0xD) {
			return true;
		} else if (codepoint >= 0xE000 && codepoint <= 0xFFFD) {
			return true;
		} else if (codepoint >= 0x10000 && codepoint <= 0x10FFFF) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determine if the given string is a valid XML name.
	 *
	 * @param s
	 *            String to examine for illegal XML characters
	 * @return true if the String can be written to an XML file without
	 *         encoding; false otherwise
	 */
	public static boolean isValidXMLName(String s) {

		// Catch the empty string or null.
		if (s == null || "".equals(s)) {
			return false;
		}

		// Since the string isn't empty, check that the first character is a
		// valid starting character.
		if (!isXMLNameStart(s.codePointAt(0))) {
			return false;
		}

		// Loop through the string by UNICODE codepoints. This is NOT equivalent
		// to looping through the characters because some UNICODE codepoints can
		// occupy more than one character.
		int length = s.length();
		int index = 1;
		while (index < length) {
			int codePoint = s.codePointAt(index);
			if (!isXMLNamePart(codePoint)) {
				return false;
			}
			index += Character.charCount(codePoint);
		}

		// Names that begin with "xml" with letters in any case are reserved by
		// the XML specification.
		if (s.toLowerCase().startsWith("xml")) {
			return false;
		}

		// If we get here then all of the characters have been checked and are
		// valid. Unfortunately the usual case takes the longest to verify.
		return true;
	}

	/**
	 * Determine if the given string can be written to an XML file without
	 * encoding. This will be the case so long as the string does not contain
	 * illegal XML characters.
	 *
	 * @param s
	 *            String to examine for illegal XML characters
	 * @return true if the String can be written to an XML file without
	 *         encoding; false otherwise
	 */
	public static boolean isValidXMLString(String s) {

		int length = s.length();

		// Loop through the string by UNICODE codepoints. This is NOT equivalent
		// to looping through the characters because some UNICODE codepoints can
		// occupy more than one character.
		int index = 0;
		while (index < length) {
			int codePoint = s.codePointAt(index);
			if (!isValidXMLCharacter(codePoint)) {
				return false;
			}
			index += Character.charCount(codePoint);
		}

		// If we get here then all of the characters have been checked and are
		// valid. Unfortunately the usual case takes the longest to verify.
		return true;
	}

	/**
	 * This will encode the given string as a valid XML name. The format will be
	 * an initial underscore followed by the hexadecimal representation of the
	 * string.
	 *
	 * The method will throw an exception if the argument is null.
	 *
	 * @param s
	 *            String to encode as an XML name
	 *
	 * @return valid XML name from String
	 *
	 * @throws NullPointerException
	 */
	public static String encodeAsXMLName(String s) {

		StringBuilder sb = new StringBuilder("_");
		for (byte b : s.getBytes(Charset.forName("UTF-8"))) {
			sb.append(Integer.toHexString((b >>> 4) & 0xF));
			sb.append(Integer.toHexString(b & 0xF));
		}

		return sb.toString();
	}


}
