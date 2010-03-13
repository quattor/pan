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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/KeyTest.java $
 $Id: KeyTest.java 3550 2008-08-02 14:54:26Z loomis $
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
import org.quattor.pan.utils.TermFactory;

public class KeyTest extends BuiltInFunctionTestUtils {

	private static final HashResource hashInstance;

	static {
		HashResource nlist = new HashResource();
		try {
			nlist.put(TermFactory.create("a"), StringProperty.getInstance("0"));
			nlist.put(TermFactory.create("b"), StringProperty.getInstance("1"));
		} catch (InvalidTermException consumed) {
		}

		hashInstance = nlist;
	}

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Key.class);
	}

	@Test
	public void checkCorrectKeys() throws SyntaxException {

		// The actual keys should be the following.
		String[] trueKeys = new String[] { "a", "b", "c" };

		// Loop over all of the keys and make sure we get the correct ones.
		for (int i = 0; i < hashInstance.size(); i++) {

			Element r1 = runDml(Key.getInstance(null, hashInstance,
					LongProperty.getInstance((long) i)));

			// Must be a string with the correct value.
			assertTrue(r1 instanceof StringProperty);
			String s = ((StringProperty) r1).getValue();
			assertTrue(trueKeys[i].equals(s));
		}
	}

	@Test(expected = EvaluationException.class)
	public void invalidNegativeIndex() throws SyntaxException {
		runDml(Key.getInstance(null, hashInstance, LongProperty
				.getInstance(-1L)));
	}

	@Test(expected = EvaluationException.class)
	public void invalidPositiveIndex() throws SyntaxException {
		runDml(Key.getInstance(null, hashInstance, LongProperty
				.getInstance((long) hashInstance.size())));
	}

	@Test(expected = EvaluationException.class)
	public void invalidHash() throws SyntaxException {
		runDml(Key.getInstance(null, new ListResource(), LongProperty
				.getInstance(0L)));
	}

	@Test(expected = SyntaxException.class)
	public void invalidCallWithNoArguments() throws SyntaxException {
		Key.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void invalidCallWithTooFewArguments() throws SyntaxException {
		Key.getInstance(null, hashInstance);
	}

	@Test(expected = SyntaxException.class)
	public void invalidCallWithTooManyArguments() throws SyntaxException {
		Key.getInstance(null, hashInstance, StringProperty.getInstance("a"),
				StringProperty.getInstance("b"));
	}

}
