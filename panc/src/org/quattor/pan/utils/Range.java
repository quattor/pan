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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/Range.java $
 $Id: Range.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.utils;

import static org.quattor.pan.utils.MessageUtils.MSG_MAX_RANGE_VALUE_IS_NOT_VALID_LONG;
import static org.quattor.pan.utils.MessageUtils.MSG_MIN_MUST_BE_LESS_OR_EQUAL_TO_MAX;
import static org.quattor.pan.utils.MessageUtils.MSG_MIN_RANGE_VALUE_CANNOT_BE_NEGATIVE;
import static org.quattor.pan.utils.MessageUtils.MSG_MIN_RANGE_VALUE_IS_NOT_VALID_LONG;

import java.io.Serializable;

import org.quattor.pan.exceptions.EvaluationException;

/**
 * Represents a range associated with a type.
 * 
 * @author loomis
 * 
 */
// FIXME: Should this throw SyntaxExceptions rather than EvaluationExceptions?
public class Range implements Serializable {

	private static final long serialVersionUID = 2879164571801135202L;

	private final long min;

	private final long max;

	/**
	 * A convenience constructor which takes two longs encoded as strings and
	 * creates a Range. An IllegalArgumentException will be thrown if either
	 * string does not contain a valid long value. It will also throw an
	 * IllegalArgumentException if the minimum value is less than zero or the
	 * minimum value is greater than the maximum value.
	 * 
	 * @param minimum
	 * @param maximum
	 */
	public Range(String minimum, String maximum) {

		// Convert the strings to long values, checking for conversion problems.
		long minValue;
		try {
			minValue = minimum != null ? Long.decode(minimum) : 0L;
		} catch (NumberFormatException e) {
			throw EvaluationException.create(
					MSG_MIN_RANGE_VALUE_IS_NOT_VALID_LONG, minimum);
		}

		long maxValue;
		try {
			maxValue = maximum != null ? Long.decode(maximum) : Long.MAX_VALUE;
		} catch (NumberFormatException e) {
			throw EvaluationException.create(
					MSG_MAX_RANGE_VALUE_IS_NOT_VALID_LONG, maximum);
		}

		// Sanity checking.
		checkRangeValues(minValue, maxValue);

		// All OK. Set the field values.
		min = minValue;
		max = maxValue;
	}

	/**
	 * Constructs an inclusive Range from the two values given. It will throw an
	 * IllegalArgumentException if the minimum value is less than zero or the
	 * minimum value is greater than the maximum. Use Long.MAX_VALUE for the
	 * maximum if you want an unbounded range.
	 * 
	 * @param minimum
	 * @param maximum
	 */
	public Range(long minimum, long maximum) {
		checkRangeValues(minimum, maximum);
		min = minimum;
		max = maximum;
	}

	/**
	 * Test whether the given long value is within this range.
	 * 
	 * @param test
	 */
	public boolean isInRange(long test) {
		return (test >= min && test <= max);
	}

	/**
	 * Test whether the given double value is within this range. *
	 * 
	 * @param test
	 */
	public boolean isInRange(double test) {
		return (test >= ((double) min) && test <= ((double) max));
	}

	/**
	 * Provide a formatted String describing this Range.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Long.toString(min));
		sb.append(".."); //$NON-NLS-1$
		if (max != Long.MAX_VALUE) {
			sb.append(max);
		}
		return sb.toString();
	}

	/**
	 * Checks whether the range values are valid. Specifically whether the lower
	 * index is non-negative and the minimum is less than or equal to the
	 * maximum. This will throw an EvaluationException if any problems are
	 * found.
	 * 
	 * @param minimum
	 * @param maximum
	 */
	private void checkRangeValues(long minimum, long maximum) {
		if (minimum < 0) {
			throw EvaluationException.create(
					MSG_MIN_RANGE_VALUE_CANNOT_BE_NEGATIVE, minimum);
		}
		if (minimum > maximum) {
			throw EvaluationException.create(
					MSG_MIN_MUST_BE_LESS_OR_EQUAL_TO_MAX, minimum, maximum);
		}
	}

}
