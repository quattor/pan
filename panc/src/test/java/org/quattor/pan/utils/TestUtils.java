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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.quattor.pan.output.Formatter;
import org.quattor.pan.output.FormatterUtils;
import org.quattor.pan.output.PanFormatter;

public class TestUtils {

	private static final Pattern dependencyPattern = Pattern
			.compile("#\\s*dep:\\s*([\\w\\.]+)\\s*");

	private static final Pattern exceptionPattern = Pattern
			.compile("#\\s*@expect\\s*=\\s*([\\w\\.]+)(?:\\s*\"(.*)\")?\\s*");

	private static final Pattern xpathPattern = Pattern
			.compile("#\\s*@expect\\s*=\\s*\"(.*)\"\\s*");

	private static final Pattern formatterPattern = Pattern
			.compile("#\\s*@format\\s*=\\s*([\\w\\.]+)\\s*");

	private static Pattern depline = Pattern.compile("(.*)\\s+(.*)\\s+(.*)");

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

		if (!tmpdir.endsWith("/")) {
			tmpdir = tmpdir + "/";
		}

		File tmpfile = new File(tmpdir);
		if (!tmpfile.exists()) {
			tmpfile.mkdirs();
		}
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
			fail("panc.testdir (" + testfile.toString()
					+ ") must be an absolute path");
		}
		if (!testfile.isDirectory()) {
			fail("panc.testdir (" + testfile.toString()
					+ ") must be an existing directory");
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
	 * @expect=org.quattor.pan.exception.SyntaxException "regex"
	 * 
	 *                                                   The regex is optional.
	 * 
	 * @expect="/profile/result=1"
	 * 
	 *                             The value is the expected XPath value that
	 *                             must be true for the output.
	 * 
	 * @param objtpl
	 *            object template to analyze
	 * 
	 * @return either a ExceptionChecker if an exception is expected, an XPath
	 *         expression for a valid file, or a String with a message on error
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
					result = new ExceptionChecker(m.group(1), m.group(2));
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
	 * Extract the set of dependencies from the source file. The source file
	 * should have comments like the following to indicate the expected
	 * dependencies:
	 * 
	 * # dep: mytemplate
	 * 
	 * @param objtpl
	 *            object template to analyze
	 * 
	 * @return set of expected dependencies
	 * 
	 * @throws FileNotFoundException
	 *             if the object file does not exist
	 * @throws IOException
	 *             if there is a problem reading the object file
	 */
	static public Set<String> extractDependencies(File objtpl) {

		Set<String> dependencies = new TreeSet<String>();

		// Read the object template line-by-line looking for the expected
		// outcome.
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(objtpl));
			for (String line = in.readLine(); line != null; line = in
					.readLine()) {

				Matcher m = dependencyPattern.matcher(line);
				if (m.matches()) {
					String dependency = m.group(1);
					dependencies.add(dependency);
				}
			}
		} catch (FileNotFoundException fnfe) {
			dependencies.clear();
		} catch (IOException ioe) {
			dependencies.clear();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException consumed) {
				}
			}
		}

		return dependencies;
	}

	/**
	 * Extract the set of dependencies from the generated dependency file.
	 * 
	 * @param depfile
	 *            dependency file to analyze
	 * 
	 * @return set of generated dependencies
	 * 
	 * @throws FileNotFoundException
	 *             if the object file does not exist
	 * @throws IOException
	 *             if there is a problem reading the object file
	 */
	static public Set<String> extractGeneratedDependencies(File depfile) {

		Set<String> dependencies = new TreeSet<String>();

		// Read the object template line-by-line looking for the expected
		// outcome.
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(depfile));
			for (String line = in.readLine(); line != null; line = in
					.readLine()) {

				Matcher m = depline.matcher(line);
				if (m.matches()) {
					String dependency = m.group(1);
					dependencies.add(dependency);
				}
			}
		} catch (FileNotFoundException fnfe) {
			dependencies.clear();
		} catch (IOException ioe) {
			dependencies.clear();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException consumed) {
				}
			}
		}

		return dependencies;
	}

	/**
	 * Extract the formatter to use for this compilation.
	 * 
	 * @format=pan If no directive is found or if the formatter is not known,
	 *             then pan format will be used.
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
			result = PanFormatter.getInstance();
		}

		return result;
	}

	/**
	 * Extract the format string to use for this compilation.
	 * 
	 * @format=pan If no directive is found or if the formatter is not known,
	 *             then pan format will be used.
	 * 
	 * @param objtpl
	 *            object template to analyze
	 * 
	 * @return a string giving the formatter to use
	 */
	static public String extractFormatName(File objtpl) {

		String result = "pan";

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
			// Do nothing. Return the pan formatter.
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
							|| (f.isFile() && f.getName().endsWith(".pan"))) {
						tests.add(f);
					}
				}
			}
		} else {
			fail("collectTests requires a directory");
		}
		return tests;
	}

	/**
	 * Ensure that the given file exists and has the current time as the
	 * modification time.
	 * 
	 * @param dir
	 *            name of the directory to use; this must already exist
	 * @param name
	 *            name of the file relative to the given directory
	 * @returns the file that has had it's modification time changed (created if
	 *          necessary)
	 */
	static public File touch(File dir, String name) throws IOException {

		// Ensure that the directory argument exists and is a directory.
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new RuntimeException("could not create directory " + dir);
			}
		}
		if (!dir.isDirectory()) {
			fail(dir + " is not a directory");
		}

		// Location of the file to create/update.
		File file = new File(dir, name);

		// Create file if necessary and update modification time.
		if (!file.exists()) {
			OutputStream os = new FileOutputStream(file);
			os.close();
		}
		if (!file.setLastModified((new Date()).getTime())) {
			throw new RuntimeException("could not update timestamp for " + file);
		}
		return file;
	}

	/**
	 * Ensure that the given relative file exists and has the current time as
	 * the modification time. The files will be created in a subdirectory named
	 * "rpath" of the argument dir.
	 * 
	 * @param dir
	 *            name of the directory to use; this must already exist
	 * @param name
	 *            name of the file relative to the given directory
	 * @returns the file that has had it's modification time changed (created if
	 *          necessary)
	 */
	static public File rtouch(File dir, String name) throws IOException {
		File rdir = new File(dir, "rpath");
		return touch(rdir, name);
	}

	/**
	 * Recursively delete all of the files rooted at the given directory. If the
	 * argument is a file it is deleted. If the File corresponding to the
	 * argument does not exist, nothing is done. The argument may not be null.
	 * 
	 * @param root
	 */
	static public void recursiveFileDelete(File root) {
		if (root.exists()) {
			if (root.isDirectory()) {
				for (File f : root.listFiles()) {
					recursiveFileDelete(f);
				}
			}
			if (!root.delete()) {
				throw new RuntimeException("could not delete file " + root);
			}
		}
	}

	public static class ExceptionChecker {

		public final Class<?> exceptionClass;

		public final Pattern messagePattern;

		public ExceptionChecker(String className, String regex) {

			String exceptionName = className;
			try {
				exceptionClass = Class.forName(exceptionName);
			} catch (ClassNotFoundException cnfe) {
				throw new RuntimeException("exception class (" + exceptionName
						+ ") not found");
			}

			if (regex != null) {
				try {
					messagePattern = Pattern.compile(regex, Pattern.DOTALL);
				} catch (Exception e) {
					throw new RuntimeException("invalid pattern: "
							+ e.getMessage());
				}
			} else {
				messagePattern = Pattern.compile(".*", Pattern.DOTALL);
			}

		}

		public String getExceptionName() {
			return exceptionClass.getSimpleName();
		}

		public String check(Set<Throwable> exceptions) {
			for (Throwable t : exceptions) {
				try {
					exceptionClass.cast(t);
				} catch (ClassCastException cce) {
					t.printStackTrace();
					return ("expected " + getExceptionName() + " but got " + t
							.getClass().getSimpleName());
				}

				String message = t.getMessage();
				Matcher m = messagePattern.matcher(message);
				if (!m.matches()) {
					t.printStackTrace();
					return ("error message: does not match expected pattern\nPATTERN: "
							+ messagePattern.toString() + "\nMESSAGE:\n" + message);
				}
			}
			return null;
		}
	}

}
