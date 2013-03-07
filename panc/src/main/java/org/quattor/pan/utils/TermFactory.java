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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/Sources/panc/trunk/src/org/quattor/pan/utils/Term.java $
 $Id: Term.java 1000 2006-11-15 20:47:58Z loomis $
 */

package org.quattor.pan.utils;

import static org.quattor.pan.utils.MessageUtils.MSG_INDEX_CANNOT_BE_NEGATIVE;
import static org.quattor.pan.utils.MessageUtils.MSG_INDEX_EXCEEDS_MAXIMUM;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ELEMENT_FOR_INDEX;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_KEY;
import static org.quattor.pan.utils.MessageUtils.MSG_KEY_CANNOT_BEGIN_WITH_DIGIT;
import static org.quattor.pan.utils.MessageUtils.MSG_KEY_CANNOT_BE_EMPTY_STRING;

import java.util.regex.Pattern;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;

/**
 * A factory to generate Term objects. The produced objects are just standard
 * StringProperty and LongProperty instances.
 * 
 * @author loomis
 * 
 */
public class TermFactory {

	/**
	 * This regular expression identifies strings which are a list index. (That
	 * is, they start with a digit.)
	 */
	private static final Pattern isIndexPattern = Pattern.compile("^\\d.*$"); //$NON-NLS-1$

	/**
	 * This regular expression identifies valid dict keys.
	 */
	private static final Pattern isKeyPattern = Pattern
			.compile("^[a-zA-Z_][\\w\\+\\-\\.]*$"); //$NON-NLS-1$

	/**
	 * This array contains a cache of the most frequently used indexes. It
	 * avoids having to create new LongProperties as Term objects when not
	 * necessary.
	 */
	private static final Term[] indexCache;

	// This code creates all of the cached indexes.
	static {
		int size = 100;
		Term[] terms = new Term[size];
		for (int i = 0; i < size; i++) {
			terms[i] = LongProperty.getInstance((long) i);
		}
		indexCache = terms;
	}

	private TermFactory() {
	}

	/**
	 * An internal method to check that a numeric index is valid. It must be a
	 * non-negative integer.
	 * 
	 * @param index
	 * @return validated index as an Integer
	 */
	private static void checkNumericIndex(long index) {
		if (index > Integer.MAX_VALUE) {
			throw EvaluationException.create(MSG_INDEX_EXCEEDS_MAXIMUM, index,
					Integer.MAX_VALUE);
		} else if (index < 0) {
			throw EvaluationException.create(MSG_INDEX_CANNOT_BE_NEGATIVE,
					index);
		}
	}

	/**
	 * An internal method to check that a string value is valid. It can either
	 * be an Integer or String which is returned.
	 * 
	 * @param term
	 * @return validated term as Integer or String
	 */
	private static long checkStringIndex(String term) {

		assert (term != null);

		long result = 0L;

		// Empty strings are not allowed.
		if ("".equals(term)) { //$NON-NLS-1$
			throw EvaluationException.create(MSG_KEY_CANNOT_BE_EMPTY_STRING);
		}

		if (isIndexPattern.matcher(term).matches()) {

			// This starts with a number, so try to convert it to an integer.
			try {
				result = Long.decode(term).longValue();
				checkNumericIndex(result);
			} catch (NumberFormatException nfe) {
				throw EvaluationException.create(
						MSG_KEY_CANNOT_BEGIN_WITH_DIGIT, term);
			}

		} else if (isKeyPattern.matcher(term).matches()) {

			// Return a negative number to indicate that this is an OK key
			// value.
			result = -1L;

		} else {

			// Doesn't work for either a key or index.
			throw EvaluationException.create(MSG_INVALID_KEY, term);
		}

		return result;
	}

	/**
	 * Constructor of a path from a String. If the path does not have the
	 * correct syntax, an IllegalArgumentException will be thrown.
	 */
	public static Term create(String term) {
		long value = checkStringIndex(term);
		if (value < 0L) {
			return StringProperty.getInstance(term);
		} else {
			return create(value);
		}
	}

	/**
	 * Create a term directly from a long index.
	 * 
	 * @param index
	 *            the index to use for this term
	 */
	public static Term create(long index) {

		checkNumericIndex(index);

		// Decide whether to use a cached value or not. Negative indexes are
		// removed by the check above, so there is no need to check again.
		if (index < indexCache.length) {

			// Use cached value.
			return indexCache[(int) index];

		} else {

			// Generate a new property.
			return LongProperty.getInstance(index);

		}
	}

	/**
	 * Create a term from a given element.
	 * 
	 * @param element
	 *            the element to create the term from
	 */
	public static Term create(Element element) {
		if (element instanceof StringProperty) {
			long value = checkStringIndex(((StringProperty) element).getValue());
			if (value < 0L) {
				return (Term) element;
			} else {
				return LongProperty.getInstance(value);
			}
		} else if (element instanceof LongProperty) {
			checkNumericIndex(((LongProperty) element).getValue());
			return (Term) element;
		} else {
			throw EvaluationException.create(MSG_INVALID_ELEMENT_FOR_INDEX,
					element.getTypeAsString());
		}
	}

	/**
	 * A utility method to allow the comparison of any two terms.
	 */
	public static int compare(Term self, Term other) {

		// Sanity check.
		if (self == null || other == null) {
			throw new NullPointerException();
		}

		// Identical objects are always equal.
		if (self == other) {
			return 0;
		}

		// Easy case is when they are not the same type of term.
		if (self.isKey() != other.isKey()) {
			return (self.isKey()) ? 1 : -1;
		}

		// Compare the underlying values.
		try {
			if (self.isKey()) {
				return self.getKey().compareTo(other.getKey());
			} else {
				return self.getIndex().compareTo(other.getIndex());
			}
		} catch (InvalidTermException consumed) {
			// This statement can never be reached because both objects are
			// either keys or indexes. This try/catch block is only here to make
			// the compiler happy.
			assert (false);
			return 0;
		}
	}

}
