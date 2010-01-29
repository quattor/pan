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

package org.quattor.ant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.quattor.pan.repository.SourceFile.Type;

public class DependencyCheckerTest {

	@Test
	public void ensureExtensionStrippingWorks() {
		String name = "ok/";
		for (Type type : Type.values()) {

			if (type.isSource()) {
				String extendedName = name + type.getExtension();
				String strippedName = DependencyChecker
						.stripPanExtensions(extendedName);
				assertEquals(name, strippedName);
			}
		}
	}

	@Test
	public void checkFileComparison() {

		File file1 = new File("/first/file.txt");
		File file2 = new File("/second/file.txt");
		File file3 = new File("/first/file.txt");

		assertTrue(DependencyChecker.isSingleDependencyDifferent(file1, file2));
		assertFalse(DependencyChecker.isSingleDependencyDifferent(file1, file3));

		// This is a special case for the algorithm.
		assertFalse(DependencyChecker.isSingleDependencyDifferent(file1, null));
	}

	@Test
	public void checkDependencyReconstruction() {
		String schema = "file:";
		String templatePath = "/alpha/beta/gamma/";
		String namespace = "delta";
		String name = "name";
		String tplName = namespace + "/" + name;

		String expectedPath = templatePath + namespace;

		for (Type type : Type.values()) {

			File file = DependencyChecker.reconstructSingleDependency(schema
					+ templatePath, tplName, type);

			String reconName = file.getName();
			String reconPath = file.getParent();

			String expectedName = name + type.getExtension();

			assertEquals(expectedName, reconName);
			assertEquals(expectedPath, reconPath);

		}
	}
}
