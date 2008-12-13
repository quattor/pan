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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/template/ObjectContextTest.java $
 $Id: ObjectContextTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.template;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.utils.TermFactory;

public class LocalVariableMapTest {

	@Test
	public void checkNullForUndefinedVariable() {
		LocalVariableMap map = new LocalVariableMap();
		assertTrue(map.get("DUMMY") == null);
	}

	@Test
	public void checkArgvAndArgc() throws InvalidTermException {

		// Setup an argument list with two string values.
		StringProperty value = StringProperty.getInstance("DUMMY");
		ListResource list = new ListResource();
		list.put(TermFactory.create(0L), value);
		list.put(TermFactory.create(1L), value);

		// Create the map.
		LocalVariableMap map = new LocalVariableMap(list);

		// Ensure that ARGC is set and has a value of 2.
		Element argc = map.get("ARGC");
		if (argc != null) {
			if (argc instanceof LongProperty) {
				long size = ((LongProperty) argc).getValue();
				assertTrue(size == 2L);
			} else {
				fail("ARGC was not a LongProperty; it was a "
						+ argc.getTypeAsString());
			}

		} else {
			fail("ARGC was undefined");
		}
	}

	@Test
	public void checkSimpleAssignment() {

		// Setup an argument list with two string values.
		StringProperty value1 = StringProperty.getInstance("DUMMY1");
		StringProperty value2 = StringProperty.getInstance("DUMMY2");

		// Create the map.
		LocalVariableMap map = new LocalVariableMap();

		// Set the value.
		Element oldValue = map.put("x", value1);

		// Check that there was no old value and that the value is exactly the
		// same as the one we gave.
		assertTrue(oldValue == null);
		assertTrue(map.get("x") == value1);

		// Set a new value.
		oldValue = map.put("x", value2);

		// Check that the old value was the same as the last one and the new
		// value is correct.
		assertTrue(oldValue == value1);
		assertTrue(map.get("x") == value2);

		// Remove the value. Check the old value and ensure variable no longer
		// exists.
		oldValue = map.put("x", null);
		assertTrue(oldValue == value2);
		assertTrue(map.get("x") == null);
	}

	@Test(expected = EvaluationException.class)
	public void checkInvalidAssignment() {

		// Setup an argument list with two string values.
		StringProperty value1 = StringProperty.getInstance("DUMMY1");
		LongProperty value2 = LongProperty.getInstance(0L);

		// Create the map.
		LocalVariableMap map = new LocalVariableMap();

		// Set the first value.
		Element oldValue = map.put("x", value1);

		// Check that there was no old value and that the value is exactly the
		// same as the one we gave.
		assertTrue(oldValue == null);
		assertTrue(map.get("x") == value1);

		// Try to set to an invalid replacement. (A long cannot replace a
		// string.) This should throw an exception.
		map.put("x", value2);
	}
}
