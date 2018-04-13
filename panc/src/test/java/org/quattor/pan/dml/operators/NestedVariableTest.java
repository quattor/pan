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
 $Id: VariableTest.java 2659 2008-01-07 14:48:07Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.AbstractOperationTestUtils;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.CompileTimeContext;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.utils.TermFactory;

public class NestedVariableTest extends AbstractOperationTestUtils {

	@Test
	public void testHashValue() throws SyntaxException, InvalidTermException {

		Context context = new CompileTimeContext();

		long va = 1L;
		String vb = "ok";

		HashResource dict = new HashResource();
		dict.put(TermFactory.create("a"), LongProperty.getInstance(va));
		dict.put(TermFactory.create("b"), StringProperty.getInstance(vb));
		context.setLocalVariable("x", dict);

		// Execute the operations.
		Operation op = new NestedVariable(null, "x", false, StringProperty
				.getInstance("a"));
		Element r1 = op.execute(context);

		// Pull the value off the stack.
		assertTrue(r1 instanceof LongProperty);
		LongProperty n1 = (LongProperty) r1;
		long nresult = n1.getValue().longValue();
		assertTrue(va == nresult);

		// Execute the operations.
		op = new NestedVariable(null, "x", false, StringProperty
				.getInstance("b"));
		r1 = op.execute(context);

		// Pull the value off the stack.
		assertTrue(r1 instanceof StringProperty);
		StringProperty s1 = (StringProperty) r1;
		String sresult = s1.getValue();
		assertTrue(vb.equals(sresult));
	}

	@Test
	public void testListValue() throws SyntaxException, InvalidTermException {

		Context context = new CompileTimeContext();

		long va = 1L;
		String vb = "ok";

		ListResource list = new ListResource();
		list.put(TermFactory.create(1), LongProperty.getInstance(va));
		list.put(TermFactory.create(2), StringProperty.getInstance(vb));
		context.setLocalVariable("x", list);

		// Execute the operations.
		Operation op = new NestedVariable(null, "x", false, StringProperty
				.getInstance("1"));
		Element r1 = op.execute(context);

		// Pull the value off the stack.
		assertTrue(r1 instanceof LongProperty);
		LongProperty n1 = (LongProperty) r1;
		long nresult = n1.getValue().longValue();
		assertTrue(va == nresult);

		// Execute the operations.
		op = new NestedVariable(null, "x", false, LongProperty.getInstance(2));
		r1 = op.execute(context);

		// Pull the value off the stack.
		assertTrue(r1 instanceof StringProperty);
		StringProperty s1 = (StringProperty) r1;
		String sresult = s1.getValue();
		assertTrue(vb.equals(sresult));

		// Execute the operations.
		op = new NestedVariable(null, "x", false, LongProperty.getInstance(0));
		r1 = op.execute(context);

		// Pull the value off the stack.
		assertTrue(r1 instanceof Undef);
	}

	@Test
	public void testValidTwoLevelDereference() throws SyntaxException,
			InvalidTermException {

		Context context = new CompileTimeContext();

		long va = 1L;
		String vb = "ok";

		ListResource list = new ListResource();
		HashResource hash = new HashResource();
		hash.put(TermFactory.create("a"), LongProperty.getInstance(va));
		hash.put(TermFactory.create("b"), StringProperty.getInstance(vb));
		list.put(TermFactory.create(0), hash);
		context.setLocalVariable("x", list);

		// Execute the operations.
		Operation op = new NestedVariable(null, "x", false, LongProperty
				.getInstance(0L), StringProperty.getInstance("a"));
		Element r1 = op.execute(context);

		// Pull the value off the stack.
		assertTrue(r1 instanceof LongProperty);
		LongProperty n1 = (LongProperty) r1;
		long nresult = n1.getValue().longValue();
		assertTrue(va == nresult);

		// Execute the operations.
		op = new NestedVariable(null, "x", false, LongProperty.getInstance(0L),
				StringProperty.getInstance("b"));
		r1 = op.execute(context);

		// Pull the value off the stack.
		assertTrue(r1 instanceof StringProperty);
		StringProperty s1 = (StringProperty) r1;
		String sresult = s1.getValue();
		assertTrue(vb.equals(sresult));
	}

	@Test(expected = EvaluationException.class)
	public void testMissingTwoLevelDereference() throws SyntaxException,
			InvalidTermException {

		Context context = new CompileTimeContext();

		long va = 1L;
		String vb = "ok";

		ListResource list = new ListResource();
		HashResource hash = new HashResource();
		hash.put(TermFactory.create("a"), LongProperty.getInstance(va));
		hash.put(TermFactory.create("b"), StringProperty.getInstance(vb));
		list.put(TermFactory.create(0), hash);
		context.setLocalVariable("x", list);

		// Execute the operations.
		Operation op = new NestedVariable(null, "x", false, LongProperty
				.getInstance(1L), StringProperty.getInstance("a"));

		op.execute(context);
	}

	@Test
	public void testMissingTwoLevelLookup() throws SyntaxException,
			InvalidTermException {

		Context context = new CompileTimeContext();

		long va = 1L;
		String vb = "ok";

		ListResource list = new ListResource();
		HashResource hash = new HashResource();
		hash.put(TermFactory.create("a"), LongProperty.getInstance(va));
		hash.put(TermFactory.create("b"), StringProperty.getInstance(vb));
		list.put(TermFactory.create(0), hash);
		context.setLocalVariable("x", list);

		// Execute the operations.
		Operation op = new NestedVariable(null, "x", true, LongProperty
				.getInstance(1L), StringProperty.getInstance("a"));

		op.execute(context);
	}

	@Test(expected = EvaluationException.class)
	public void undefMultilevelDereference() throws SyntaxException {

		Context context = new CompileTimeContext();

		context.setLocalVariable("x", Undef.VALUE);

		// Execute the operations.
		Operation op = new NestedVariable(null, "x", false, LongProperty
				.getInstance(1L), StringProperty.getInstance("a"));

		op.execute(context);
	}

	@Test
	public void undefMultilevelLookup() throws SyntaxException {

		Context context = new CompileTimeContext();

		context.setLocalVariable("x", Undef.VALUE);

		// Execute the operations.
		Operation op = new NestedVariable(null, "x", true, LongProperty
				.getInstance(1L), StringProperty.getInstance("a"));

		op.execute(context);
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidTwoLevelDereference() throws SyntaxException,
			InvalidTermException {

		Context context = new CompileTimeContext();

		long va = 1L;
		String vb = "ok";

		ListResource list = new ListResource();
		HashResource hash = new HashResource();
		hash.put(TermFactory.create("a"), LongProperty.getInstance(va));
		hash.put(TermFactory.create("b"), StringProperty.getInstance(vb));
		list.put(TermFactory.create(0), hash);
		context.setLocalVariable("x", list);

		// Execute the operations.
		Operation op = new NestedVariable(null, "x", false, StringProperty
				.getInstance("a"), LongProperty.getInstance(0L));

		op.execute(context);
	}

	@Test
	public void testUndefinedLookup() throws SyntaxException {
		runDml(new NestedVariable(null, "x", true, LongProperty.getInstance(1L)));
	}

	@Test(expected = EvaluationException.class)
	public void testUndefinedVariable() throws SyntaxException {
		runDml(new NestedVariable(null, "x", false, LongProperty
				.getInstance(1L)));
	}

	@Test(expected = EvaluationException.class)
	public void testInvalidHashReference() throws SyntaxException,
			InvalidTermException {

		Context context = new CompileTimeContext();

		long va = 1L;
		String vb = "ok";

		HashResource hash = new HashResource();
		hash.put(TermFactory.create("a"), LongProperty.getInstance(va));
		hash.put(TermFactory.create("b"), StringProperty.getInstance(vb));
		context.setLocalVariable("x", hash);

		// Execute the operations.
		Operation op = new NestedVariable(null, "x", false, StringProperty
				.getInstance("a/b"));

		op.execute(context);
	}

}
