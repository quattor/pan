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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/Sources/panc/trunk/src/org/quattor/pan/utils/PathTest.java $
 $Id: PathTest.java 998 2006-11-15 19:44:28Z loomis $
 */

package org.quattor.pan.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.exceptions.SyntaxException;

public class FinalFlagsTest {

	@Test
	public void rootPath() throws SyntaxException {

		FinalFlags flags = new FinalFlags();

		Path p = new Path("/");

		// Initial state should be false.
		assertFalse(flags.isFinal(p));

		// Set it as final and make sure it returns the right value.
		flags.setFinal(p);
		assertTrue(flags.isFinal(p));
	}

	@Test
	public void multilevelPath() throws SyntaxException {

		FinalFlags flags = new FinalFlags();

		Path p = new Path("/a/0/b/1");

		flags.setFinal(p);

		// All parents and the path must be true.
		assertTrue(flags.isFinal(new Path("/")));
		assertTrue(flags.isFinal(new Path("/a")));
		assertTrue(flags.isFinal(new Path("/a/0")));
		assertTrue(flags.isFinal(new Path("/a/0/b")));
		assertTrue(flags.isFinal(p));

		// Anything off of the this path must be false.
		assertFalse(flags.isFinal(new Path("/a/1")));
	}

	@Test
	public void inheritedStatus() throws SyntaxException {

		FinalFlags flags = new FinalFlags();

		Path p = new Path("/a/0/b/1/c");
		flags.setFinal(p);

		flags.setFinal(new Path("/a/0"));

		// All direct parents and descendants must be true.
		assertTrue(flags.isFinal(new Path("/")));
		assertTrue(flags.isFinal(new Path("/a")));
		assertTrue(flags.isFinal(new Path("/a/0")));
		assertTrue(flags.isFinal(new Path("/a/0/b")));
		assertTrue(flags.isFinal(new Path("/a/0/b/1")));
		assertTrue(flags.isFinal(new Path("/a/0/b/1/c")));

		// Anything off direct path should be false.
		assertFalse(flags.isFinal(new Path("/a/1")));
	}

}
