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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/GlobalVariableTest.java $
 $Id: GlobalVariableTest.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;

public class GlobalVariableTest {

	@Test
	public void createGlobalVariable() {

		GlobalVariable gvar = new GlobalVariable(false, BooleanProperty
				.getInstance(false));

		assertFalse(gvar.getFinalFlag());
		assertTrue(gvar.getValue() instanceof BooleanProperty);
		assertFalse(((BooleanProperty) gvar.getValue()).getValue()
				.booleanValue());

		gvar = new GlobalVariable(true, BooleanProperty.getInstance(true));

		assertTrue(gvar.getFinalFlag());
		assertTrue(gvar.getValue() instanceof BooleanProperty);
		assertTrue(((BooleanProperty) gvar.getValue()).getValue()
				.booleanValue());
	}

	@Test(expected = EvaluationException.class)
	public void illegalModificationOfFinalVariable() {

		GlobalVariable gvar = new GlobalVariable(true, BooleanProperty
				.getInstance(true));

		gvar.setValue(BooleanProperty.getInstance(false));
	}

	@Test
	public void validVariableReplacements() {

		GlobalVariable gvar = new GlobalVariable(false, BooleanProperty
				.getInstance(true));

		gvar.setValue(Undef.VALUE);
		gvar.setValue(Null.VALUE);
		gvar.setValue(LongProperty.getInstance(1L));
		gvar.setValue(LongProperty.getInstance(10L));

		assertFalse(gvar.getFinalFlag());
		assertTrue(gvar.getValue() instanceof LongProperty);
		assertTrue(((LongProperty) gvar.getValue()).getValue().longValue() == 10L);
	}

	@Test(expected = EvaluationException.class)
	public void illegalVariableReplacement() {

		GlobalVariable gvar = new GlobalVariable(true, BooleanProperty
				.getInstance(true));

		gvar.setValue(LongProperty.getInstance(1L));
	}

}
