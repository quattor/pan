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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/GlobalVariable.java $
 $Id: GlobalVariable.java 3620 2008-08-21 14:36:32Z loomis $
 */

package org.quattor.pan.utils;

import static org.quattor.pan.utils.MessageUtils.MSG_CANNOT_MODIFY_FINAL_GLOBAL_VARIABLE;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.EvaluationException;

/**
 * Holds the value of a global variable as well as a flag to indicate whether
 * this variable is 'final'.
 * 
 * @author loomis
 * 
 */
public class GlobalVariable {

	private boolean finalFlag = false;

	private Element value = null;

	private Element protectedValue = null;

	public GlobalVariable(boolean finalFlag, Element value) {
		assert (value != null);
		this.finalFlag = finalFlag;
		this.value = value;
		this.protectedValue = value.protect();
	}

	public void setValue(Element newValue) {
		assert (newValue != null);

		if (!finalFlag) {

			// Check that a consistent type is given.
			if (value != null) {
				value.checkValidReplacement(newValue);
			}

			// Save both a possibly unprotected and a protected value. The
			// unprotected value is used when referencing the value via SELF.
			// The protected value is used in all other cases. We need to save
			// the protected value at the same time because references must be
			// consistent for the first() and next() functions. (The reference
			// is used to index the valid iterators.)
			value = newValue;
			protectedValue = newValue.protect();

		} else {
			throw EvaluationException
					.create(MSG_CANNOT_MODIFY_FINAL_GLOBAL_VARIABLE);
		}

	}

	public Element getValue() {
		return protectedValue;
	}

	public Element getUnprotectedValue() {
		return value;
	}

	public void setFinalFlag(boolean finalFlag) {
		this.finalFlag = finalFlag;
	}

	public boolean getFinalFlag() {
		return finalFlag;
	}

	@Override
	public String toString() {
		return value.toString() + ((finalFlag) ? " (final)" : ""); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
