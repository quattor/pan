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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.junit.Test;

public class DebugPatternsTest {

	@Test
	public void verifyDefaultsAreSetCorrectly() {
		DebugPatterns debugPatterns = new DebugPatterns();
		assertSame(DebugPatterns.ALL, debugPatterns.getInclude());
		assertSame(DebugPatterns.NONE, debugPatterns.getExclude());
	}

	@Test
	public void verifyGettersAndSettersOK() {

		String selfMatcher1 = "ok_pattern_1";
		String selfMatcher2 = "ok_pattern_2";

		DebugPatterns debugPatterns = new DebugPatterns();

		debugPatterns.setInclude(selfMatcher1);
		Pattern includePattern = debugPatterns.getInclude();
		assertTrue(includePattern.matcher(selfMatcher1).matches());

		debugPatterns.setExclude(selfMatcher2);
		Pattern excludePattern = debugPatterns.getExclude();
		assertTrue(excludePattern.matcher(selfMatcher2).matches());
	}

	@Test(expected = BuildException.class)
	public void exceptionForInvalidIncludePattern() {
		DebugPatterns debugPatterns = new DebugPatterns();
		debugPatterns.setInclude("[invalid-regex");
	}

	@Test(expected = BuildException.class)
	public void exceptionForInvalidExcludePattern() {
		DebugPatterns debugPatterns = new DebugPatterns();
		debugPatterns.setExclude("[invalid-regex");
	}

	@Test(expected = BuildException.class)
	public void exceptionForNullIncludePattern() {
		DebugPatterns debugPatterns = new DebugPatterns();
		debugPatterns.setInclude(null);
	}

	@Test(expected = BuildException.class)
	public void exceptionForNullExcludePattern() {
		DebugPatterns debugPatterns = new DebugPatterns();
		debugPatterns.setExclude(null);
	}

}
