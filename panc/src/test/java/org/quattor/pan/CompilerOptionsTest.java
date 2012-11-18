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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/CompilerOptionsTest.java $
 $Id: CompilerOptionsTest.java 3937 2008-11-22 10:31:49Z loomis $
 */

package org.quattor.pan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.output.DepFormatter;
import org.quattor.pan.output.Formatter;
import org.quattor.pan.output.PanFormatter;

public class CompilerOptionsTest {

	@Test
	public void testConstructorAndGetters1() throws SyntaxException {

		int iterationLimit = 1001;
		int callDepthLimit = 101;
		File outputDirectory = new File(System.getProperty("user.dir"));
		File sessionDirectory = new File(System.getProperty("user.dir"));
		List<File> includeDirectories = new LinkedList<File>();
		includeDirectories.add(outputDirectory);
		includeDirectories.add(sessionDirectory);

		Formatter formatter = PanFormatter.getInstance();

		Set<Formatter> formatters = new HashSet<Formatter>();
		formatters.add(formatter);

		CompilerOptions options = new CompilerOptions(null, null,
				iterationLimit, callDepthLimit, formatters, outputDirectory,
				sessionDirectory, includeDirectories, CompilerOptions.DeprecationWarnings.ON, null,
				null, null);

		assertTrue(iterationLimit == options.maxIteration);
		assertTrue(callDepthLimit == options.maxRecursion);

		assertTrue(options.formatters.contains(formatter));

		assertTrue(outputDirectory.equals(options.outputDirectory));
	}

	@Test
	public void testConstructorAndGetters2() throws SyntaxException {

		int iterationLimit = 2002;
		int callDepthLimit = 202;
		Formatter formatter = DepFormatter.getInstance();
		File outputDirectory = new File(System.getProperty("user.dir"));
		File sessionDirectory = null;
		List<File> includeDirectories = new LinkedList<File>();
		includeDirectories.add(outputDirectory);

		Set<Formatter> formatters = new HashSet<Formatter>();
		formatters.add(formatter);

		CompilerOptions options = new CompilerOptions(null, null,
				iterationLimit, callDepthLimit, formatters, outputDirectory,
				sessionDirectory, includeDirectories, CompilerOptions.DeprecationWarnings.ON, null,
				null, null);

		assertTrue(iterationLimit == options.maxIteration);
		assertTrue(callDepthLimit == options.maxRecursion);

		assertTrue(options.formatters.contains(formatter));

		assertTrue(outputDirectory.equals(options.outputDirectory));
	}

	@Test
	public void checkUnlimitedValues() throws SyntaxException {

		int iterationLimit = -1;
		int callDepthLimit = 0;
		Formatter formatter = PanFormatter.getInstance();
		File outputDirectory = new File(System.getProperty("user.dir"));
		File sessionDirectory = new File(System.getProperty("user.dir"));
		List<File> includeDirectories = new LinkedList<File>();
		includeDirectories.add(outputDirectory);
		includeDirectories.add(sessionDirectory);

		Set<Formatter> formatters = new HashSet<Formatter>();
		formatters.add(formatter);

		CompilerOptions options = new CompilerOptions(null, null,
				iterationLimit, callDepthLimit, formatters, outputDirectory,
				sessionDirectory, includeDirectories, CompilerOptions.DeprecationWarnings.ON, null,
				null, null);

		assertTrue(Integer.MAX_VALUE == options.maxIteration);
		assertTrue(Integer.MAX_VALUE == options.maxRecursion);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkBadOutputDirectory() throws SyntaxException {

		int iterationLimit = -1;
		int callDepthLimit = 0;
		Formatter formatter = PanFormatter.getInstance();
		File outputDirectory = new File("/xxxyyy");
		File sessionDirectory = new File(System.getProperty("user.dir"));
		List<File> includeDirectories = new LinkedList<File>();
		includeDirectories.add(outputDirectory);
		includeDirectories.add(sessionDirectory);

		Set<Formatter> formatters = new HashSet<Formatter>();
		formatters.add(formatter);

		new CompilerOptions(null, null, iterationLimit, callDepthLimit,
				formatters, outputDirectory, sessionDirectory,
				includeDirectories, CompilerOptions.DeprecationWarnings.ON, null,
				null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkBadSessionDirectory() throws SyntaxException {

		int iterationLimit = -1;
		int callDepthLimit = 0;
		Formatter formatter = PanFormatter.getInstance();
		File outputDirectory = new File(System.getProperty("user.dir"));
		File sessionDirectory = new File("/xxxyyy");
		List<File> includeDirectories = new LinkedList<File>();
		includeDirectories.add(outputDirectory);
		includeDirectories.add(sessionDirectory);

		Set<Formatter> formatters = new HashSet<Formatter>();
		formatters.add(formatter);

		new CompilerOptions(null, null, iterationLimit, callDepthLimit,
				formatters, outputDirectory, sessionDirectory,
				includeDirectories, CompilerOptions.DeprecationWarnings.ON, null,
				null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkIncludeDirectory() throws SyntaxException {

		int iterationLimit = -1;
		int callDepthLimit = 0;
		Formatter formatter = PanFormatter.getInstance();
		File outputDirectory = new File(System.getProperty("user.dir"));
		File sessionDirectory = new File(System.getProperty("user.dir"));
		List<File> includeDirectories = new LinkedList<File>();
		includeDirectories.add(new File("/xxxyyy"));

		Set<Formatter> formatters = new HashSet<Formatter>();
		formatters.add(formatter);

		new CompilerOptions(null, null, iterationLimit, callDepthLimit,
				formatters, outputDirectory, sessionDirectory,
				includeDirectories, CompilerOptions.DeprecationWarnings.ON, null,
				null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkMissingOutputDirectory1() throws SyntaxException {

		int iterationLimit = -1;
		int callDepthLimit = 0;
		Formatter formatter = null;
		File outputDirectory = null;
		File sessionDirectory = new File(System.getProperty("user.dir"));
		List<File> includeDirectories = new LinkedList<File>();
		includeDirectories.add(outputDirectory);
		includeDirectories.add(sessionDirectory);

		Set<Formatter> formatters = new HashSet<Formatter>();
		formatters.add(formatter);

		new CompilerOptions(null, null, iterationLimit, callDepthLimit,
				formatters, outputDirectory, sessionDirectory,
				includeDirectories, CompilerOptions.DeprecationWarnings.ON, null,
				null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkMissingOutputDirectory2() throws SyntaxException {

		int iterationLimit = -1;
		int callDepthLimit = 0;
		Formatter formatter = DepFormatter.getInstance();
		File outputDirectory = null;
		File sessionDirectory = new File(System.getProperty("user.dir"));
		List<File> includeDirectories = new LinkedList<File>();

		Set<Formatter> formatters = new HashSet<Formatter>();
		formatters.add(formatter);

		new CompilerOptions(null, null, iterationLimit, callDepthLimit,
				formatters, outputDirectory, sessionDirectory,
				includeDirectories, CompilerOptions.DeprecationWarnings.ON, null,
				null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkNullIncludeDirectory() throws SyntaxException {

		int iterationLimit = -1;
		int callDepthLimit = 0;
		Formatter formatter = DepFormatter.getInstance();
		File outputDirectory = null;
		File sessionDirectory = new File(System.getProperty("user.dir"));
		List<File> includeDirectories = new LinkedList<File>();
		includeDirectories.add(null);

		Set<Formatter> formatters = new HashSet<Formatter>();
		formatters.add(formatter);

		new CompilerOptions(null, null, iterationLimit, callDepthLimit,
				formatters, outputDirectory, sessionDirectory,
				includeDirectories, CompilerOptions.DeprecationWarnings.ON, null,
				null, null);
	}

	@Test
	public void checkNullAndEmptyInputForRootElement() throws SyntaxException {
		String[] inputs = new String[] { null, "", " ", "\t" };
		for (String input : inputs) {
			Element rootElement = CompilerOptions.createRootElement(input);
			assertTrue(rootElement.isNlist());
			HashResource hash = (HashResource) rootElement;
			assertTrue(hash.size() == 0);
		}
	}

	@Test
	public void checkCorrectRootElement() throws SyntaxException {
		Map<String, Integer> inputs = new HashMap<String, Integer>();
		inputs.put("nlist()", 0);
		inputs.put("nlist('a', 1)", 1);
		inputs.put("nlist('a', nlist('b', 1))", 1);
		inputs.put("nlist('a', 1, 'b', 2)", 2);
		inputs.put("nlist('a', nlist(), 'b', nlist('c', 2))", 2);
		for (Map.Entry<String, Integer> entry : inputs.entrySet()) {
			String dml = entry.getKey();
			int value = entry.getValue();

			Element rootElement = CompilerOptions.createRootElement(dml);
			assertTrue(rootElement.isNlist());

			HashResource hash = (HashResource) rootElement;
			assertEquals(value, hash.size());
		}
	}

}