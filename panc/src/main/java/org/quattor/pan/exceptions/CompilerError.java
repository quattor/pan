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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/exceptions/CompilerError.java $
 $Id: CompilerError.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.exceptions;

import org.quattor.pan.utils.MessageUtils;

/**
 * This error is thrown only for conditions which should never occur. They
 * indicate an internal compiler error and should always be reported as a bug
 * when they occur.
 * 
 * @author loomis
 * 
 */
@SuppressWarnings("serial")
public class CompilerError extends Error {

	/**
	 * Creates a raw CompilerError directly from an unlocalized message. This
	 * should only be used for problems arising from the use of the message
	 * resource bundles. All others should use the static create method.
	 * 
	 * @param message
	 */
	public CompilerError(String message) {
		super(message);
	}

	/**
	 * Preferred mechanism for creating a localized CompilerError. Except for
	 * messages related to the resource bundle itself, this method should be
	 * used.
	 * 
	 * @param msgkey
	 * @param args
	 * @return new localized instance of CompilerError
	 */
	public static CompilerError create(String msgkey, Object... args) {
		String msg = MessageUtils.format(msgkey, args);
		return new CompilerError(msg);
	}

}
