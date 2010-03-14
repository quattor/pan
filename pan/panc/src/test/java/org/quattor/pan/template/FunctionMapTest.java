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

import org.junit.Test;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.utils.FunctionDefinition;

public class FunctionMapTest {

	@Test
	public void checkNullForUndefinedFunction() {
		FunctionMap map = new FunctionMap();
		assertTrue(map.get("DUMMY") == null);
	}

	@Test
	public void checkSimpleAssociation() throws SyntaxException {

		FunctionMap map = new FunctionMap();

		// Setup an argument list with two string values.
		String name = "DUMMY";
		Operation op = StringProperty.getInstance("OK");
		Template emptyTemplate = new Template("empty");
		SourceRange sourceRange = new SourceRange(1, 1, 1, 10);

		// Put this into the function map.
		map.put(name, op, emptyTemplate, sourceRange);

		// Ensure that we get something out.
		FunctionDefinition defn = map.get(name);
		assertTrue(defn.sourceRange == sourceRange);
	}

	@Test(expected = EvaluationException.class)
	public void replaceExistingDefn() throws SyntaxException {

		FunctionMap map = new FunctionMap();

		// Setup an argument list with two string values.
		String name = "DUMMY";
		Operation op = StringProperty.getInstance("OK");
		Template emptyTemplate = new Template("empty");
		SourceRange sourceRange1 = new SourceRange(1, 1, 1, 10);
		SourceRange sourceRange2 = new SourceRange(2, 1, 2, 10);

		// Put first definition in and then try to replace it.
		map.put(name, op, emptyTemplate, sourceRange1);
		map.put(name, op, emptyTemplate, sourceRange2);
	}

	@Test
	public void testNullTemplate() {

		FunctionMap map = new FunctionMap();

		// Setup an argument list with two string values.
		String name = "DUMMY";
		Operation op = StringProperty.getInstance("OK");
		SourceRange sourceRange = new SourceRange(1, 1, 1, 10);

		// Put this into the function map.
		map.put(name, op, null, sourceRange);

		// Ensure that we get something out.
		FunctionDefinition defn = map.get(name);
		assertTrue(defn.sourceRange == sourceRange);
	}

	@Test
	public void testNullSourceRange() throws SyntaxException {

		FunctionMap map = new FunctionMap();

		// Setup an argument list with two string values.
		String name = "DUMMY";
		Operation op = StringProperty.getInstance("OK");
		Template emptyTemplate = new Template("empty");

		// Put this into the function map.
		map.put(name, op, emptyTemplate, null);

		// Ensure that we get something out.
		FunctionDefinition defn = map.get(name);
		assertTrue(defn.template == emptyTemplate);
	}

}
