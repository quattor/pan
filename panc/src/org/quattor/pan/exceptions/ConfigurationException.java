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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/exceptions/SyntaxException.java $
 $Id: SyntaxException.java 2799 2008-01-26 17:10:23Z loomis $
 */

package org.quattor.pan.exceptions;

import org.quattor.pan.utils.MessageUtils;

/**
 * Identifies a problem with the configuration of the compiler. This is usually
 * the result of inconsistent or incomplete options being given.
 * 
 * @author loomis
 * 
 */
public class ConfigurationException extends Exception {

	private static final long serialVersionUID = -5178408787782445383L;

	protected ConfigurationException(String message) {
		super(message);
	}

	public static ConfigurationException create(String msgkey, Object... args) {
		String msg = MessageUtils.format(msgkey, args);
		return new ConfigurationException(msg);
	}

}
