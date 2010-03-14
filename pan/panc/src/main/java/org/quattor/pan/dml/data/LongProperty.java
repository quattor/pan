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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/LongProperty.java $
 $Id: LongProperty.java 3595 2008-08-17 07:35:14Z loomis $
 */

package org.quattor.pan.dml.data;

import static org.quattor.pan.utils.MessageUtils.MSG_NUMBER_OUTSIDE_RANGE;
import net.jcip.annotations.Immutable;

import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.utils.Range;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

/**
 * Represents a long value. This implements the <code>Term</code> interface and
 * may be used to reference a resource.
 * 
 * @author loomis
 * 
 */
@Immutable
final public class LongProperty extends NumberProperty implements Term {

	private LongProperty(Long value) {
		super(value);
	}

	public static LongProperty getInstance(long value) {
		return new LongProperty(Long.valueOf(value));
	}

	public static LongProperty getInstance(Long value) {
		return new LongProperty(value);
	}

	public static LongProperty getInstance(String value) {
		return new LongProperty(Long.decode(value));
	}

	@Override
	public void checkRange(Range range) throws ValidationException {
		if (!range.isInRange(getValue().longValue())) {
			throw ValidationException.create(MSG_NUMBER_OUTSIDE_RANGE,
					getValue(), range.toString());
		}
	}

	@Override
	public double doubleValue() {
		return getValue().doubleValue();
	}

	@Override
	public Long getValue() {
		return (Long) super.getValue();
	}

	@Override
	public String getTypeAsString() {
		return "long";
	}

	public boolean isKey() {
		return false;
	}

	public Integer getIndex() throws InvalidTermException {
		return Integer.valueOf(getValue().intValue());
	}

	public String getKey() throws InvalidTermException {
		throw new InvalidTermException(getIndex().toString());
	}

	public int compareTo(Term o) {
		return TermFactory.compare((Term) this, (Term) o);
	}

}
