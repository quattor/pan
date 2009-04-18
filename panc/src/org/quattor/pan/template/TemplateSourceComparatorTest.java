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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/template/ObjectContextTest.java $
 $Id: ObjectContextTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.template;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.quattor.pan.exceptions.SyntaxException;

public class TemplateSourceComparatorTest {

	@Test
	public void checkNormalTemplates() throws SyntaxException {

		File f1 = new File("a.tpl");
		File f2 = new File("b.tpl");
		File f3 = new File("c.tpl");

		Template t1 = new Template(f1.getAbsoluteFile(), null,
				Template.TemplateType.ORDINARY, "a", null);
		Template t2 = new Template(f2.getAbsoluteFile(), null,
				Template.TemplateType.ORDINARY, "b", null);
		Template t3 = new Template(f3.getAbsoluteFile(), null,
				Template.TemplateType.ORDINARY, "c", null);

		TemplateSourceComparator comparator = TemplateSourceComparator
				.getInstance();

		assertTrue(comparator.compare(t1, t2) < 0);
		assertTrue(comparator.compare(t1, t3) < 0);
		assertTrue(comparator.compare(t2, t3) < 0);

		assertTrue(comparator.compare(t2, t1) > 0);
		assertTrue(comparator.compare(t3, t1) > 0);
		assertTrue(comparator.compare(t3, t2) > 0);

		assertTrue(comparator.compare(t1, t1) == 0);
		assertTrue(comparator.compare(t2, t2) == 0);
		assertTrue(comparator.compare(t3, t3) == 0);
	}

	@Test
	public void checkNullSourceTemplates() throws SyntaxException {

		File f = new File("a.tpl");

		Template t = new Template(f.getAbsoluteFile(), null,
				Template.TemplateType.ORDINARY, "a", null);
		Template n = new Template(null, null, Template.TemplateType.ORDINARY,
				"n", null);

		TemplateSourceComparator comparator = TemplateSourceComparator
				.getInstance();

		// A null source should be less than one with a source.
		assertTrue(comparator.compare(n, t) < 0);
		assertTrue(comparator.compare(t, n) > 0);

		// Two null source templates should be equal.
		assertTrue(comparator.compare(n, n) == 0);
	}

}
