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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/exceptions/EvaluationException.java $
 $Id: EvaluationException.java 1800 2007-06-10 07:05:07Z loomis $
 */

package org.quattor.pan.exceptions;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_PATH_DEREFERENCE;

import java.util.List;

import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.Path;
import org.quattor.pan.utils.Term;

/**
 * Exceptions of this type are thrown when an invalid term is encountered.
 * Either when an index is found where a key is expected or vice versa. This
 * exception must be caught and should not be visible to the end user. This is
 * intended to allow a better error message to be generated by collecting
 * information up the call stack.
 * 
 * @author loomis
 * 
 */
@SuppressWarnings("serial")
public class InvalidTermException extends Exception {

	private String errorPath = null;

	private String actualType = null;

	public InvalidTermException(String message) {
		super(message);
	}

	public InvalidTermException setInfo(Term[] terms, int index,
			String actualType) {

		this.actualType = actualType;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i <= index; i++) {
			if (i > 0) {
				sb.append("/");
			}
			sb.append(terms[i].toString());
		}
		errorPath = sb.toString();

		return this;
	}

	public InvalidTermException setVariableInfo(List<Term> terms, int index,
			String actualType) {

		this.actualType = actualType;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i <= index; i++) {
			sb.append("[");
			sb.append(terms.get(i).toString());
			sb.append("]");
		}
		errorPath = sb.toString();

		return this;
	}

	public String formatMessage(Path path) {

		// Create the prefix for the error path.
		StringBuilder sb = new StringBuilder();
		if (path.isAbsolute()) {
			sb.append("/");
		} else if (path.isExternal()) {
			sb.append(path.getAuthority());
			sb.append(":");
		}
		if (errorPath != null) {
			sb.append(errorPath);
		}

		return MessageUtils.format(MSG_INVALID_PATH_DEREFERENCE, actualType,
				getMessage(), sb.toString(), path.toString());
	}

	public String formatVariableMessage(String name, Term[] terms) {

		// Create the prefix for the error path.
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		if (terms != null) {
			for (Term term : terms) {
				sb.append("[");
				sb.append(term.toString());
				sb.append("]");
			}
		}

		return MessageUtils.format(MSG_INVALID_PATH_DEREFERENCE, actualType,
				getMessage(), name + errorPath, name);
	}

}
