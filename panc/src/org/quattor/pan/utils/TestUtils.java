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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/TestUtils.java $
 $Id: TestUtils.java 2986 2008-03-01 10:10:06Z loomis $
 */

package org.quattor.pan.utils;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.quattor.pan.output.Formatter;
import org.quattor.pan.output.FormatterUtils;
import org.quattor.pan.output.XmlDBFormatter;

public class TestUtils {

	private static final Pattern exceptionPattern = Pattern
			.compile("#\\s*@expect\\s*=\\s*([\\w\\.]+)\\s*");

	private static final Pattern xpathPattern = Pattern
			.compile("#\\s*@expect\\s*=\\s*\"(.*)\"\\s*");

	private static final Pattern formatterPattern = Pattern
			.compile("#\\s*@format\\s*=\\s*([\\w\\.]+)\\s*");

	private static final XPath xpath = XPathFactory.newInstance().newXPath();

	/**
	 * The location of the directory to write temporary test files to is passed
	 * to the JUnit task through a system property named "panc.tmpdir". Recover
	 * this information and return a File object corresponding to this
	 * directory.
	 * 
	 * @return File object representing directory for temporary test files
	 */
	static public File getTmpdir() {

		String tmpdir = System.getProperty("panc.tmpdir");
		if (tmpdir == null) {
			fail("panc.tmpdir property must be defined");
		}

		File tmpfile = new File(tmpdir);
		if (!tmpfile.isAbsolute()) {
			fail("panc.tmpdir must be an absolute path");
		}
		if (!tmpfile.isDirectory()) {
			fail("panc.tmpdir must be an existing directory");
		}

		return tmpfile;
	}

	/**
	 * The location of the directory where to find the template files for full
	 * unit tests. This location is passed to the JUnit task through a system
	 * property named "panc.testdir". Recover this information and return a File
	 * object corresponding to this directory.
	 * 
	 * @return File object representing the top directory containing test files
	 */
	static public File getTestdir() {

		String testdir = System.getProperty("panc.testdir");
		if (testdir == null) {
			fail("panc.testdir property must be defined");
		}

		File testfile = new File(testdir);
		if (!testfile.isAbsolute()) {
			fail("panc.testdir must be an absolute path");
		}
		if (!testfile.isDirectory()) {
			fail("panc.testdir must be an existing directory");
		}

		return testfile;
	}

	/**
	 * The location of the script for invoking panc from the command line. This
	 * location is passed to the JUnit task through a system property named
	 * "panc.script". Recover this information and return a File object
	 * corresponding to this directory.
	 * 
	 * @return File object giving location of panc script
	 */
	static public File getPancScript() {

		String script = System.getProperty("panc.script");
		if (script == null) {
			fail("panc.script property must be defined");
		}

		File scriptfile = new File(script);
		if (!scriptfile.isAbsolute()) {
			fail("panc.script must be an absolute path");
		}
		if (scriptfile.isDirectory()) {
			fail("panc.script must be an ordinary file");
		}

		return scriptfile;
	}

	/**
	 * Extract the expected result from an object template. This expectation
	 * should be embedded into the object template in a comment of the form:
	 * 
	 * @expect=org.quattor.pan.exception.SyntaxException
	 * 
	 * or
	 * 
	 * @expect="/profile/result=1"
	 * 
	 * for an expected exception or XPath value, respectively.
	 * @param objtpl
	 *            object template to analyze
	 * 
	 * @return either a Class if an exception is expected, an XPath expression
	 *         for a valid file, or a String with a message on error
	 */
	static public Object extractExpectation(File objtpl) {

		Object result = null;

		// Read the object template line-by-line looking for the expected
		// outcome.
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(objtpl));
			for (String line = in.readLine(); line != null; line = in
					.readLine()) {

				Matcher m = exceptionPattern.matcher(line);
				if (m.matches()) {
					String exceptionName = m.group(1);
					try {
						result = Class.forName(exceptionName);
					} catch (ClassNotFoundException cnfe) {
						return ("exception class (" + exceptionName + ") not found");
					}
				}

				m = xpathPattern.matcher(line);
				if (m.matches()) {
					String xp = m.group(1);
					try {
						result = xpath.compile(xp);
					} catch (XPathExpressionException xee) {
						return ("invalid xpath expression " + xp);
					}
				}
			}
		} catch (FileNotFoundException fnfe) {
			return ("object template not found");
		} catch (IOException ioe) {
			return ("IO exception reading object template");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException consumed) {
				}
			}
		}

		if (result == null) {
			return "@expect not found in file";
		} else {
			return result;
		}
	}

	/**
	 * Extract the formatter to use for this compilation.
	 * 
	 * @format=pan
	 * 
	 * If no directive is found or if the formatter is not known, then xmldb
	 * format will be used.
	 * 
	 * @param objtpl
	 *            object template to analyze
	 * 
	 * @return a string giving the formatter to use
	 */
	static public Formatter extractFormatter(File objtpl) {

		String formatterName = extractFormatName(objtpl);
		Formatter result = FormatterUtils.getFormatterInstance(formatterName);
		if (result == null) {
			result = XmlDBFormatter.getInstance();
		}

		return result;
	}

	/**
	 * Extract the format string to use for this compilation.
	 * 
	 * @format=pan
	 * 
	 * If no directive is found or if the formatter is not known, then xmldb
	 * format will be used.
	 * 
	 * @param objtpl
	 *            object template to analyze
	 * 
	 * @return a string giving the formatter to use (must be xmldb, pan, or
	 *         txt).
	 */
	static public String extractFormatName(File objtpl) {

		String result = "xmldb";

		// Read the object template line-by-line looking for the format tag.
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(objtpl));
			for (String line = in.readLine(); line != null; line = in
					.readLine()) {

				Matcher m = formatterPattern.matcher(line);
				if (m.matches()) {
					result = m.group(1);
				}

			}
		} catch (IOException consumed) {
			// Do nothing. Return the XMLDB formatter.
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException consumed) {
				}
			}
		}

		return result;
	}

	// Collect all of the directories within a given directory. Any hidden
	// directories are ignored.
	static public TreeSet<File> collectDirectories(File parent) {

		TreeSet<File> dirs = new TreeSet<File>();

		if (parent.isDirectory()) {
			for (File d : parent.listFiles()) {
				if (d.isDirectory()) {
					if (!d.isHidden()) {
						dirs.add(d);
					}
				}
			}
		} else {
			fail("collectDirectories requires a directory");
		}
		return dirs;
	}

	// This method will collect directories or individual templates within a
	// given parent directory.
	static public TreeSet<File> collectTests(File parent) {

		TreeSet<File> tests = new TreeSet<File>();

		if (parent.isDirectory()) {
			for (File f : parent.listFiles()) {
				if (!f.isHidden()) {
					if (f.isDirectory()
							|| (f.isFile() && f.getName().endsWith(".tpl"))) {
						tests.add(f);
					}
				}
			}
		} else {
			fail("collectTests requires a directory");
		}
		return tests;
	}

}
