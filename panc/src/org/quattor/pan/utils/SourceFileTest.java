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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/RangeTest.java $
 $Id: RangeTest.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.utils;

import java.io.File;

import org.junit.Test;
import org.quattor.pan.exceptions.CompilerError;

public class SourceFileTest {

	@Test(expected = CompilerError.class)
	public void testIllegalArguments1() {
		new SourceFile(null, SourceFile.Type.PAN, null);
	}

	@Test(expected = CompilerError.class)
	public void testIllegalArguments2() {
		new SourceFile("valid.tpl", null, null);
	}

	@Test
	public void validNullPath() {
		new SourceFile("valid.tpl", SourceFile.Type.PAN, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidSourceFileName1() {
		new SourceFile("/illegal-name.tpl", SourceFile.Type.PAN, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidSourceFileName2() {
		new SourceFile("path/.illegal-name.tpl", SourceFile.Type.PAN, null);
	}

	@Test(expected = CompilerError.class)
	public void invalidRelativePath() {
		new SourceFile("valid.tpl", SourceFile.Type.PAN, new File(
				"home/valid.tpl"));
	}

	@Test
	public void matchedNameAndSource() {
		new SourceFile("a/b/name", SourceFile.Type.PAN, new File(
				"/home/a/b/name.tpl"));
		new SourceFile("a/b/name.tpl", SourceFile.Type.TXT, new File(
				"/home/a/b/name.tpl"));
		new SourceFile("a/b/name.txt", SourceFile.Type.TXT, new File(
				"/home/a/b/name.txt"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void mismatchedNameAndSource1() {
		new SourceFile("a/b/name", SourceFile.Type.PAN, new File(
				"/home/a/c/name.tpl"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void mismatchedNameAndSource2() {
		new SourceFile("a/c/name.tpl", SourceFile.Type.TXT, new File(
				"/home/a/b/name.tpl"));
	}

}
