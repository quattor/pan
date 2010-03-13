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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/parser/PanParserTest.java $
 $Id: PanParserTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.parser;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class PanParserTest {

	protected File getTestdir() {

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

	protected void collectTemplateFiles(File file, List<File> templates) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				collectTemplateFiles(f, templates);
			}
		} else {
			if (file.toString().endsWith(".tpl")) {
				templates.add(file);
			}
		}
	}

	public void verifyInvalidParseTemplate(File f) {

		try {
			PanParser parser = new PanParser(new FileReader(f));
			try {
				parser.template();
				fail(f + " did not throw ParseException");
			} catch (ParseException pe) {
				// OK
			} catch (Exception e) {
				fail(f + " threw unexpected exception: " + e.getMessage());
			} catch (TokenMgrError err) {
				fail(f + " threw TokenMgrError: " + err.getMessage());
			}
		} catch (FileNotFoundException fe) {
			fail("file " + f + " does not exist: " + fe.getMessage());
		}
	}

	public void verifyInvalidSyntaxTemplate(File f) {

		try {
			PanParser parser = new PanParser(new FileReader(f));
			try {
				parser.template();
				fail(f + " did not throw SyntaxException");
				// } catch (SyntaxException pe) {
				// OK
			} catch (Exception e) {
				fail(f + " threw unexpected exception: " + e.getMessage());
			} catch (TokenMgrError err) {
				fail(f + "throw TokenMgrError: " + err.getMessage());
			}
		} catch (FileNotFoundException fe) {
			fail("file " + f + " does not exist: " + fe.getMessage());
		}
	}

	public void verifyValidTemplate(File f) {

		try {
			PanParser parser = new PanParser(new FileReader(f));
			try {
				parser.template();
			} catch (Exception e) {
				fail(f + " threw unexpected exception: " + e.getMessage());
			} catch (TokenMgrError err) {
				fail(f + "throw TokenMgrError: " + err.getMessage());
			}
		} catch (FileNotFoundException fe) {
			fail("file " + f + " does not exist: " + fe.getMessage());
		}
	}

	@Test
	public void testParseExceptionTemplates() {

		File testdir = getTestdir();
		List<File> templates = new LinkedList<File>();
		collectTemplateFiles(new File(testdir, "ParseException"), templates);

		for (File tpl : templates) {
			verifyInvalidParseTemplate(tpl);
		}
	}

	@Test
	public void testSyntaxExceptionTemplates() {

		File testdir = getTestdir();
		List<File> templates = new LinkedList<File>();
		collectTemplateFiles(new File(testdir, "ParseException"), templates);

		for (File tpl : templates) {
			verifyInvalidParseTemplate(tpl);
		}
	}

	@Test
	public void testAllValidTemplates() {

		File testdir = getTestdir();
		List<File> templates = new LinkedList<File>();
		collectTemplateFiles(new File(testdir, "Valid"), templates);

		for (File tpl : templates) {
			verifyValidTemplate(tpl);
		}
	}

}
