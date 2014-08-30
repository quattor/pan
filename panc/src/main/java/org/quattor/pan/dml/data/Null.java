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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/Null.java $
 $Id: Null.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.dml.data;

import static org.quattor.pan.utils.MessageUtils.MSG_NULL_ELEMENT_IN_CONFIGURATION;

import java.io.ObjectStreamException;

import net.jcip.annotations.Immutable;

import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;

/**
 * Represents a null value in the pan language.
 *
 * @author loomis
 *
 */
@Immutable
public class Null extends TransientElement {

	public static final Null VALUE = new Null();

	private Null() {
	}

	public Object readResolve() throws ObjectStreamException {
		return VALUE;
	}

	static public Null getInstance() {
		return VALUE;
	}

	@Override
	public void checkValidReplacement(Element newValue)
			throws EvaluationException {

		// Anything can replace a null value. This method doesn't need to do
		// anything.
	}

	@Override
	public String getTypeAsString() {
		return "null";
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(VALUE);
	}

	@Override
	public boolean equals(Object o) {
		return (this == o);
	}

	@Override
	public String toString() {
		return "null";
	}

}
