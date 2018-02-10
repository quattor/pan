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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/AssignTest.java $
 $Id: AssignTest.java 3600 2008-08-17 14:48:32Z loomis $
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

public class AssignTest extends AbstractOperationTestUtils {

	@Test
	public void referenceSimpleValue() throws SyntaxException {

		Context context = new CompileTimeContext();

		long va = 1L;

		// Execute the operations.
		Operation op = new Assign(null, LongProperty.getInstance(va),
				new SetValue(null, "x"));
		Element r1 = op.execute(context);

		// Check that the value is correct.
		assertTrue(r1 instanceof LongProperty);
		LongProperty s1 = (LongProperty) r1;
		long sresult = s1.getValue().longValue();
		assertTrue(va == sresult);

		// Check that the variable is set.
		r1 = context.getLocalVariable("x");
		assertTrue(r1 instanceof LongProperty);
		s1 = (LongProperty) r1;
		sresult = s1.getValue().longValue();
		assertTrue(va == sresult);
	}

	@Test
	public void referenceHash() throws SyntaxException, InvalidTermException {

		Context context = new CompileTimeContext();

		long va = 1L;
		String vb = "a";

		// Execute the operations.
		Operation op = new Assign(null, LongProperty.getInstance(va),
				new SetValue(null, "x", StringProperty.getInstance(vb)));
		Element r1 = op.execute(context);

		// Check that the value is correct.
		assertTrue(r1 instanceof LongProperty);
		LongProperty s1 = (LongProperty) r1;
		long sresult = s1.getValue().longValue();
		assertTrue(va == sresult);

		// Check that the variable is set.
		r1 = context.getLocalVariable("x");
		assertTrue(r1 instanceof HashResource);
		HashResource dict = (HashResource) r1;
		Element r2 = dict.get(TermFactory.create(vb));
		assert (r2 instanceof LongProperty);
		LongProperty s2 = (LongProperty) r2;
		sresult = s2.getValue().longValue();
		assertTrue(va == sresult);
	}

	@Test
	public void referenceList() throws SyntaxException, InvalidTermException {

		Context context = new CompileTimeContext();

		long va = 1L;
		long vb = 1;

		// Execute the operations.
		Operation op = new Assign(null, LongProperty.getInstance(va),
				new SetValue(null, "x", LongProperty.getInstance(vb)));
		Element r1 = op.execute(context);

		// Check that the value is correct.
		assertTrue(r1 instanceof LongProperty);
		LongProperty s1 = (LongProperty) r1;
		long sresult = s1.getValue().longValue();
		assertTrue(va == sresult);

		// Check that the variable is set.
		r1 = context.getLocalVariable("x");
		assertTrue(r1 instanceof ListResource);
		ListResource list = (ListResource) r1;
		Element r2 = list.get(TermFactory.create(vb));
		assert (r2 instanceof LongProperty);
		LongProperty s2 = (LongProperty) r2;
		sresult = s2.getValue().longValue();
		assertTrue(va == sresult);

		// Make sure that an undef value was inserted.
		Element r3 = list.get(TermFactory.create(vb - 1));
		assert (r3 instanceof Undef);
	}

	@Test(expected = EvaluationException.class)
	public void illegalReference() throws SyntaxException {

		Context context = new CompileTimeContext();

		long va = 1L;
		String vb = "a";

		// Set the variable to a list.
		context.setLocalVariable("x", new ListResource());

		// Execute the operations.
		Operation op = new Assign(null, LongProperty.getInstance(va),
				new SetValue(null, "x", StringProperty.getInstance(vb)));

		op.execute(context);
	}

}
