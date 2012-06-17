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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/output/PanFormatterTest.java $
 $Id: PanFormatterTest.java 3848 2008-10-31 09:29:15Z loomis $
 */

package org.quattor.pan.output;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FormatterUtilsTest {

	@Rule
	public final TemporaryFolder tmpDir = new TemporaryFolder();

	private File existingTempFile = null;

	@Before
	public void setupDirectory() throws IOException {
		File root = tmpDir.getRoot();
		existingTempFile = File.createTempFile("tmp.", ".existing", root);
	}

	@Test
	public void validNameToURI() {
		String objectName = "my/ns/object-template";
		String suffix = "dummy";
		URI result = FormatterUtils.getResultURI(objectName, suffix);
		String uriPath = result.getPath();
		assertEquals(objectName + "." + suffix, uriPath);
	}

	@Test
	public void checkCanCreateParents() {
		File f1 = new File(tmpDir.getRoot(), "level1");
		File f2 = new File(f1, "level2");
		FormatterUtils.createParentDirectories(f2);
	}

	@Test
	public void checkCannotCreateParents() {
		String tmpFilePath = existingTempFile.getAbsolutePath();
		String invalidPath = tmpFilePath + File.separator + "should-fail";
		File invalidFile = new File(invalidPath);
		FormatterUtils.createParentDirectories(invalidFile);
	}

}
