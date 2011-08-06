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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/ToStringTest.java $
 $Id: ToStringTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.SyntaxException;

public class ToStringTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(ToString.class);
	}

	@Test
	public void convertString() throws SyntaxException {

		String s = "OK";
		Element r1 = runDml(ToString.getInstance(null, StringProperty
				.getInstance(s)));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		StringProperty s1 = (StringProperty) r1;
		String sresult = s1.getValue();
		assertTrue(s.equals(sresult));
	}

	@Test
	public void convertLong() throws SyntaxException {

		Long s = Long.valueOf(1024L);
		Element r1 = runDml(ToString.getInstance(null, LongProperty
				.getInstance(s)));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		StringProperty s1 = (StringProperty) r1;
		String sresult = s1.getValue();
		assertTrue(s.equals(Long.parseLong(sresult)));
	}

	@Test
	public void convertDouble() throws SyntaxException {

		Double s = Double.valueOf(3.14159);
		Element r1 = runDml(ToString.getInstance(null, DoubleProperty
				.getInstance(s)));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		StringProperty s1 = (StringProperty) r1;
		String sresult = s1.getValue();
		assertTrue(s.equals(Double.parseDouble(sresult)));
	}

	@Test
	public void convertBooleans() throws SyntaxException {

		boolean s = true;
		Element r1 = runDml(ToString.getInstance(null, BooleanProperty
				.getInstance(s)));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		StringProperty s1 = (StringProperty) r1;
		String sresult = s1.getValue();
		assertTrue(s == Boolean.parseBoolean(sresult));

		s = false;
		r1 = runDml(ToString.getInstance(null, BooleanProperty.getInstance(s)));

		// Check result.
		assertTrue(r1 instanceof StringProperty);
		s1 = (StringProperty) r1;
		sresult = s1.getValue();
		assertTrue(s == Boolean.parseBoolean(sresult));
	}

	@Test(expected = SyntaxException.class)
	public void tooFewArguments() throws SyntaxException {
		ToString.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void tooManyArguments() throws SyntaxException {
		ToString.getInstance(null, StringProperty.getInstance("OK"),
				StringProperty.getInstance("OK"));
	}

}
