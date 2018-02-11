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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/StringProperty.java $
 $Id: StringProperty.java 3595 2008-08-17 07:35:14Z loomis $
 */

package org.quattor.pan.dml.data;

import static org.quattor.pan.utils.MessageUtils.MSG_STRING_SIZE_OUTSIDE_RANGE;
import net.jcip.annotations.Immutable;

import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.utils.Range;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

/**
 * Represents a string value. This class implements the <code>Term</code>
 * interface; instances of this class can be used to dereference Resources.
 *
 * @author loomis
 *
 */
@Immutable
public class StringProperty extends Property implements Term {

	protected StringProperty(String value) {
		super(value);
	}

	public static StringProperty getInstance(String value) {
		return new StringProperty(value);
	}

	@Override
	public void checkRange(Range range) throws ValidationException {
		if (!range.isInRange(getValue().length())) {
			throw ValidationException.create(MSG_STRING_SIZE_OUTSIDE_RANGE,
					getValue().length(), range.toString());
		}
	}

	@Override
	public String getValue() {
		return (String) super.getValue();
	}

	@Override
	public String getTypeAsString() {
		return "string";
	}

    // should not be overridden by child, unless compareTo is also overridden
	public boolean isKey() {
		return true;
	}

	public Integer getIndex() throws InvalidTermException {
		throw new InvalidTermException(getKey());
	}

	public String getKey() throws InvalidTermException {
		return getValue();
	}

	public int compareTo(Term other) {
        // TermFactory.compare((Term) this, (Term) o) is way too expensive
        // Main use case is TreeMap<Term, ..> access, so optimise for that

		// Sanity check.
		if (other == null) {
			throw new NullPointerException();
		}

        // no self==other, equals() is expensive
        // and this is very unlikely to happen anyway

        // Other type
        if (!other.isKey()){
            return 1;
        }

		// Compare the underlying values.
		try {
            return getKey().compareTo(other.getKey());
		} catch (InvalidTermException consumed) {
			// This statement can never be reached because both objects are
			// either keys or indexes. This try/catch block is only here to make
			// the compiler happy.
			assert (false);
			return 0;
		}
	}

}
