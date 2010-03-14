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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/CompilerTest.java $
 $Id: CompilerTest.java 1450 2007-03-09 17:17:08Z loomis $
 */

package org.quattor.pan.utils;

import static org.junit.Assert.fail;
import static org.quattor.pan.utils.MessageUtils.getMessageString;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Test;

public class MessageUtilsTest {

	@Test(expected = AssertionError.class)
	public void testNullArgument() {
		getMessageString(null);
	}

	@Test
	public void testAllMessagesKeys() throws IllegalAccessException {

		// Loop over all of the static String fields in MessageUtils and ensure
		// that each has a value identical to name of field.
		Class<MessageUtils> muclass = MessageUtils.class;
		for (Field f : muclass.getFields()) {
			if (f.getType() == String.class
					&& Modifier.isStatic(f.getModifiers())) {

				String msgKey = (String) f.get(null);
				String fieldName = f.getName();
				if (!fieldName.equals(msgKey)) {
					fail("message field named '" + fieldName
							+ "' does not have value identical to name");
				}
			}

		}
	}

	@Test
	public void testAllMessagesDefined() throws IllegalAccessException {

		// Loop over all of the static String fields in MessageUtils and ensure
		// that each has a defined message in the bundle.
		Class<MessageUtils> muclass = MessageUtils.class;
		for (Field f : muclass.getFields()) {
			if (f.getType() == String.class
					&& Modifier.isStatic(f.getModifiers())) {

				String msgKey = (String) f.get(null);
				if (getMessageString(msgKey) == null) {
					fail("message for key '" + msgKey
							+ "' is not defined in bundle");
				}
			}

		}
	}

}
