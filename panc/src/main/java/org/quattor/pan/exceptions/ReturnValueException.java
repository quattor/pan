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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/exceptions/ReturnValueException.java $
 $Id: ReturnValueException.java 1351 2007-02-20 10:39:16Z loomis $
 */

package org.quattor.pan.exceptions;

import org.quattor.pan.dml.data.Element;

/**
 * Exceptions of this type are thrown when executing a DML return statement.
 * Implementing the return statement in this way allows most of the stack
 * information to be kept within the virtual machine.
 *
 * @author loomis
 *
 */
@SuppressWarnings("serial")
public class ReturnValueException extends RuntimeException {

	final private Element element;

	public ReturnValueException(Element element) {
		super("ReturnValueException");
		this.element = element;
	}

	public Element getElement() {
		return element;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + " [" + element + "]";
	}

}
