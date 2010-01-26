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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/parser/PanParserUtils.java $
 $Id: PanParserUtils.java 3798 2008-10-17 16:23:46Z loomis $
 */

package org.quattor.pan.parser;

import java.util.List;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.parser.ASTOperation.OperationType;
import org.quattor.pan.template.SourceRange;

public class PanParserUtils {

	static void addSpecialTokens(SimpleNode node, Token token) {
		for (Token currentToken = token; currentToken.specialToken != null; currentToken = currentToken.specialToken) {
			node.addSpecialToken(currentToken.specialToken);
		}
	}

	static SourceRange sourceRangeFromTokens(Token start, Token end) {
		return new SourceRange(start.beginLine, start.beginColumn, end.endLine,
				end.endColumn);
	}

	/**
	 * A utility to replace HEREDOC operations with the associated
	 * StringProperty operations. This must be done at the end of the template
	 * processing to be sure that all heredoc strings have been captured.
	 * 
	 * @param tnode
	 * @param strings
	 */
	static void replaceHeredocStrings(ASTTemplate tnode,
			List<StringProperty> strings) {

		// Only something to do if there were heredoc strings defined.
		if (strings.size() > 0) {
			processHeredocStrings(tnode, strings);
		}
	}

	/**
	 * Descend through the AST and replace individual ASTOperation (HEREDOC)
	 * nodes with the associated strings.
	 * 
	 * @param node
	 * @param strings
	 */
	static private void processHeredocStrings(SimpleNode node,
			List<StringProperty> strings) {

		// If there are any children, then process them in post-traversal order.
		int count = node.jjtGetNumChildren();
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				SimpleNode n = (SimpleNode) node.jjtGetChild(i);

				// If this is a heredoc literal, then actually update the node
				// with the string.
				if (n instanceof ASTOperation) {
					ASTOperation astop = (ASTOperation) n;
					if (astop.getOperationType() == OperationType.HEREDOC) {

						Operation op = astop.getOperation();
						if (op instanceof LongProperty) {
							LongProperty lprop = (LongProperty) op;
							int index = lprop.getValue().intValue();
							StringProperty sprop = strings.get(index);
							astop.setOperationType(OperationType.LITERAL);
							astop.setOperation(sprop);
						} else {
							throw new RuntimeException(
									"HEREDOC operation doesn't contain LongProperty");
						}
					}
				}

				// Alway process any children.
				processHeredocStrings(n, strings);
			}
		}
	}

	/**
	 * This takes a single-quoted string as identified by the parser and
	 * processes it into a standard java string. It removes the surrounding
	 * quotes and substitues a single apostrophe or doubled apostrophes.
	 * 
	 * Note: This method modifies the given StringBuilder directly. No copies of
	 * the data are made.
	 * 
	 * @param sb
	 *            StringBuilder containing the single-quoted string
	 */
	static void processSingleQuotedString(StringBuilder sb) {

		// Sanity checking. Make sure input string has at least two characters
		// and that the first and last are single quotes.
		assert (sb.length() >= 2);
		assert (sb.charAt(0) == '\'' && sb.charAt(sb.length() - 1) == '\'');

		// Remove the starting and ending quotes.
		sb.deleteCharAt(sb.length() - 1);
		sb.deleteCharAt(0);

		// Loop through the string and replace any doubled apostrophes with
		// a single apostrophe. (Really just delete the second apostrophe.)
		int i = sb.indexOf("''", 0) + 1;
		while (i > 0) {
			sb.deleteCharAt(i);
			i = sb.indexOf("''", i) + 1;
		}
	}

	/**
	 * This method takes a double-quoted string as identified by the parser and
	 * creates a standard java string from it.
	 * 
	 * Note: The StringBuilder is modified directly. No copies of the data are
	 * made.
	 * 
	 * @param sb
	 *            StringBuilder which contains the double-quoted string to
	 *            convert
	 * @param sourceRange
	 *            source location for double-quoted string
	 */
	static void processDoubleQuotedString(StringBuilder sb,
			SourceRange sourceRange) throws ParseException {

		// Sanity checking. Make sure input string has at least two characters
		// and that the first and last are double quotes.
		assert (sb.length() >= 2);
		assert (sb.charAt(0) == '"' && sb.charAt(sb.length() - 1) == '"');

		// Remove the starting and ending quotes.
		sb.deleteCharAt(sb.length() - 1);
		sb.deleteCharAt(0);

		// Scan through the string looking for backslashes.
		int i = sb.indexOf("\\", 0);
		while (i >= 0) {

			if (i + 1 >= sb.length()) {
				ParseException pe = new ParseException(
						"double-quoted string contains incomplete escape sequence");
				pe.sourceRange = sourceRange;
				throw pe;
			}

			switch (sb.charAt(i + 1)) {
			case 'n':
				sb.replace(i, i + 2, "\n");
				break;
			case 't':
				sb.replace(i, i + 2, "\t");
				break;
			case 'r':
				sb.replace(i, i + 2, "\r");
				break;
			case '\\':
				sb.replace(i, i + 2, "\\");
				break;
			case '"':
				sb.replace(i, i + 2, "\"");
				break;
			case 'x':
				try {
					int codepoint = Integer.parseInt(
							sb.substring(i + 2, i + 4), 16);
					char[] chars = Character.toChars(codepoint);
					sb.replace(i, i + 4, Character.toString(chars[0]));
				} catch (StringIndexOutOfBoundsException se) {
					ParseException pe = new ParseException(
							"double-quoted string contains incomplete hex escape sequence");
					pe.sourceRange = sourceRange;
					throw pe;
				} catch (NumberFormatException e) {
					ParseException pe = new ParseException(
							"double-quoted string contains illegal hex replacement ("
									+ sb.substring(i + 2, i + 4) + ")");
					pe.sourceRange = sourceRange;
					throw pe;
				}
				break;
			case '\r':
				// Fall through.
			case '\n':
				int count = ((i + 2 < sb.length()) && "\r\n".equals(sb
						.substring(i + 1, i + 3))) ? 3 : 2;
				sb.delete(i, i + count);

				// Because this replaces the entire sequence with nothing, the
				// counter must decremented. If it isn't, then the first
				// character after the replacement will never be checked for a
				// escape sequence. (See SF bug #2533401.)
				i--;

				break;
			default:
				ParseException pe = new ParseException(
						"double-quoted string contains an illegal escape sequence (\\"
								+ sb.charAt(i + 1) + ")");
				pe.sourceRange = sourceRange;
				throw pe;
			}

			i++;
			i = sb.indexOf("\\", i);
		}
	}

}
