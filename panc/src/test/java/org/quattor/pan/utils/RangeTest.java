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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/RangeTest.java $
 $Id: RangeTest.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.exceptions.EvaluationException;

public class RangeTest {

	@Test
	public void testValidRange() {

		long minimum = 10;
		long maximum = 20;

		Range range = new Range(minimum, maximum);

		internalRangeCheck(minimum, maximum, range);
	}

	@Test
	public void testValidStringRanges() {

		Range range = null;
		long minimum, maximum;
		String strmin, strmax;

		strmin = "0xff";
		strmax = "0xcaff";

		minimum = Long.decode(strmin);
		maximum = Long.decode(strmax);

		range = new Range(strmin, strmax);

		internalRangeCheck(minimum, maximum, range);

		strmin = "0157";
		strmax = "0733";

		minimum = Long.decode(strmin);
		maximum = Long.decode(strmax);

		// Verify that creating the range is OK.
		range = new Range(strmin, strmax);

		// Run the tests.
		internalRangeCheck(minimum, maximum, range);

		// Run the tests.
		internalRangeCheck(minimum, maximum, range);

		strmin = null;
		strmax = null;

		minimum = 0L;
		maximum = Long.MAX_VALUE;

		// Verify that creating the range is OK.
		range = new Range(strmin, strmax);

		// Run the tests.
		internalRangeCheck(minimum, maximum, range);

		strmin = "-1";
		strmax = "10";

		minimum = -1;
		maximum = 10;

		// Verify that creating the range is OK.
		range = new Range(strmin, strmax);

		// Run the tests.
		internalRangeCheck(minimum, maximum, range);

		strmin = "-10";
		strmax = "-4";

		minimum = -10;
		maximum = -4;

		// Verify that creating the range is OK.
		range = new Range(strmin, strmax);

		// Run the tests.
		internalRangeCheck(minimum, maximum, range);
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidRange2() {
		new Range(10, 5);
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidStringRange1() {
		new Range("0xfg", "100");
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidStringRange2() {
		new Range("10", "099");
	}

	private void internalRangeCheck(long minimum, long maximum, Range range) {

		// Verify that minimum, maximum, and value between do not throw
		// exceptions.
		assertTrue(range.isInRange(minimum));
		assertTrue(range.isInRange((double) minimum));
		assertTrue(range.isInRange(maximum));
		assertTrue(range.isInRange((double) maximum));
		long middle = (minimum + maximum) / 2L;
		assertTrue(range.isInRange(middle));
		assertTrue(range.isInRange((double) middle));

		// Check that values outside of the range do throw exceptions.
		if (minimum != Long.MIN_VALUE) {
			assertFalse(range.isInRange(minimum - 1));
			assertFalse(range.isInRange((double) (minimum - 1)));
		}
		if (maximum != Long.MAX_VALUE) {
			assertFalse(range.isInRange(maximum + 1));
			assertFalse(range.isInRange((double) (maximum + 1)));
		}

	}

}
