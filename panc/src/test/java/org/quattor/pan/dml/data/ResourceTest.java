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

$HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/ListResourceTest.java $
$Id: ListResourceTest.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.dml.data;

import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

public class ResourceTest {

	@Test(expected = CompilerError.class)
	public void testGetListOnProtectedList() throws InvalidTermException {
		Term[] terms = { TermFactory.create(0L) };

		ListResource list = new ListResource();
		list = (ListResource) list.protect();
		list.rgetList(terms, 0);
	}

	@Test(expected = CompilerError.class)
	public void testGetListOnProtectedHash() throws InvalidTermException {
		Term[] terms = { TermFactory.create("a") };

		HashResource hash = new HashResource();
		hash = (HashResource) hash.protect();
		hash.rgetList(terms, 0);
	}

	@Test
	public void testGetListCreateOnList() throws InvalidTermException {

		Term term = TermFactory.create(0L);
		Term[] terms = { term };

		Resource root = new ListResource();
		ListResource list = root.rgetList(terms, 0);

		// Verify that the returned list was created and is empty.
		assertTrue(list != null);
		assertTrue(list.size() == 0);

		// Extract the child from the parent and ensure it is the same object.
		Element child = root.get(term);
		assertTrue(child == list);
	}

	@Test
	public void testGetListCreateOnHash() throws InvalidTermException {

		Term term = TermFactory.create("a");
		Term[] terms = { term };

		Resource root = new HashResource();
		ListResource list = root.rgetList(terms, 0);

		// Verify that the returned list was created and is empty.
		assertTrue(list != null);
		assertTrue(list.size() == 0);

		// Extract the child from the parent and ensure it is the same object.
		Element child = root.get(term);
		assertTrue(child == list);
	}

	@Test
	public void testGetListCreateOnListWithUndef() throws InvalidTermException {

		Term term = TermFactory.create(0L);
		Term[] terms = { term };

		Resource root = new ListResource();
		root.put(term, Undef.getInstance());
		ListResource list = root.rgetList(terms, 0);

		// Verify that the returned list was created and is empty.
		assertTrue(list != null);
		assertTrue(list.size() == 0);

		// Extract the child from the parent and ensure it is the same object.
		Element child = root.get(term);
		assertTrue(child == list);
	}

	@Test
	public void testGetListCreateOnHashWithUndef() throws InvalidTermException {

		Term term = TermFactory.create("a");
		Term[] terms = { term };

		Resource root = new HashResource();
		root.put(term, Undef.getInstance());
		ListResource list = root.rgetList(terms, 0);

		// Verify that the returned list was created and is empty.
		assertTrue(list != null);
		assertTrue(list.size() == 0);

		// Extract the child from the parent and ensure it is the same object.
		Element child = root.get(term);
		assertTrue(child == list);
	}

	@Test
	public void testGetListMultilevelCreateOnList() throws InvalidTermException {

		Term[] terms = { TermFactory.create(0L), TermFactory.create(0L) };

		Resource root = new ListResource();
		ListResource list = root.rgetList(terms, 0);

		// Verify that the returned list was created and is empty.
		assertTrue(list != null);
		assertTrue(list.size() == 0);

		// Extract the child from the parent and ensure it is the same object.
		Element child = ((Resource) root.get(terms[0])).get(terms[1]);
		assertTrue(child == list);
	}

	@Test
	public void testGetListMultilevelCreateOnHash() throws InvalidTermException {

		Term[] terms = { TermFactory.create("a"), TermFactory.create(0L) };

		Resource root = new HashResource();
		ListResource list = root.rgetList(terms, 0);

		// Verify that the returned list was created and is empty.
		assertTrue(list != null);
		assertTrue(list.size() == 0);

		// Extract the child from the parent and ensure it is the same object.
		Element child = ((Resource) root.get(terms[0])).get(terms[1]);
		assertTrue(child == list);
	}

	@Test(expected = EvaluationException.class)
	public void testGetListOnPropertyInList() throws InvalidTermException {

		Term term = TermFactory.create(0L);
		Term[] terms = { term };

		Resource root = new ListResource();
		root.put(term, StringProperty.getInstance("BAD"));
		root.rgetList(terms, 0);
	}

	@Test(expected = EvaluationException.class)
	public void testGetListOnPropertyInHash() throws InvalidTermException {

		Term term = TermFactory.create("a");
		Term[] terms = { term };

		Resource root = new HashResource();
		root.put(term, StringProperty.getInstance("BAD"));
		root.rgetList(terms, 0);
	}

}
