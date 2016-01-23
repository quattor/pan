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

package org.quattor.pan;

import static org.junit.Assert.fail;
import static org.quattor.pan.utils.TestUtils.collectDirectories;
import static org.quattor.pan.utils.TestUtils.collectTests;
import static org.quattor.pan.utils.TestUtils.extractExpectation;
import static org.quattor.pan.utils.TestUtils.extractFormatName;
import static org.quattor.pan.utils.TestUtils.getPancScript;
import static org.quattor.pan.utils.TestUtils.getTestdir;
import static org.quattor.pan.utils.TestUtils.getTmpdir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.tools.ant.BuildException;
import org.junit.Test;
import org.xml.sax.InputSource;

public class ScriptCompilerTest {

	protected Object runCompilerViaScript(File tplfile, File dir, String format) {

		Object value = null;
		String panc = getPancScript().getAbsolutePath();

		ProcessBuilder pb = new ProcessBuilder(panc, "-x", format, "-O",
				getTmpdir().getAbsolutePath(), "-I", dir.getAbsolutePath(),
				tplfile.getAbsolutePath());

		try {
			Process process = pb.start();
			value = Integer.valueOf(process.waitFor());
		} catch (IOException ioe) {
			value = "IO exception while running process: " + ioe.getMessage();
		} catch (InterruptedException ie) {
			value = "process interrupted";
		}

		return value;

	}

	@Test
	public void scriptFunctionalTests() {

		// Locate the directory with the functionality tests and extract all of
		// the children.
		File root = new File(getTestdir(), "Functionality");
		TreeSet<File> testdirs = collectDirectories(root);

		// Create a list to hold all of the errors.
		List<String> errors = new LinkedList<String>();

		// Loop over all of the children and treat each of those in turn.
		for (File dir : testdirs) {

			TreeSet<File> tests = collectTests(dir);

			for (File t : tests) {

				File tpl = null;
				File rootdir = null;

				// Set the root directory and name of the template. In the case
				// of a directory, the main test template is expected to have
				// the same name as the directory with ".pan" appended.
				if (t.isFile()) {
					tpl = t;
					rootdir = t.getParentFile();
				} else if (t.isDirectory()) {
					tpl = new File(t, t.getName() + ".pan");
					rootdir = t;
				}

				// Run an individual test and collect any errors that arise.
				if (tpl != null) {
					if (tpl.exists()) {
						String message = invokeTest(rootdir, tpl);
						if (message != null) {
							errors.add(tpl.getName() + ": " + message);
						}
					} else {
						errors.add(tpl.getName() + ": test does not exist");
					}
				} else {
					errors.add(t.getName()
							+ ": file is not ordinary file or directory");
				}
			}
		}

		// If the error list isn't empty, then fail.
		if (errors.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (String message : errors) {
				sb.append(message);
				sb.append("\n");
			}
			fail(sb.toString());
		}

	}

	/**
	 * Invoke a single test by extracting the expected result from the object
	 * template, building the configuration, and ensuring that the expected
	 * result (either an exception or an XPath expression) is returned.
	 * 
	 * @param rootdir
	 *            root directory for build of this object template
	 * @param objtpl
	 *            the object template to compile
	 * 
	 * @return message (String) if there is an error, null otherwise
	 */
	protected String invokeTest(File rootdir, File objtpl) {

		// Create the name of the output XML file from the template name.
		String fname = objtpl.getName();
		fname = fname.substring(0, fname.length() - 4) + ".xml";
		File xml = new File(getTmpdir(), fname);

		// Delete the output XML file, if it exists.
		if (xml.exists()) {
			if (!xml.delete()) {
				throw new BuildException("cannot delete existing XML file: "
						+ xml.getAbsolutePath());
			}
		}

		// Extract what is expected from the template file. This should either
		// be an XPath expression, exception class, or a string that indicates
		// an error.
		Object expectation = extractExpectation(objtpl);
		String format = extractFormatName(objtpl);

		// Compile the given template and collect any errors.
		Object value = runCompilerViaScript(objtpl, rootdir, format);

		// Check if there was an error when running the script. If so, a String
		// was returned with the error.
		if (value instanceof String) {
			return (String) value;
		}

		// The return value should be an integer with the return code.
		int rc = -1;
		if (value instanceof Integer) {
			rc = ((Integer) value).intValue();
		} else {
			return "script returned unexpected object: "
					+ value.getClass().getSimpleName();
		}

		if (expectation instanceof Class<?>) {

			// Cast to an exception class.
			Class<?> exceptionClass = (Class<?>) expectation;

			// If return code was zero, we didn't get the exception we expected.
			if (rc == 0) {
				return ("expected " + exceptionClass.getSimpleName() + " but return code was zero");
			}

			// An exception was expected. Ensure that the output XML file does
			// not exist.
			if (xml.exists()) {
				return "expected " + exceptionClass.getSimpleName() + " but "
						+ xml.getName() + " exists";
			}

		} else if (expectation instanceof XPathExpression) {

			// An XPath expression was returned, so the template should have
			// compiled. Ensure that the output file exists and that the XPath
			// expression evaluates to true.
			XPathExpression xp = (XPathExpression) expectation;

			// Ensure that the output file exists.
			if (!xml.exists()) {

				// Check to see if there was an exception thrown. If so,
				// communicate what this exception was.
				if (rc != 0) {
					return ("unexpected exception; return code " + rc);
				} else {
					return xml.getName()
							+ " does not exist and return code was zero";
				}
			}

			// Now check that the given XPath expression evaluates to true.
			try {

				InputStream is = new FileInputStream(xml);
				InputSource source = new InputSource(is);

				Boolean ok = (Boolean) xp.evaluate(source,
						XPathConstants.BOOLEAN);
				if (!ok.booleanValue()) {
					return "XPath expression evaluated to false";
				}

			} catch (ClassCastException cce) {
				return "XPath expression is not a Boolean";
			} catch (FileNotFoundException fnfe) {
				return "can't find template file: " + objtpl.getName();
			} catch (XPathExpressionException xpee) {
				return xpee.getMessage();
			}

		} else if (expectation instanceof String) {
			return (String) expectation;
		} else {
			return "unexpected exception type "
					+ ((Class<?>) expectation).getSimpleName();
		}

		// Everything's OK, so return null to indicate this.
		return null;
	}

}
