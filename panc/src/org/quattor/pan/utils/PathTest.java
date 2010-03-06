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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/PathTest.java $
 $Id: PathTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.quattor.pan.utils.TestUtils.getTmpdir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.utils.Path.PathType;

public class PathTest {

	@Test
	public void testPathType() throws SyntaxException {

		// Older form that will be deprecated.
		Path p = new Path("//external/");
		assertTrue(p.isExternal());
		assertFalse(p.isAbsolute());
		assertFalse(p.isRelative());
		assertEquals(PathType.EXTERNAL, p.getType());

		// Newer form that will allow namespaced object templates.
		p = new Path("external:");
		assertTrue(p.isExternal());
		assertFalse(p.isAbsolute());
		assertFalse(p.isRelative());
		assertEquals(PathType.EXTERNAL, p.getType());

		p = new Path("/absolute/");
		assertFalse(p.isExternal());
		assertTrue(p.isAbsolute());
		assertFalse(p.isRelative());
		assertEquals(PathType.ABSOLUTE, p.getType());

		p = new Path("relative/");
		assertFalse(p.isExternal());
		assertFalse(p.isAbsolute());
		assertTrue(p.isRelative());
		assertEquals(PathType.RELATIVE, p.getType());
	}

	@Test
	public void testLegalAuthoritiesOld() throws SyntaxException {
		List<String> paths = Arrays.asList("//alpha", "//alpha/",
				"//127.0.0.1", "//127.0.0.1/", "//-", "//-/", "//+", "//+/",
				"//_", "//_/");
		for (String s : paths) {
			try {
				new Path(s);
			} catch (IllegalArgumentException iae) {
				fail("legal authority threw exception (" + s + ")");
			}
		}
	}

	@Test
	public void testLegalAuthorities() {
		List<String> paths = Arrays.asList("alpha:", "alpha:/", "127.0.0.1:",
				"127.0.0.1:/", "-:", "-:/", "+:", "+:/", "_:", "_:/");
		for (String s : paths) {
			try {
				new Path(s);
			} catch (SyntaxException se) {
				fail("legal authority threw exception (" + s + ")");
			}
		}
	}

	@Test
	public void testLegalNamespacedAuthorities() {
		List<String> paths = Arrays.asList("ns/alpha:", "ns/alpha:/",
				"ns/127.0.0.1:", "ns/127.0.0.1:/", "ns/-:", "ns/-:/", "ns/+:",
				"ns/+:/", "ns/_:", "ns/_:/");
		for (String s : paths) {
			try {
				new Path(s);
			} catch (SyntaxException se) {
				fail("legal authority threw exception (" + s + ")");
			}
		}
	}

	@Test
	public void testIllegalAuthoritiesOld() {
		List<String> paths = Arrays.asList("//alpha;", "///", "//");
		for (String s : paths) {
			try {
				new Path(s);
				fail("illegal authority did not throw exception (" + s + ")");
			} catch (SyntaxException se) {
				// OK.
			}
		}
	}

	@Test
	public void testIllegalAuthorities() {
		List<String> paths = Arrays.asList("alpha;:", ":/", ":");
		for (String s : paths) {
			try {
				new Path(s);
				fail("illegal authority did not throw exception (" + s + ")");
			} catch (SyntaxException se) {
				// OK.
			}
		}
	}

	@Test
	public void testIllegalNamespacedAuthorities() {
		List<String> paths = Arrays.asList("ns/.:/", "ns/..:/", "ns/.hidden:/",
				"ns//profile:/", "ns/./profile:/", "ns/../profile:/",
				"ns/.hidden/profile:/");
		for (String s : paths) {
			try {
				new Path(s);
				fail("illegal authority did not throw exception (" + s + ")");
			} catch (SyntaxException se) {
				// OK.
			}
		}
	}

	@Test
	public void testInvalidFirstTerm() {
		List<String> paths = Arrays.asList("//alpha/0/a", "alpha:/0/a",
				"alpha:0/a", "/0/a", "0/a", "//alpha/0xf/a", "alpha:/0xf/a",
				"alpha:0xf/a", "/0xf/a", "0xf/a");
		for (String s : paths) {
			try {
				new Path(s);
				fail("illegal first term did not throw an exception (" + s
						+ ")");
			} catch (SyntaxException se) {
				// OK.
			}
		}
	}

	@Test
	public void testValidEscapedValues() {
		List<String> paths = Arrays.asList("{}", "{a}", "{a/b}", "a/{b}",
				"/{b}/c", "a/{b}/c", "a/{b/c}/d");
		for (String s : paths) {
			try {
				new Path(s);
			} catch (SyntaxException se) {
				fail("valid escaped path threw exception (" + s + ")");
			}
		}
	}

	@Test
	public void testInvalidEscapedValues() {
		List<String> paths = Arrays.asList("{", "}", "{{}", "{}}",
				"a/{a{b}c}/d", "a/{b}c}/d");
		for (String s : paths) {
			try {
				new Path(s);
				fail("invalid escaped path did not throw an exception (" + s
						+ ")");
			} catch (SyntaxException se) {
				// OK.
			}
		}
	}

	@Test
	public void testProperPathEscaping() throws SyntaxException {
		String x = "<45/77>";
		String[] paths = new String[] { "{}", "{" + x + "}" };
		String[] results = new String[] { "_", EscapeUtils.escape(x) };
		for (int i = 0; i < paths.length; i++) {
			Path p = new Path(paths[i]);
			if (!results[i].equals(p.toString())) {
				fail("incorrect escaping: " + paths[i] + " " + results[i] + " "
						+ p.toString());
			}
		}
	}

	@Test
	public void testPathToList() throws SyntaxException {

		List<String> correct = Arrays.asList("alpha", "beta", "gamma", "3");
		List<String> paths = Arrays.asList("/alpha/beta/gamma/3",
				"alpha/beta/gamma/3", "alpha/beta/gamma/3/");

		for (String s : paths) {
			Path p = new Path(s);
			List<String> test = p.toList();
			assertTrue(correct.equals(test));
		}
	}

	@Test
	public void testLegalCombinations() throws SyntaxException {

		Term[] p2 = { TermFactory.create("gamma"), TermFactory.create("delta") };
		List<String> correct = Arrays.asList("alpha", "beta", "gamma", "delta");

		Path p1 = new Path("alpha/beta");
		Path p = new Path(p1, p2);
		assertTrue(correct.equals(p.toList()));
		assertTrue(p.isRelative());

		p1 = new Path("/alpha/beta");
		p = new Path(p1, p2);
		assertTrue(correct.equals(p.toList()));
		assertTrue(p.isAbsolute());
	}

	@Test
	public void testIllegalCombinations() {

		Term[] p2 = { TermFactory.create("gamma"), TermFactory.create("delta") };
		List<String> correct = Arrays.asList("alpha", "beta", "gamma", "delta");

		List<String> paths = Arrays.asList("//alpha/beta", "alpha:beta",
				"alpha:/beta");

		for (String s : paths) {
			try {

				Path p1 = new Path(s);
				Path p = new Path(p1, p2);
				assertTrue(correct.equals(p.toList()));
				assertTrue(p.isExternal());

				fail("illegal combination did not thrown an exception (" + s
						+ ")");
			} catch (SyntaxException se) {
				// OK.
			}
		}
	}

	@Test
	public void testEquals() throws SyntaxException {

		Path p1 = new Path("//alpha/");
		Path p2 = new Path("//alpha");
		assertTrue(p1.equals(p2));

		p1 = new Path("alpha:/");
		p2 = new Path("alpha:");
		assertTrue(p1.equals(p2));

		p1 = new Path("/absolute/");
		p2 = new Path("/absolute");
		assertTrue(p1.equals(p2));

		p1 = new Path("relative/");
		p2 = new Path("relative");
		assertTrue(p1.equals(p2));
	}

	@Test
	public void testToString() throws SyntaxException {
		Path p = new Path("//alpha/");
		assertEquals("incorrect path string representation: " + p, "alpha:/", p
				.toString());

		p = new Path("//alpha");
		assertEquals("incorrect path string representation: " + p, "alpha:/", p
				.toString());

		p = new Path("alpha:/");
		assertEquals("incorrect path string representation: " + p, "alpha:/", p
				.toString());

		p = new Path("alpha:");
		assertEquals("incorrect path string representation: " + p, "alpha:/", p
				.toString());

		p = new Path("/");
		assertEquals("incorrect path string representation: " + p, "/", p
				.toString());

		p = new Path("//alpha/beta");
		assertEquals("incorrect path string representation: " + p,
				"alpha:/beta", p.toString());

		p = new Path("alpha:/beta");
		assertEquals("incorrect path string representation: " + p,
				"alpha:/beta", p.toString());

		p = new Path("alpha:beta");
		assertEquals("incorrect path string representation: " + p,
				"alpha:/beta", p.toString());

		p = new Path("/absolute");
		assertEquals("incorrect path string representation: " + p, "/absolute",
				p.toString());

		p = new Path("/absolute");
		assertEquals("incorrect path string representation: " + p, "/absolute",
				p.toString());

		p = new Path("/absolute/beta");
		assertEquals("incorrect path string representation: " + p,
				"/absolute/beta", p.toString());

		p = new Path("relative/");
		assertEquals("incorrect path string representation: " + p, "relative",
				p.toString());

		p = new Path("relative");
		assertEquals("incorrect path string representation: " + p, "relative",
				p.toString());

		p = new Path("relative/beta");
		assertEquals("incorrect path string representation: " + p,
				"relative/beta", p.toString());

		p = new Path("relative/beta/gamma");
		assertEquals("incorrect path string representation: " + p,
				"relative/beta/gamma", p.toString());
	}

	@Test
	public void testOrdering() throws SyntaxException {

		Path e = new Path("//alpha/beta/gamma");
		Path r = new Path("alpha/beta/gamma");
		Path a = new Path("/alpha/beta/gamma");

		Path e2 = new Path("//alpha/beta");
		Path r2 = new Path("alpha/beta");
		Path a2 = new Path("/alpha/beta");

		// Check ordering of different types.
		assertTrue(r.compareTo(a) < 0);
		assertTrue(a.compareTo(r) > 0);

		assertTrue(r.compareTo(e) < 0);
		assertTrue(e.compareTo(r) > 0);

		assertTrue(a.compareTo(e) < 0);
		assertTrue(e.compareTo(a) > 0);

		// Ensure equal values really are.
		assertTrue(e.compareTo(e) == 0);
		assertTrue(r.compareTo(r) == 0);
		assertTrue(a.compareTo(a) == 0);

		// Check ordering with different lengths.
		assertTrue(e.compareTo(e2) < 0);
		assertTrue(e2.compareTo(e) > 0);

		assertTrue(r.compareTo(r2) < 0);
		assertTrue(r2.compareTo(r) > 0);

		assertTrue(a.compareTo(a2) < 0);
		assertTrue(a2.compareTo(a) > 0);
	}

	@Test
	public void testOrdering2() throws SyntaxException {

		Path e = new Path("alpha:beta/gamma");
		Path r = new Path("alpha/beta/gamma");
		Path a = new Path("/alpha/beta/gamma");

		Path e2 = new Path("alpha:beta");
		Path r2 = new Path("alpha/beta");
		Path a2 = new Path("/alpha/beta");

		// Check ordering of different types.
		assertTrue(r.compareTo(a) < 0);
		assertTrue(a.compareTo(r) > 0);

		assertTrue(r.compareTo(e) < 0);
		assertTrue(e.compareTo(r) > 0);

		assertTrue(a.compareTo(e) < 0);
		assertTrue(e.compareTo(a) > 0);

		// Ensure equal values really are.
		assertTrue(e.compareTo(e) == 0);
		assertTrue(r.compareTo(r) == 0);
		assertTrue(a.compareTo(a) == 0);

		// Check ordering with different lengths.
		assertTrue(e.compareTo(e2) < 0);
		assertTrue(e2.compareTo(e) > 0);

		assertTrue(r.compareTo(r2) < 0);
		assertTrue(r2.compareTo(r) > 0);

		assertTrue(a.compareTo(a2) < 0);
		assertTrue(a2.compareTo(a) > 0);
	}

	@Test(expected = NullPointerException.class)
	public void testNullOrdering() throws SyntaxException {
		Path p = new Path("alpha");
		p.compareTo(null);
	}

}
