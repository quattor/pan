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
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.BuildContext;
import org.quattor.pan.template.CompileTimeContext;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.PathSelfHolder;
import org.quattor.pan.template.ReadOnlySelfHolder;
import org.quattor.pan.utils.Path;
import org.quattor.pan.utils.TermFactory;

public class SelfSimpleListVariableTest extends AbstractOperationTestUtils {

	@Test(expected = EvaluationException.class)
	public void testNotInCompileTimeContext() {

		Context context = new CompileTimeContext();

		Operation op = ListVariable.getInstance(null, "SELF");

		op.execute(context);
	}

	@Test(expected = EvaluationException.class)
	public void testFixedSelfValue() {

		Context context = new BuildContext();

		Operation op = ListVariable.getInstance(null, "SELF");

		context.initializeSelfHolder(new ReadOnlySelfHolder(Undef.VALUE));
		op.execute(context);
	}

	@Test(expected = CompilerError.class)
	public void testMissingSelfDefinition() {

		Context context = new BuildContext();

		Operation op = ListVariable.getInstance(null, "SELF");

		op.execute(context);
	}

	@Test
	public void testCreateEmptyList() throws SyntaxException {

		Context context = new BuildContext();
		Path path = new Path("/a");
		context.initializeSelfHolder(new PathSelfHolder(path, context));

		Operation op = ListVariable.getInstance(null, "SELF");

		op.execute(context);

		// Verify that an empty list has been created.
		Element result = context.getSelf();
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
		StringProperty ok = StringProperty.getInstance("OK");

		// Setup of the context to have path set to protected list.
		ListResource value = new ListResource();
		value.append(ok);
		value = (ListResource) value.protect();
		context.putElement(path, value);
		context.initializeSelfHolder(new PathSelfHolder(path, context));

		Operation op = ListVariable.getInstance(null, "SELF");

		op.execute(context);

		// Verify that an empty list has been created.
		Element result = context.getSelf();
		assertTrue(result != null);
		assertTrue(result instanceof ListResource);

		// Ensure that the result is not the same protected list used to
		// initialize the path.
		assertTrue(result != value);
		assertFalse(result.isProtected());

		// Ensure that the result really is a copy of the input.
		ListResource list = (ListResource) result;
		assertTrue(list.size() == 1);
		assertTrue(list.get(TermFactory.create(0L)) == ok);
	}

	@Test(expected = EvaluationException.class)
	public void testValueNotList() throws SyntaxException, InvalidTermException {

		Context context = new BuildContext();
		Path path = new Path("/a");
		StringProperty bad = StringProperty.getInstance("BAD");

		// Set the path to the given string property.
		context.putElement(path, bad);
		context.initializeSelfHolder(new PathSelfHolder(path, context));

		Operation op = ListVariable.getInstance(null, "SELF");

		op.execute(context);
	}

}
