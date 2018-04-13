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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Base64DecodeTest.java $
 $Id: Base64DecodeTest.java 1042 2006-11-28 10:04:35Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.quattor.pan.dml.AbstractOperationTestUtils;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.ttemplate.SourceRange;

public class BuiltInFunctionTestUtils extends AbstractOperationTestUtils {

	/**
	 * Verify that the classes implementing built-in functions pass the tests
	 * regarding the factory methods and constructors.
	 * 
	 * @param c
	 *            Class to check against Built-In function requirements
	 */
	public static void checkClassRequirements(Class<?> c) {
		checkGetInstanceExists(c);
		checkFinalOrAbstractClass(c);
		checkConstructorAccess(c);
	}

	/**
	 * Checks that the given class has a method with the signature
	 * getInstance(Operation...). This ensures that all of the built-in
	 * functions properly follow the factory pattern. If the given class does
	 * not have the expected method, fail() will be called to abort the testing
	 * phase.
	 * 
	 * @param c
	 *            Class to check that factory method exists and has correct form
	 */
	public static void checkGetInstanceExists(Class<?> c) {

		if (c != null) {
			try {

				// Find the appropriate method and collect the modifiers.
				Method m = c.getDeclaredMethod("getInstance",
						SourceRange.class, Operation[].class);
				int modifiers = m.getModifiers();

				// Check that the factory method is static.
				if (!Modifier.isStatic(modifiers)) {
					fail("getInstance(sourceRange, operation, ...) in class "
							+ c.getSimpleName() + " must be static");
				}

				// Check that the factory method is public.
				if (!Modifier.isPublic(modifiers)) {
					fail("getInstance(sourceRange, operation, ...) in class "
							+ c.getSimpleName() + " must be public");
				}

				// Check the return type. It must be something that implements
				// the Operation interface.
				Class<?> rtype = m.getReturnType();
				if (!Operation.class.isAssignableFrom(rtype)) {
					fail("getInstance(sourceRange, operation, ...) in class "
							+ c.getSimpleName() + " return an Operation");
				}

			} catch (NoSuchMethodException nsme) {
				fail("getInstance(sourceRange, operation, ...) does not exist in class "
						+ c.getSimpleName());
			}
		} else {
			fail("checkGetInstanceExists() cannot be called with null value");
		}
	}

	/**
	 * Check that the given class is actually marked as final or abstract.
	 * 
	 * @param c
	 *            Class to check is marked final or abstract
	 */
	public static void checkFinalOrAbstractClass(Class<?> c) {

		if (c != null) {
			int mod = c.getModifiers();
			if (!Modifier.isFinal(mod) && !Modifier.isAbstract(mod)) {
				fail("class " + c.getSimpleName()
						+ " must be marked as final or abstract");
			}
		} else {
			fail("checkFinalClass() cannot be called with null value");
		}
	}

	/**
	 * Check that all of the constructors are marked as private or protected.
	 * Instances must be created from the factory method and there can be no
	 * subclasses of a final method.
	 * 
	 * @param c
	 *            Class to check for private/protected constructors
	 */
	public static void checkConstructorAccess(Class<?> c) {

		if (c != null) {

			int cmods = c.getModifiers();
			if (Modifier.isFinal(cmods)) {
				for (Constructor<?> constructor : c.getConstructors()) {
					if (!Modifier.isPrivate(constructor.getModifiers())) {
						fail("all constructors for class " + c.getSimpleName()
								+ " must be private");
					}
				}
			} else if (Modifier.isAbstract(cmods)) {
				for (Constructor<?> constructor : c.getConstructors()) {
					if (!Modifier.isProtected(constructor.getModifiers())) {
						fail("all constructors for class " + c.getSimpleName()
								+ " must be protected");
					}
				}
			}
		} else {
			fail("checkPrivateConstructors() cannot be called with null value");
		}

	}
}
