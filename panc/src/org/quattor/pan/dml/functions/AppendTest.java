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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/ExistsTest.java $
 $Id: ExistsTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.CompileTimeContext;
import org.quattor.pan.template.Context;
import org.quattor.pan.utils.TermFactory;

public class AppendTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Append.class);
	}

	@Test
	public void isSelfAppend() throws SyntaxException {
		Operation op = Append.getInstance(null, StringProperty
				.getInstance("OK"));
		assertTrue(op instanceof SelfAppend);
	}

	@Test
	public void isListAppend() throws SyntaxException {
		Operation op = Append.getInstance(null, new ListResource(),
				StringProperty.getInstance("OK"));
		assertTrue(op instanceof ListAppend);
	}

	@Test(expected = SyntaxException.class)
	public void testTooFewArguments() throws SyntaxException {
		Append.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void testTooManyArguments() throws SyntaxException {
		Append.getInstance(null, new ListResource(), StringProperty
				.getInstance("OK"), StringProperty.getInstance("OK"));
	}

	@Test(expected = SyntaxException.class)
	public void testNullValueOneArg() throws SyntaxException {
		Append.getInstance(null, Null.getInstance());
	}

	@Test(expected = SyntaxException.class)
	public void testNullValueTwoArgs() throws SyntaxException {
		Append.getInstance(null, new ListResource(), Null.getInstance());
	}

	@Test
	public void testAppendToList() throws SyntaxException, InvalidTermException {

		Context context = new CompileTimeContext();
		ListResource list = new ListResource();
		Element value = StringProperty.getInstance("OK");

		Operation dml = Append.getInstance(null, list, value);

		Element result = context.executeDmlBlock(dml);

		// Check that the list argument has been updated.
		assertTrue(list.size() == 1);
		Element element = list.get(TermFactory.create(0));
		assertTrue(value == element);

		// Verify that the same list is given as the result.
		assertTrue(result == list);
	}

	@Test
	public void testAppendToProtectedList() throws SyntaxException,
			InvalidTermException {

		Context context = new CompileTimeContext();

		// Create a protected resource.
		ListResource list = new ListResource();
		list = (ListResource) list.protect();

		Element value = StringProperty.getInstance("OK");

		Operation dml = Append.getInstance(null, list, value);

		Element result = context.executeDmlBlock(dml);

		// Check that the list argument has NOT been updated.
		assertTrue(list.size() == 0);

		// Verify that a copy has been made.
		assertTrue(result != list);

		// It must also be a list.
		assertTrue(result instanceof ListResource);

		// Verify that the new list has the correct value.
		Element element = ((ListResource) result).get(TermFactory.create(0));
		assertTrue(value == element);
	}

}
