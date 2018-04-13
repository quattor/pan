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

import java.io.StringReader;
import java.util.ArrayList;

import org.junit.Test;

public class PanParserTokenManagerSpecialTokenTest {

	@Test
	public void testSingleComment() {

		ArrayList<Token> tokens = parseSpecialTokens("# ALPHA\n\r");
		assertEquals(1, tokens.size());

		Token token = tokens.get(0);
		assertEquals(PanParserConstants.COMMENT, token.kind);
		assertEquals("# ALPHA", token.image);
	}

	@Test
	public void testCommentNoEOL() {

		ArrayList<Token> tokens = parseSpecialTokens("# ALPHA");
		assertEquals(1, tokens.size());

		Token token = tokens.get(0);
		assertEquals(PanParserConstants.COMMENT, token.kind);
		assertEquals("# ALPHA", token.image);
	}

	@Test
	public void testTwoComments() {

		ArrayList<Token> tokens = parseSpecialTokens("# ALPHA\n\r  # BETA\n\r");
		assertEquals(2, tokens.size());

		Token token = tokens.get(0);
		assertEquals(PanParserConstants.COMMENT, token.kind);
		assertEquals("# ALPHA", token.image);

		token = tokens.get(1);
		assertEquals(PanParserConstants.COMMENT, token.kind);
		assertEquals("# BETA", token.image);
	}

	@Test
	public void testEmptyAnnotation() {

		ArrayList<Token> tokens = parseSpecialTokens("@{}");
		assertEquals(1, tokens.size());

		Token token = tokens.get(0);
		assertEquals(PanParserConstants.ANNOTATION, token.kind);
		assertEquals("@{}", token.image);
	}

	@Test
	public void testSingleAnnotation() {

		ArrayList<Token> tokens = parseSpecialTokens("@(alpha = one)");
		assertEquals(1, tokens.size());

		Token token = tokens.get(0);
		assertEquals(PanParserConstants.ANNOTATION, token.kind);
		assertEquals("@(alpha = one)", token.image);
	}

	@Test
	public void testAnnotationWithNewlines() {

		ArrayList<Token> tokens = parseSpecialTokens("@(alpha = one\n\rbeta = two\n)");
		assertEquals(1, tokens.size());

		Token token = tokens.get(0);
		assertEquals(PanParserConstants.ANNOTATION, token.kind);
		assertEquals("@(alpha = one\n\rbeta = two\n)", token.image);
	}

	@Test
	public void testTwoAnnotations() {

		ArrayList<Token> tokens = parseSpecialTokens("@(alpha = one)@(beta = two)");
		assertEquals(2, tokens.size());

		Token token = tokens.get(0);
		assertEquals(PanParserConstants.ANNOTATION, token.kind);
		assertEquals("@(alpha = one)", token.image);
		token = tokens.get(1);
		assertEquals(PanParserConstants.ANNOTATION, token.kind);
		assertEquals("@(beta = two)", token.image);
	}

	/**
	 * Internal method to parse a given string into a sequence of tokens.
	 */
	private ArrayList<Token> parseSpecialTokens(String s) {

		ArrayList<Token> tokens = new ArrayList<Token>();

		SimpleCharStream stream = new SimpleCharStream(new StringReader(s));
		PanParserTokenManager manager = new PanParserTokenManager(stream);

		Token token = manager.getNextToken();
		if (token != null && token.specialToken != null) {

			// Find the starting point of the string of special tokens.
			Token start = token.specialToken;
			while (start.specialToken != null) {
				start = start.specialToken;
			}

			// Add all of the special tokens to the return value.
			do {
				tokens.add(start);
				start = start.next;
			} while (start != null);

		}
		return tokens;
	}

}
