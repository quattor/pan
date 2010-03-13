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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/exceptions/SystemException.java $
 $Id: SystemException.java 1351 2007-02-20 10:39:16Z loomis $
 */

package org.quattor.pan.exceptions;

import java.io.File;

/**
 * Exceptions of this type are thrown when basic system exceptions (for example,
 * IO exceptions) are encountered. The root cause of the exception is wrapped by
 * this exception.
 * 
 * @author loomis
 * 
 */
@SuppressWarnings("serial")
public class SystemException extends RuntimeException {

	private File file;

	public SystemException(String message) {
		this(message, null);
	}

	public SystemException(String message, File file) {
		super(message);
		this.file = file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	@Override
	public String getMessage() {
		String s = "system error [";
		s += (file != null) ? file.toString() : "<unknown>";
		s += "]\n";
		s += super.getMessage() + "\n";
		return s;
	}
}
