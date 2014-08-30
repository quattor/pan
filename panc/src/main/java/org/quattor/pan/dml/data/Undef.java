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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/Undef.java $
 $Id: Undef.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.dml.data;

import java.io.ObjectStreamException;

import net.jcip.annotations.Immutable;

import org.quattor.pan.exceptions.EvaluationException;

/**
 * Represents the undef value in the pan language.
 *
 * @author loomis
 *
 */
@Immutable
public class Undef extends TransientElement {

	public static final Undef VALUE = new Undef();

	private Undef() {
	}

	public Object readResolve() throws ObjectStreamException {
		return VALUE;
	}

	static public Undef getInstance() {
		return VALUE;
	}

	@Override
	public void checkValidReplacement(Element newValue)
			throws EvaluationException {

		// Anything can replace an undef value. This method doesn't need to do
		// anything.
	}

	@Override
	public String getTypeAsString() {
		return "undef";
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
		return "undef";
	}

}
