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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/RelativeAssignmentStatementTest.java $
 $Id: RelativeAssignmentStatementTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.statement;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.template.Context;
import org.quattor.pan.utils.Path;

public class RelativeAssignmentStatementTest extends StatementTestUtils {

	@Test
	public void simpleAssignment() throws Exception {

		Path path = new Path("/result");

		Context context = setupTemplateToRun2("as1", "'" + path + "' = 1;",
				false);

		assertTrue(context.getElement(path) != null);
		assertTrue(context.getElement(path) instanceof LongProperty);
		assertTrue(((LongProperty) context.getElement(path)).getValue().equals(
				Long.valueOf(1L)));
	}

	@Test
	public void implicitHashAssignment() throws Exception {

		Path path = new Path("/result/a");

		Context context = setupTemplateToRun2("as2", "'" + path + "' = 1;",
				false);

		assertTrue(context.getElement(path) != null);
		assertTrue(context.getElement(path) instanceof LongProperty);
		assertTrue(((LongProperty) context.getElement(path)).getValue().equals(
				Long.valueOf(1L)));
	}

	@Test
	public void implicitListAssignment() throws Exception {

		Path path = new Path("/result/0");

		Context context = setupTemplateToRun2("as3", "'" + path + "' = 1;",
				false);

		assertTrue(context.getElement(path) != null);
		assertTrue(context.getElement(path) instanceof LongProperty);
		assertTrue(((LongProperty) context.getElement(path)).getValue().equals(
				Long.valueOf(1L)));
	}

	@Test
	public void conditionalAssignment() throws Exception {

		Path path = new Path("/result");

		Context context = setupTemplateToRun2("as4", "'" + path + "' ?= 1;",
				false);

		assertTrue(context.getElement(path) != null);
		assertTrue(context.getElement(path) instanceof LongProperty);
		assertTrue(((LongProperty) context.getElement(path)).getValue().equals(
				Long.valueOf(1L)));

		context = setupTemplateToRun2("as5", "'" + path + "' = undef; '" + path
				+ "' ?= 1;", false);

		assertTrue(context.getElement(path) != null);
		assertTrue(context.getElement(path) instanceof LongProperty);
		assertTrue(((LongProperty) context.getElement(path)).getValue().equals(
				Long.valueOf(1L)));
	}

}
