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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/IndexTest.java $
 $Id: IndexTest.java 1149 2007-01-20 13:50:39Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;

public class FormatTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(Format.class);
	}

	@Test
	public void checkOctalConversion() throws SyntaxException {

		Element r = runDml(Format.getInstance(null, StringProperty
				.getInstance("%o"), LongProperty.getInstance(15L)));

		assertTrue(r instanceof StringProperty);
		String s = ((StringProperty) r).getValue();
		assertTrue("17".equals(s));

	}

	@Test
	public void checkLowerHexConversion() throws SyntaxException {

		Element r = runDml(Format.getInstance(null, StringProperty
				.getInstance("%x"), LongProperty.getInstance(15L)));

		assertTrue(r instanceof StringProperty);
		String s = ((StringProperty) r).getValue();
		assertTrue("f".equals(s));

	}

	@Test
	public void checkUpperHexConversion() throws SyntaxException {

		Element r = runDml(Format.getInstance(null, StringProperty
				.getInstance("%X"), LongProperty.getInstance(15L)));

		assertTrue(r instanceof StringProperty);
		String s = ((StringProperty) r).getValue();
		assertTrue("F".equals(s));

	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments1() throws SyntaxException {
		runDml(Format.getInstance(null, LongProperty.getInstance(1L)));
	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments2() throws SyntaxException {
		runDml(Format.getInstance(null, StringProperty.getInstance("%d")));
	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments3() throws SyntaxException {
		runDml(Format.getInstance(null, StringProperty.getInstance("%d"),
				StringProperty.getInstance("bad")));
	}

	@Test(expected = SyntaxException.class)
	public void tooFewArguments() throws SyntaxException {
		Format.getInstance(null);
	}

}
