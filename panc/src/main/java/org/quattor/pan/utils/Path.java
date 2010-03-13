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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/Path.java $
 $Id: Path.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.utils;

import static org.quattor.pan.utils.MessageUtils.MSG_EXTERNAL_PATH_NOT_ALLOWED;
import static org.quattor.pan.utils.MessageUtils.MSG_FILE_BUG_REPORT;
import static org.quattor.pan.utils.MessageUtils.MSG_PATH_INVALID_AUTHORITY;
import static org.quattor.pan.utils.MessageUtils.MSG_PATH_INVALID_BRACES;
import static org.quattor.pan.utils.MessageUtils.MSG_PATH_INVALID_FIRST_TERM;
import static org.quattor.pan.utils.MessageUtils.MSG_PATH_MISSING_TERM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Template;

/**
 * This immutable class represents a pan path. The paths can be either absolute
 * or relative. The individual path terms are validated when the Path is
 * created; an EvaluationException will be thrown if there is a syntax error.
 * 
 * @author loomis
 * 
 */
public class Path implements Comparable<Path> {

	/**
	 * An enumeration containing the three different types of paths. Note that
	 * the ordering is important because it is used in the comparison of paths.
	 */
	public static enum PathType {
		RELATIVE, ABSOLUTE, EXTERNAL
	};

	/**
	 * The authority of the path if it exists.
	 */
	private String authority;

	/**
	 * The path other than the authority is represented an immutable list of
	 * terms. This must remain immutable, so copies of the terms should be
	 * returned rather than the array itself.
	 */
	private Term[] terms;

	/**
	 * The type of this Path.
	 */
	private PathType type;

	/**
	 * This regular expression defines a valid object term for an external Path.
	 */
	private static Pattern validAuthority = Pattern
			.compile("^[\\w\\-\\+\\.]+$"); //$NON-NLS-1$

	/**
	 * This regular expression extracts the authority from an external Path.
	 */
	private static Pattern extractAuthorityOldForm = Pattern
			.compile("//([^/]*)(?:/(.*))?"); //$NON-NLS-1$

	/**
	 * This regular expression extracts the authority from an external Path.
	 */
	private static Pattern extractAuthority = Pattern.compile("([^:]*):/?(.*)"); //$NON-NLS-1$

	/**
	 * This regular expression will determine if the Path contains proper escape
	 * sequences. The escape sequences cannot be nested and each must contain
	 * both an opening and closing brace.
	 */
	private static Pattern validEscapeSequences = Pattern
			.compile("^([^\\{\\}]*(\\{[^\\{\\}]*\\})?)*$"); //$NON-NLS-1$

	/**
	 * Constructor of a path from a String. If the path does not have the
	 * correct syntax, an EvaluationException will be thrown.
	 */
	public Path(String path) throws SyntaxException {

		assert (path != null);

		// Check that the string contains matched pairs of braces and that those
		// are not nested.
		Matcher matcher = validEscapeSequences.matcher(path);
		if (!matcher.matches()) {
			throw SyntaxException.create(null, MSG_PATH_INVALID_BRACES, path);
		}

		// Escape the strings within braces and re-create the path with those.
		// Splitting on all of the braces will return a list of substrings where
		// all of the ones with odd indices need to be escaped.
		StringBuilder sb = new StringBuilder();
		String[] substrings = path.split("[\\{\\}]", -1); //$NON-NLS-1$
		for (int i = 0; i < substrings.length; i++) {
			if (i % 2 == 0) {
				sb.append(substrings[i]);
			} else {
				sb.append(EscapeUtils.escape(substrings[i]));
			}
		}

		// Reset the path variable to the unescaped value.
		path = sb.toString();

		// Assume that it is a relative path to start.
		String s = path;
		type = PathType.RELATIVE;

		// Start with an empty list. NOTE: This must be a list implementation
		// that provides fast random access to the list elements. This is
		// because some clients will need to look forward in the path to decide
		// what to do.
		ArrayList<Term> xt = new ArrayList<Term>();

		// Determine the type of path and do initial processing if required.
		if (path.startsWith("//")) { //$NON-NLS-1$
			type = PathType.EXTERNAL;
			Matcher m = extractAuthorityOldForm.matcher(path);
			if (m.matches()) {
				if (m.groupCount() == 2) {
					String auth = m.group(1);
					s = m.group(2);

					// Check that the authority is actually valid.
					m = validAuthority.matcher(auth);
					if (m.matches()) {
						authority = auth;
					} else {
						throw SyntaxException.create(null,
								MSG_PATH_INVALID_AUTHORITY, auth);
					}
				} else {
					throw CompilerError.create(MSG_FILE_BUG_REPORT);
				}
			} else {
				throw CompilerError.create(MSG_FILE_BUG_REPORT);
			}
		} else if (path.contains(":")) { //$NON-NLS-1$
			type = PathType.EXTERNAL;
			Matcher m = extractAuthority.matcher(path);
			if (m.matches()) {
				if (m.groupCount() == 2) {
					String auth = m.group(1);
					s = m.group(2);

					// Check that the authority is actually valid. This is
					// potentially a namespaced template name. Use the standard
					// method for determining the validity of the name.
					if (Template.isValidTemplateName(auth)) {
						authority = auth;
					} else {
						throw SyntaxException.create(null,
								MSG_PATH_INVALID_AUTHORITY, auth);
					}
				} else {
					throw CompilerError.create(MSG_FILE_BUG_REPORT);
				}
			} else {
				throw CompilerError.create(MSG_FILE_BUG_REPORT);
			}
		} else if (path.startsWith("/")) { //$NON-NLS-1$
			authority = null;
			type = PathType.ABSOLUTE;
			s = path.substring(1);
		} else {
			authority = null;
		}

		// If the string isn't empty or null, then split the remaining
		// string on slashes.
		if (s != null && !"".equals(s)) { //$NON-NLS-1$
			for (String element : s.split("/")) { //$NON-NLS-1$
				xt.add(TermFactory.create(element));
			}
		}

		// Finally check that there is at least one term if this is a relative
		// Path.
		if (type == PathType.RELATIVE && xt.size() == 0) {
			throw SyntaxException.create(null, MSG_PATH_MISSING_TERM);
		}

		// The first term in a path must always be a key. If not, throw an
		// exception.
		if (xt.size() > 0 && !xt.get(0).isKey()) {
			throw SyntaxException.create(null, MSG_PATH_INVALID_FIRST_TERM);
		}

		// Minimize the space used by the list.
		xt.trimToSize();

		// Make this an unmodifiable list.
		terms = xt.toArray(new Term[xt.size()]);
	}

	/**
	 * Constructor which will create a new Path from the concatenation of two
	 * existing paths. The first argument cannot be an external path. The second
	 * argument may be null.
	 */
	public Path(Path root, Term[] terms) throws SyntaxException {

		// Check that the root path is not an external path.
		if (root.isExternal()) {
			throw SyntaxException.create(null, MSG_EXTERNAL_PATH_NOT_ALLOWED);
		}

		// The type of this path will be the same as the root Path.
		type = root.getType();

		// Copy the authority.
		authority = root.authority;

		// Add in the path terms from the parent and child. Use the known size
		// of the result.
		int size = root.terms.length;
		if (terms != null) {
			size += terms.length;
		}
		Term[] result = new Term[size];

		System.arraycopy(root.terms, 0, result, 0, root.terms.length);
		if (terms != null) {
			System.arraycopy(terms, 0, result, root.terms.length, terms.length);
		}

		// Make this an unmodifiable list.
		this.terms = result;
	}

	/**
	 * This method returns the Path as an unmodifiable list of the terms
	 * comprising the Path.
	 */
	public List<String> toList() {
		List<String> list = new LinkedList<String>();
		if (authority != null) {
			list.add(authority);
		}
		for (Term t : terms) {
			list.add(t.toString());
		}
		return list;
	}

	/**
	 * Get the list of terms in this path. The returned list will implement the
	 * RandomAccess interface, so fast random access to individual element can
	 * be assumed.
	 */
	public Term[] getTerms() {
		return terms.clone();
	}

	/**
	 * Return the type (EXTERNAL, ABSOLUTE, or RELATIVE) for this path.
	 */
	public PathType getType() {
		return type;
	}

	/**
	 * Return the authority for this path or null if it doesn't exist.
	 */
	public String getAuthority() {
		return authority;
	}

	/**
	 * A convenience method which returns a boolean indicating whether the Path
	 * is absolute or not.
	 */
	public boolean isAbsolute() {
		return (type == PathType.ABSOLUTE);
	}

	/**
	 * A convenience method which returns a boolean indicating whether the Path
	 * is relative or not.
	 */
	public boolean isRelative() {
		return (type == PathType.RELATIVE);
	}

	/**
	 * A convenience method which returns a boolean indicating whether the Path
	 * is external or not.
	 */
	public boolean isExternal() {
		return (type == PathType.EXTERNAL);
	}

	/**
	 * Get the type of this path.
	 * 
	 * @return the type of this path
	 */
	public PathType getPathType() {
		return type;
	}

	/**
	 * Determine if two paths are equal.
	 */
	@Override
	public boolean equals(Object obj) {

		// Check obvious mismatches--null or wrong type of instance.
		if (obj == null || !(obj instanceof Path)) {
			return false;
		}

		// The other object is a Path. Cast it into one so we can access the
		// internal fields.
		Path p = (Path) obj;

		// Check that the path types are the same.
		if (type != p.type) {
			return false;
		}

		// Check that the authorities are either equal--they have the same value
		// or are both null.
		if (authority != null && p.authority != null) {
			if (!authority.equals(p.authority)) {
				return false;
			}
		} else if (authority != null || p.authority != null) {
			return false;
		}

		// Finally check the individual terms.
		return Arrays.equals(terms, p.terms);
	}

	/**
	 * This must be defined so that Paths can be used properly in Maps.
	 */
	// FIXME: This should cache the hashcode for a path. This is expensive to
	// recalculate every time. This should also include the authority.
	@Override
	public int hashCode() {

		int code = type.hashCode();

		for (Term t : terms) {
			code = code ^ t.hashCode();
		}
		return code;
	}

	/**
	 * Convert this path to a string.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (isExternal())
			sb.append(authority + ":/"); //$NON-NLS-1$

		if (isAbsolute())
			sb.append("/"); //$NON-NLS-1$

		boolean first = true;
		for (Term t : terms) {
			if (!first) {
				sb.append("/" + t.toString()); //$NON-NLS-1$
			} else {
				sb.append(t.toString());
				first = false;
			}
		}

		return sb.toString();
	}

	/**
	 * The default ordering for paths is such that it will produce a
	 * post-traversal ordering. All relative paths will be before absolute paths
	 * which are before external paths.
	 */
	public int compareTo(Path o) {

		// Sanity check.
		if (o == null) {
			throw new NullPointerException();
		}

		// If these are not the same type, do the simple comparison.
		if (this.type != o.type) {
			return type.compareTo(o.type);
		}

		// Same type of path, so check first the number of terms. If not equal,
		// then it is easy to decide the order. (Longer paths come before
		// shorter ones.)
		int mySize = this.terms.length;
		int otherSize = o.terms.length;
		if (mySize != otherSize) {
			return (mySize < otherSize) ? 1 : -1;
		}

		// Ok, the hard case, the two paths are of the same type and have the
		// same number of terms.
		for (int i = 0; i < mySize; i++) {
			Term myTerm = this.terms[i];
			Term otherTerm = o.terms[i];
			int comparison = myTerm.compareTo(otherTerm);
			if (comparison != 0) {
				return comparison;
			}
		}

		// We've gone through all of the checks, so the two paths must be equal;
		// return zero.
		return 0;
	}
}
