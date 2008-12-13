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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/NumberProperty.java $
 $Id: NumberProperty.java 3595 2008-08-17 07:35:14Z loomis $
 */

package org.quattor.pan.dml.data;

import net.jcip.annotations.Immutable;

/**
 * Implements a superclass for all primitive pan numbers (long and double).
 * 
 * @author loomis
 * 
 */
@Immutable
abstract public class NumberProperty extends Property {

	private static final long serialVersionUID = 7614195834501343311L;

	protected NumberProperty(Number value) {
		super(value);
	}

	/**
	 * All number classes must implement this method which is used to facilitate
	 * type conversion in mixed-type, arithmetic expressions.
	 * 
	 * @return value of this number as a double
	 */
	abstract public double doubleValue();
}
