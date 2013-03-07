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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/MergeTest.java $
 $Id: MergeTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

public class MergeTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Merge.class);
	}

	@Test
	public void checkHashMerge() throws SyntaxException, InvalidTermException {

		HashResource dict1 = new HashResource();
		dict1.put(TermFactory.create("a"), StringProperty.getInstance("0"));
		dict1.put(TermFactory.create("b"), StringProperty.getInstance("1"));
		dict1.put(TermFactory.create("c"), StringProperty.getInstance("2"));

		HashResource dict2 = new HashResource();
		dict2.put(TermFactory.create("d"), StringProperty.getInstance("0"));
		dict2.put(TermFactory.create("e"), StringProperty.getInstance("1"));
		dict2.put(TermFactory.create("f"), StringProperty.getInstance("2"));

		// The actual keys should be the following.
		String[] trueKeys = new String[] { "a", "b", "c", "d", "e", "f" };

		Element r1 = runDml(Merge.getInstance(null, dict1, dict2));

		// Must be a hash.
		assertTrue(r1 instanceof HashResource);
		HashResource dictResult = (HashResource) r1;

		// Loop over all of the keys and make sure we get the correct ones.
		for (String key : trueKeys) {
			Term term = TermFactory.create(key);
			assertTrue(dictResult.get(term) != null);
		}
	}

	@Test
	public void checkListMerge() throws SyntaxException, InvalidTermException {

		ListResource list1 = new ListResource();
		list1.put(TermFactory.create(0), LongProperty.getInstance(0L));
		list1.put(TermFactory.create(1), LongProperty.getInstance(1L));
		list1.put(TermFactory.create(2), LongProperty.getInstance(2L));

		ListResource list2 = new ListResource();
		list2.put(TermFactory.create(0), LongProperty.getInstance(3L));
		list2.put(TermFactory.create(1), LongProperty.getInstance(4L));
		list2.put(TermFactory.create(2), LongProperty.getInstance(5L));

		Element r1 = runDml(Merge.getInstance(null, list1, list2));

		// Must be a list.
		assertTrue(r1 instanceof ListResource);
		ListResource listResult = (ListResource) r1;

		// Is the size correct?
		assertTrue(listResult.size() == 6);

		// Loop over all of the keys and make sure we get the correct ones.
		for (int i = 0; i < listResult.size(); i++) {
			Term term = TermFactory.create(i);
			Element li = listResult.get(term);

			assertTrue(li instanceof LongProperty);
			long r = ((LongProperty) li).getValue().longValue();
			assertTrue(((long) i) == r);
		}
	}

	@Test(expected = EvaluationException.class)
	public void checkCollision() throws SyntaxException, InvalidTermException {

		HashResource dict1 = new HashResource();
		dict1.put(TermFactory.create("a"), StringProperty.getInstance("0"));
		dict1.put(TermFactory.create("b"), StringProperty.getInstance("1"));

		HashResource dict2 = new HashResource();
		dict2.put(TermFactory.create("b"), StringProperty.getInstance("0"));
		dict2.put(TermFactory.create("c"), StringProperty.getInstance("1"));

		runDml(Merge.getInstance(null, dict1, dict2));
	}

	@Test(expected = EvaluationException.class)
	public void invalidArguments() throws SyntaxException {
		runDml(Merge.getInstance(null, LongProperty.getInstance(1L)));
	}

	@Test(expected = EvaluationException.class)
	public void mixedArguments1() throws SyntaxException {
		runDml(Merge.getInstance(null, new HashResource(), new ListResource()));
	}

	@Test(expected = EvaluationException.class)
	public void mixedArguments2() throws SyntaxException {
		runDml(Merge.getInstance(null, new ListResource(), new HashResource()));
	}

	@Test(expected = SyntaxException.class)
	public void invalidCallWithNoArguments() throws SyntaxException {
		Merge.getInstance(null);
	}

}
