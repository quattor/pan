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
 $Id: ObjectContextTest.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.template;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

public class ObjectContextTest {

	@Test
	public void setVariableXLong() throws SyntaxException {

		Context context = new CompileTimeContext();

		// Set the variable.
		long va = 1L;
		context.setLocalVariable("x", LongProperty.getInstance(va));

		// Ensure that the variable exists.
		Element value = context.getLocalVariable("x");
		assertTrue(value != null);
		assertTrue(value instanceof LongProperty);
		long r1 = ((LongProperty) value).getValue().longValue();
		assertTrue(r1 == va);
	}

	@Test
	public void setVariableXDouble() throws SyntaxException {

		Context context = new CompileTimeContext();

		// Set the variable.
		long va = 1L;
		double vb = 10.5;
		context.setLocalVariable("x", LongProperty.getInstance(va));

		// Replacing with undef should be OK.
		context.setLocalVariable("x", Undef.VALUE);

		// Now try replacing with a double.
		context.setLocalVariable("x", DoubleProperty.getInstance(vb));

		// Ensure that the variable exists.
		Element value = context.getLocalVariable("x");
		assertTrue(value != null);
		assertTrue(value instanceof DoubleProperty);
		double r2 = ((DoubleProperty) value).getValue().doubleValue();
		assertTrue(r2 == vb);

	}

	@Test(expected = EvaluationException.class)
	public void simpleIllegalValueReplacement() throws SyntaxException {

		Context context = new CompileTimeContext();

		// Set the variable.
		long va = 1L;
		double vb = 10.5;
		context.setLocalVariable("x", LongProperty.getInstance(va));

		// Check that replacing a value with an incorrect type causes an error.
		context.setLocalVariable("x", DoubleProperty.getInstance(vb));
	}

	@Test
	public void hashValueReplacement() throws SyntaxException,
			InvalidTermException {

		Context context = new CompileTimeContext();

		// Create a nlist to check.
		HashResource nlist = new HashResource();
		nlist.put(TermFactory.create("a"), LongProperty.getInstance(1L));
		context.setLocalVariable("x", nlist);

		// Check that replacing a value with an incorrect type causes an error.
		List<Term> terms = new ArrayList<Term>();
		terms.add(TermFactory.create("a"));
		context.setLocalVariable("x", terms, LongProperty.getInstance(2L));

		// Ensure that the variable exists.
		Element value = context.getLocalVariable("x");
		assertTrue(value != null);
		assertTrue(value instanceof HashResource);
		Element child = ((HashResource) value).get(TermFactory.create("a"));
		assertTrue(child instanceof LongProperty);
		assertTrue(((LongProperty) child).getValue().longValue() == 2L);

	}

	@Test(expected = EvaluationException.class)
	public void hashIllegalValueReplacement() throws SyntaxException,
			InvalidTermException {

		Context context = new CompileTimeContext();

		// Create a nlist to check.
		HashResource nlist = new HashResource();
		nlist.put(TermFactory.create("a"), LongProperty.getInstance(1L));
		context.setLocalVariable("x", nlist);

		// Check that replacing a value with an incorrect type causes an error.
		List<Term> terms = new ArrayList<Term>();
		terms.add(TermFactory.create("a"));
		context.setLocalVariable("x", terms, DoubleProperty.getInstance(2.0));
	}

	@Test
	public void listValueReplacement() throws SyntaxException,
			InvalidTermException {

		Context context = new CompileTimeContext();

		// Create a list to check.
		ListResource list = new ListResource();
		list.put(TermFactory.create(0), LongProperty.getInstance(1L));
		context.setLocalVariable("x", list);

		// Check that replacing a value with an incorrect type causes an error.
		List<Term> terms = new ArrayList<Term>();
		terms.add(TermFactory.create(0));
		context.setLocalVariable("x", terms, LongProperty.getInstance(2L));

		// Ensure that the variable exists.
		Element value = context.getLocalVariable("x");
		assertTrue(value != null);
		assertTrue(value instanceof ListResource);
		Element child = ((ListResource) value).get(TermFactory.create(0));
		assertTrue(child instanceof LongProperty);
		assertTrue(((LongProperty) child).getValue().longValue() == 2L);
	}

	@Test(expected = EvaluationException.class)
	public void listIllegalValueReplacement() throws SyntaxException,
			InvalidTermException {

		Context context = new CompileTimeContext();

		// Create a list to check.
		ListResource list = new ListResource();
		list.put(TermFactory.create(0), LongProperty.getInstance(1L));
		context.setLocalVariable("x", list);

		// Check that replacing a value with an incorrect type causes an error.
		List<Term> terms = new ArrayList<Term>();
		terms.add(TermFactory.create(0));
		context.setLocalVariable("x", terms, DoubleProperty.getInstance(2.0));
	}

	@Test
	public void autoCreateFromUndef() throws SyntaxException,
			InvalidTermException {

		Context context = new CompileTimeContext();

		// Create an undef to check.
		context.setLocalVariable("x", Undef.VALUE);

		// Check that replacing a value with an incorrect type causes an error.
		List<Term> terms = new ArrayList<Term>();
		terms.add(TermFactory.create("a"));
		context.setLocalVariable("x", terms, LongProperty.getInstance(2L));

		// Ensure that the variable exists.
		Element value = context.getLocalVariable("x");
		assertTrue(value != null);
		assertTrue(value instanceof HashResource);
		Element child = ((HashResource) value).get(TermFactory.create("a"));
		assertTrue(child instanceof LongProperty);
		assertTrue(((LongProperty) child).getValue().longValue() == 2L);

	}
}
