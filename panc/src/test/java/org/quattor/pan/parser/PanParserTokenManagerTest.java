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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/parser/PanParserTokenManagerTest.java $
 $Id: PanParserTokenManagerTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.ArrayList;

import org.junit.Test;

public class PanParserTokenManagerTest {

	@Test
	public void testKeywords() {

		String[] keywords = { "if", "with", "else", "type", "bind", "while",
				"valid", "final", "object", "include", "template", "function",
				"variable", "structure", "extensible", "declaration" };
		int[] kinds = { PanParserConstants.IF, PanParserConstants.WITH,
				PanParserConstants.ELSE, PanParserConstants.TYPE,
				PanParserConstants.BIND, PanParserConstants.WHILE,
				PanParserConstants.VALID, PanParserConstants.FINAL,
				PanParserConstants.OBJECT, PanParserConstants.INCLUDE,
				PanParserConstants.TEMPLATE, PanParserConstants.FUNCTION,
				PanParserConstants.VARIABLE, PanParserConstants.STRUCTURE,
				PanParserConstants.EXTENSIBLE, PanParserConstants.DECLARATION };

		// Check that keywords are properly identified if followed by an
		// end-of-file.
		for (int i = 0; i < keywords.length; i++) {

			String s = keywords[i];
			int kind = kinds[i];

			ArrayList<Token> tokens = parseString(s);
			assertEquals("check one token returned", 1, tokens.size());

			Token token = tokens.get(0);
			assertEquals("check 'kind' field for keyword", kind, token.kind);
			assertEquals("check 'image' field for keyword", s, token.image);
		}

		// Check that keywords are properly identified if followed by
		// whitespace.
		for (int i = 0; i < keywords.length; i++) {

			String s = keywords[i];
			int kind = kinds[i];

			ArrayList<Token> tokens = parseString(s + "\t");
			assertEquals("check one token returned", 1, tokens.size());

			Token token = tokens.get(0);
			assertEquals("check 'kind' field for keyword", kind, token.kind);
			assertEquals("check 'image' field for keyword", s, token.image);
		}

		// Check that keywords are properly identified if followed by
		// non-letter (semi-colon here).
		for (int i = 0; i < keywords.length; i++) {

			String s = keywords[i];
			int kind = kinds[i];

			ArrayList<Token> tokens = parseString(s + " object\n");
			assertEquals("check two tokens returned", 2, tokens.size());

			Token token = tokens.get(0);
			assertEquals("check 'kind' field for keyword", kind, token.kind);
			assertEquals("check 'image' field for keyword", s, token.image);
		}
	}

	@Test
	public void testComments() {

		ArrayList<Token> tokens;
		String s;

		s = "# Simple comment with EOF afterwards";
		tokens = parseString(s);
		assertEquals("Comment followed by EOF", 0, tokens.size());

		s = "# Simple comment with new line afterwards\n";
		tokens = parseString(s);
		assertEquals("Comment followed by new line", 0, tokens.size());

		s = "template # comment after token\n";
		tokens = parseString(s);
		assertEquals("Token followed by comment", 1, tokens.size());
	}

	@Test
	public void testWhitespace() {

		ArrayList<Token> tokens;
		String s;

		s = "\n\r\t\f\r\n";
		tokens = parseString(s);
		assertEquals("Ignore all whitespace", 0, tokens.size());
	}

	@Test
	public void testHeredocMarker() {

		ArrayList<Token> tokens;
		String s;

		s = "<<EOF\nalpha\nEOF\n";
		tokens = parseStringReplacingHeredoc(s);
		assertEquals("Check basic heredoc handling", 1, tokens.size());

		Token token = tokens.get(0);
		assertEquals("check 'kind' field for keyword",
				PanParserConstants.HEREDOC_STRING, token.kind);

		assertEquals("check correct tag is picked up", "alpha\n", token.image);

		s = "<<EOF+<<EOG\nalpha\nEOF\nbeta\nEOG\n";
		tokens = parseStringReplacingHeredoc(s);
		assertEquals("Check basic heredoc handling", 3, tokens.size());

		token = tokens.get(0);
		assertEquals("check 'kind' field for keyword",
				PanParserConstants.HEREDOC_STRING, token.kind);

		assertEquals("check correct tag is picked up", "alpha\n", token.image);

		token = tokens.get(2);
		assertEquals("check 'kind' field for keyword",
				PanParserConstants.HEREDOC_STRING, token.kind);

		assertEquals("check correct tag is picked up", "beta\n", token.image);
	}

	@Test
	public void testNumericLiterals() {

		String[] values = { "1234", "01234", "0x1234", "0.1", "0.1e+10",
				"0.1E-10", "1e10", "9e-10" };
		int[] kinds = { PanParserConstants.DECIMAL_LITERAL,
				PanParserConstants.OCTAL_LITERAL,
				PanParserConstants.HEX_LITERAL,
				PanParserConstants.DOUBLE_LITERAL,
				PanParserConstants.DOUBLE_LITERAL,
				PanParserConstants.DOUBLE_LITERAL,
				PanParserConstants.DOUBLE_LITERAL,
				PanParserConstants.DOUBLE_LITERAL };

		for (int i = 0; i < values.length; i++) {
			String s = values[i];
			int kind = kinds[i];

			ArrayList<Token> tokens = parseString(s);
			assertEquals("check number one token returned", 1, tokens.size());

			Token token = tokens.get(0);
			assertEquals("check 'kind' field for number (" + s + ")", kind,
					token.kind);
			assertEquals("check 'image' field for number (" + s + ")", s,
					token.image);
		}

	}

	@Test
	public void testMiscLiterals() {

		String[] values = { "undef", "null", "true", "false", "<<", "*" };
		int[] kinds = { PanParserConstants.UNDEF_LITERAL,
				PanParserConstants.NULL_LITERAL, PanParserConstants.TRUE,
				PanParserConstants.FALSE, PanParserConstants.HD_MARKER,
				PanParserConstants.ASTERISK, };

		for (int i = 0; i < values.length; i++) {
			String s = values[i];
			int kind = kinds[i];

			ArrayList<Token> tokens = parseString(s);
			assertEquals("check misc literal one token returned", 1, tokens
					.size());

			Token token = tokens.get(0);
			assertEquals("check 'kind' field for literal (" + s + ")", kind,
					token.kind);
			assertEquals("check 'image' field for literal (" + s + ")", s,
					token.image);
		}

	}

	@Test
	public void testIdentifiers() {

		String[] values = { "alpha", "ALPHA", "_alpha", "_ALPHA", "_1", "_a1" };
		int[] kinds = { PanParserConstants.IDENTIFIER,
				PanParserConstants.IDENTIFIER, PanParserConstants.IDENTIFIER,
				PanParserConstants.IDENTIFIER, PanParserConstants.IDENTIFIER,
				PanParserConstants.IDENTIFIER, };

		for (int i = 0; i < values.length; i++) {
			String s = values[i];
			int kind = kinds[i];

			ArrayList<Token> tokens = parseString(s);
			assertEquals("check number one token returned", 1, tokens.size());

			Token token = tokens.get(0);
			assertEquals("check 'kind' field for identifier (" + s + ")", kind,
					token.kind);
			assertEquals("check 'image' field for identifier (" + s + ")", s,
					token.image);
		}

	}

	@Test
	public void testNSIdentifiers() {

		String[] values = { "127.0.0.1", "www.example.org", "alpha/beta/gamma",
				"alpha-beta-gamma" };
		int[] kinds = { PanParserConstants.NS_IDENTIFIER,
				PanParserConstants.NS_IDENTIFIER,
				PanParserConstants.NS_IDENTIFIER,
				PanParserConstants.NS_IDENTIFIER, };

		// Check that we go into NAMESPACE mode after 'template'.
		for (int i = 0; i < values.length; i++) {
			String s = values[i];
			int kind = kinds[i];

			ArrayList<Token> tokens = parseString("template " + s);
			assertEquals("check number two tokens returned", tokens.size(), 2);

			Token token = tokens.get(1);
			assertEquals("check 'kind' field for identifier (" + s + ")", kind,
					token.kind);
			assertEquals("check 'image' field for identifier (" + s + ")", s,
					token.image);
		}
	}

	@Test
	public void testSingleQuotedString() {

		String[] values = { "alpha", "0123", "''", "1''", "''1", "" };
		String[] results = { "alpha", "0123", "'", "1'", "'1", "" };

		for (int i = 0; i < values.length; i++) {
			String s = values[i];
			String r = results[i];

			ArrayList<Token> tokens = parseString("'" + s + "'");
			assertEquals("check misc literal one token returned", 1, tokens
					.size());

			Token token = tokens.get(0);
			assertEquals("check 'kind' for single quoted string (" + s + ")",
					PanParserConstants.SINGLE_QUOTED_STRING, token.kind);
			assertEquals("check 'image' for single quoted string (" + s + ")",
					r, token.image);
		}
	}

	@Test
	public void testDoubleQuotedString() {

		String[] values = { "", "\\n", "\\t", "\\r", "\\\\", "\\\"", "\\xff",
				"\\x00" };
		String[] results = { "", "\n", "\t", "\r", "\\", "\"", "\u00ff",
				"\u0000" };

		for (int i = 0; i < values.length; i++) {

			String s = values[i];
			String r = results[i];

			ArrayList<Token> tokens = parseString("\"" + s + "\"");
			assertEquals("check misc literal one token returned", 1, tokens
					.size());

			Token token = tokens.get(0);
			assertEquals("check 'kind' for double quoted string (" + s + ")",
					PanParserConstants.DOUBLE_QUOTED_STRING, token.kind);
			assertEquals("check 'image' for double quoted string (" + s + ")",
					r, token.image);
		}

	}

	/**
	 * Internal method to parse a given string into a sequence of tokens.
	 */
	private ArrayList<Token> parseString(String s) {

		ArrayList<Token> tokens = new ArrayList<Token>();

		SimpleCharStream stream = new SimpleCharStream(new StringReader(s));
		PanParserTokenManager manager = new PanParserTokenManager(stream);

		for (Token token = manager.getNextToken(); token.kind != PanParserConstants.EOF; token = manager
				.getNextToken()) {
			tokens.add(token);
		}
		return tokens;
	}

	/**
	 * Internal method to parse a given string into a sequence of tokens.
	 * Heredoc markers are replaced with the values saved in the token manager.
	 */
	private ArrayList<Token> parseStringReplacingHeredoc(String s) {

		ArrayList<Token> tokens = new ArrayList<Token>();

		SimpleCharStream stream = new SimpleCharStream(new StringReader(s));
		PanParserTokenManager manager = new PanParserTokenManager(stream);

		for (Token token = manager.getNextToken(); token.kind != PanParserConstants.EOF; token = manager
				.getNextToken()) {
			tokens.add(token);
		}

		// Loop over the tokens and replace heredoc markers by their content.
		for (Token t : tokens) {
			if (t.kind == PanParserConstants.HEREDOC_STRING) {
				int index = -1;
				try {
					index = Integer.parseInt(t.image);
				} catch (NumberFormatException nfe) {
					fail("encountered heredoc marker which is not a valid integer");
					return null;
				}
				try {
					t.image = manager.getHeredocStrings().get(index).getValue();
				} catch (ArrayIndexOutOfBoundsException e) {
					fail("error retrieving heredoc string with index " + index);
					return null;
				}
			}
		}

		return tokens;
	}
}
