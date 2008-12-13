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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/DoubleProperty.java $
 $Id: DoubleProperty.java 3595 2008-08-17 07:35:14Z loomis $
 */

package org.quattor.pan.dml.data;

import static org.quattor.pan.utils.MessageUtils.MSG_NUMBER_OUTSIDE_RANGE;
import net.jcip.annotations.Immutable;

import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.utils.Range;

/**
 * Represents a double value.
 * 
 * @author loomis
 * 
 */
@Immutable
final public class DoubleProperty extends NumberProperty {

	private static final long serialVersionUID = -1321326531527084560L;

	private DoubleProperty(Double value) {
		super(value);
	}

	public static DoubleProperty getInstance(double value) {
		return new DoubleProperty(Double.valueOf(value));
	}

	public static DoubleProperty getInstance(Double value) {
		return new DoubleProperty(value);
	}

	public static DoubleProperty getInstance(String value) {
		return new DoubleProperty(Double.parseDouble(value));
	}

	@Override
	public void checkRange(Range range) throws ValidationException {
		if (!range.isInRange(doubleValue())) {
			throw ValidationException.create(MSG_NUMBER_OUTSIDE_RANGE,
					getValue(), range.toString());
		}
	}

	@Override
	public double doubleValue() {
		return getValue().doubleValue();
	}

	@Override
	public Double getValue() {
		return (Double) super.getValue();
	}

	@Override
	public String getTypeAsString() {
		return "double";
	}
}
