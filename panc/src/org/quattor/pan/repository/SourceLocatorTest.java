/*
 Copyright (c) 2008 Charles A. Loomis, Jr, Cedric Duprilot, and
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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/TermTest.java $
 $Id: TermTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quattor.pan.utils.TestUtils;

public class SourceLocatorTest {

	static private File tmpdir = new File(TestUtils.getTmpdir(),
			"TemplateLocatorTest");

	static private File session = new File(tmpdir, "session");

	static private File include1 = new File(tmpdir, "include1");

	static private File include2 = new File(tmpdir, "include2");

	static private List<String> rpaths = new LinkedList<String>();
	static {
		rpaths.add("rpath");
	}

	static private SourceLocator locator;

	@BeforeClass
	static public void createTestFiles() throws IOException {

		// Remove existing directory and contents.
		if (tmpdir.exists()) {
			recursiveFileDelete(tmpdir);
		}

		// Now (re)create the test directory.
		if (!tmpdir.mkdirs()) {
			System.err.println("WARNING: could not create directory " + tmpdir);
		}

		// Now create the session and include directories.
		if (!session.mkdir()) {
			System.err
					.println("WARNING: could not create directory " + session);
		}
		if (!include1.mkdir()) {
			System.err.println("WARNING: could not create directory "
					+ include1);
		}
		if (!include2.mkdir()) {
			System.err.println("WARNING: could not create directory "
					+ include2);
		}

		// Create the TemplateLocator to test.
		List<File> includes = new LinkedList<File>();
		includes.add(include1);
		includes.add(include2);
		locator = new SourceLocator(session, includes);

	}

	@AfterClass
	static public void deleteTestFiles() {

		// Remove existing directory and contents.
		if (tmpdir.exists()) {
			recursiveFileDelete(tmpdir);
		}
	}

	@Test
	public void checkSessionTplUsed() throws IOException {

		File ok;

		// Using directly the include paths.
		ok = touch(session, "a.tpl");
		touch(include1, "a.tpl");
		touch(include2, "a.tpl");

		assertEquals(ok, locator.lookup("a"));

		ok = touch(session, "b.tpl");
		touch(include1, "b.tpl");

		assertEquals(ok, locator.lookup("b"));

		ok = touch(session, "c.tpl");
		touch(include2, "c.tpl");

		assertEquals(ok, locator.lookup("c"));

		// Using a relative path.
		ok = rtouch(session, "ra.tpl");
		rtouch(include1, "ra.tpl");
		rtouch(include2, "ra.tpl");

		assertEquals(ok, locator.lookup("ra", rpaths));

		ok = rtouch(session, "rb.tpl");
		rtouch(include1, "rb.tpl");

		assertEquals(ok, locator.lookup("rb", rpaths));

		ok = rtouch(session, "rc.tpl");
		rtouch(include2, "rc.tpl");

		assertEquals(ok, locator.lookup("rc", rpaths));

	}

	@Test
	public void checkSessionDelUsed() throws IOException {

		// Using include directories directly.
		touch(session, "d.del");
		touch(session, "d.tpl");
		touch(include1, "d.tpl");
		touch(include2, "d.tpl");

		assertNull(locator.lookup("d"));

		touch(session, "e.del");
		touch(include1, "e.tpl");

		assertNull(locator.lookup("e"));

		touch(session, "f.del");
		touch(include2, "f.tpl");

		assertNull(locator.lookup("f"));

		// Using relative path.
		rtouch(session, "rd.del");
		rtouch(session, "rd.tpl");
		rtouch(include1, "rd.tpl");
		rtouch(include2, "rd.tpl");

		assertNull(locator.lookup("rd", rpaths));

		rtouch(session, "re.del");
		rtouch(include1, "re.tpl");

		assertNull(locator.lookup("re", rpaths));

		rtouch(session, "rf.del");
		rtouch(include2, "rf.tpl");

		assertNull(locator.lookup("rf", rpaths));
	}

	@Test
	public void checkIncludeOrder() throws IOException {

		File ok;

		// Using include directories directly.
		ok = touch(include1, "g.tpl");
		touch(include2, "g.tpl");

		assertEquals(ok, locator.lookup("g"));

		ok = touch(include1, "h.tpl");

		assertEquals(ok, locator.lookup("h"));

		ok = touch(include2, "i.tpl");

		assertEquals(ok, locator.lookup("i"));

		// Using relative path.
		ok = rtouch(include1, "rg.tpl");
		rtouch(include2, "rg.tpl");

		assertEquals(ok, locator.lookup("rg", rpaths));

		ok = rtouch(include1, "rh.tpl");

		assertEquals(ok, locator.lookup("rh", rpaths));

		ok = rtouch(include2, "ri.tpl");

		assertEquals(ok, locator.lookup("ri", rpaths));
	}

	@Test
	public void checkIncludeOrderWithDel() throws IOException {

		File ok;

		// Using include directories directly.
		touch(include1, "j.tpl");
		touch(include1, "j.del");
		ok = touch(include2, "j.tpl");

		assertEquals(ok, locator.lookup("j"));

		ok = touch(include1, "k.tpl");
		touch(include2, "k.del");
		touch(include2, "k.tpl");

		assertEquals(ok, locator.lookup("k"));

		// Using relative path.
		rtouch(include1, "rj.tpl");
		rtouch(include1, "rj.del");
		ok = rtouch(include2, "rj.tpl");

		assertEquals(ok, locator.lookup("rj", rpaths));

		ok = rtouch(include1, "rk.tpl");
		rtouch(include2, "rk.del");
		rtouch(include2, "rk.tpl");

		assertEquals(ok, locator.lookup("rk", rpaths));
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
	static private File touch(File dir, String name) throws IOException {

		// Ensure that the directory argument exists and is a directory.
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				System.err
						.println("WARNING: could not create directory " + dir);
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
			System.err.println("WARNING: could not update timestamp for "
					+ file);
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
	static private File rtouch(File dir, String name) throws IOException {
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
	static private void recursiveFileDelete(File root) {
		if (root.exists()) {
			if (root.isDirectory()) {
				for (File f : root.listFiles()) {
					recursiveFileDelete(f);
				}
			}
			if (!root.delete()) {
				System.err.println("WARNING: could not delete file " + root);
			}
		}
	}

}
