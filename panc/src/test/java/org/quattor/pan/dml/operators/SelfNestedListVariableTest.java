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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/VariableTest.java $
 $Id: VariableTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.operators;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.AbstractOperationTestUtils;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.BuildContext;
import org.quattor.pan.ttemplate.CompileTimeContext;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.PathSelfHolder;
import org.quattor.pan.ttemplate.ReadOnlySelfHolder;
import org.quattor.pan.utils.Path;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

public class SelfNestedListVariableTest extends AbstractOperationTestUtils {

	private static Operation[] ops = { LongProperty.getInstance(0L),
			StringProperty.getInstance("a") };

	private static Term[] terms = { TermFactory.create(0L),
			TermFactory.create("a") };

	@Test
	public void verifyCorrectType() throws SyntaxException {

		Operation op = ListVariable.getInstance(null, "SELF", ops);

		assertTrue(op instanceof SelfNestedListVariable);
	}

	@Test(expected = SyntaxException.class)
	public void testInvalidSelfContext() throws SyntaxException {

		Operation op = ListVariable.getInstance(null, "SELF", ops);

		op.checkInvalidSelfContext();
	}

	@Test(expected = EvaluationException.class)
	public void testNotInCompileTimeContext() {

		Context context = new CompileTimeContext();

		Operation op = ListVariable.getInstance(null, "SELF", ops);

		op.execute(context);
	}

	@Test(expected = EvaluationException.class)
	public void testFixedSelfValue() {

		Context context = new BuildContext();

		Operation op = ListVariable.getInstance(null, "SELF", ops);

		context.initializeSelfHolder(new ReadOnlySelfHolder(Undef.VALUE));
		op.execute(context);
	}

	@Test(expected = CompilerError.class)
	public void testMissingSelfDefinition() {

		Context context = new BuildContext();

		Operation op = ListVariable.getInstance(null, "SELF", ops);

		op.execute(context);
	}

	@Test
	public void testCreateEmptyList() throws SyntaxException,
			InvalidTermException {

		Context context = new BuildContext();
		Path path = new Path("/a");
		context.initializeSelfHolder(new PathSelfHolder(path, context));

		Operation op = ListVariable.getInstance(null, "SELF", ops);

		op.execute(context);

		// Verify that an empty list has been created.
		Element root = context.getSelf();
		Element result = root.rget(terms, 0, false, false);
		assertTrue(result != null);
		assertTrue(result instanceof ListResource);

		ListResource list = (ListResource) result;
		assertTrue(list.size() == 0);
	}

	@Test
	public void testReplaceProtectedList() throws SyntaxException,
			InvalidTermException {

		Context context = new BuildContext();
		Path path = new Path("/a");

		// Setup of the context to have path set to protected list.
		ListResource value = new ListResource();
		value = (ListResource) value.protect();
		context.putElement(path, value);
		context.initializeSelfHolder(new PathSelfHolder(path, context));

		Operation op = ListVariable.getInstance(null, "SELF", ops);

		op.execute(context);

		// Verify that an empty list has been created.
		Element root = context.getSelf();

		// Ensure that the result is not the same protected list used to
		// initialize the path.
		assertTrue(root != value);
		assertFalse(root.isProtected());

		// Get the result.
		Element result = root.rget(terms, 0, false, false);
		assertTrue(result != null);
		assertTrue(result instanceof ListResource);

		// Ensure that the result is an empty list.
		ListResource list = (ListResource) result;
		assertTrue(list.size() == 0);
	}

	@Test(expected = EvaluationException.class)
	public void testValueNotList() throws SyntaxException, InvalidTermException {

		Context context = new BuildContext();
		Path rootPath = new Path("/a");
		Path path = new Path("/a/0/a");
		StringProperty bad = StringProperty.getInstance("BAD");

		// Set the path to the given string property.
		context.putElement(path, bad);
		context.initializeSelfHolder(new PathSelfHolder(rootPath, context));

		Operation op = ListVariable.getInstance(null, "SELF", ops);

		op.execute(context);
	}

}
