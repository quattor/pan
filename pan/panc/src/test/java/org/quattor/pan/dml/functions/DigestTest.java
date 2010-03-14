/*
 Copyright (c) 2009 Charles A. Loomis, Jr, Cedric Duprilot, and
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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/EscapeTest.java $
 $Id: EscapeTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.SyntaxException;

public class DigestTest extends BuiltInFunctionTestUtils {

	final private static StringProperty[] validNames = {
			StringProperty.getInstance("MD2"),
			StringProperty.getInstance("MD5"),
			StringProperty.getInstance("SHA"),
			StringProperty.getInstance("SHA-1"),
			StringProperty.getInstance("SHA-256"),
			StringProperty.getInstance("SHA-384"),
			StringProperty.getInstance("SHA-512") };

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Digest.class);
	}

	@Test(expected = SyntaxException.class)
	public void testNoArguments() throws SyntaxException {
		Digest.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void testOneArgument() throws SyntaxException {
		Digest.getInstance(null, StringProperty.getInstance("BAD"));
	}

	@Test(expected = SyntaxException.class)
	public void testThreeArguments() throws SyntaxException {
		Digest.getInstance(null, StringProperty.getInstance("BAD"),
				StringProperty.getInstance("BAD"), StringProperty
						.getInstance("BAD"));
	}

	@Test(expected = SyntaxException.class)
	public void testInvalidFirstArgument1() throws SyntaxException {
		Digest.getInstance(null, StringProperty.getInstance("BAD"),
				StringProperty.getInstance("OK"));
	}

	@Test(expected = SyntaxException.class)
	public void testInvalidFirstArgument2() throws SyntaxException {
		Digest.getInstance(null, BooleanProperty.TRUE, StringProperty
				.getInstance("OK"));
	}

	@Test(expected = SyntaxException.class)
	public void testInvalidSecondArgument() throws SyntaxException {
		Digest.getInstance(null, StringProperty.getInstance("OK"),
				BooleanProperty.TRUE);
	}

	@Test
	public void testValidAlgorithmNames() throws SyntaxException {

		StringProperty message = StringProperty.getInstance("");

		for (StringProperty algorithm : validNames) {
			Digest.getInstance(null, algorithm, message);
		}
	}

	@Test
	public void testGeneratedValues() throws SyntaxException,
			NoSuchAlgorithmException {

		StringProperty message = StringProperty.getInstance("OK");

		for (StringProperty algorithm : validNames) {

			MessageDigest m = MessageDigest.getInstance(algorithm.getValue());
			m.update(message.getValue().getBytes(), 0, message.getValue()
					.length());
			String correct = new BigInteger(1, m.digest()).toString(16);

			Operation op = Digest.getInstance(null, algorithm, message);
			Element e = runDml(op);

			// Check that result is a StringProperty with the correct value.
			assertTrue(e instanceof StringProperty);
			String s = ((StringProperty) e).getValue();
			assertTrue(correct.equals(s));

		}
	}
}
