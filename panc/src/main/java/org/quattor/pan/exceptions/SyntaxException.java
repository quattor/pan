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

import java.io.File;

import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.utils.MessageUtils;

/**
 * Exceptions of this type are thrown lexing and parsing phases. A few of these
 * exceptions are thrown by the <code>Template</code> constructor, which makes
 * the more complex syntax checks.
 * 
 * @author loomis
 * 
 */
@SuppressWarnings("serial")
public class SyntaxException extends Exception {

	private SourceRange sourceRange;

	private File file;

	public SyntaxException(String message, SourceRange sourceRange) {
		this(message, sourceRange, null);
	}

	public SyntaxException(String message, SourceRange sourceRange, File file) {
		super(message);
		this.sourceRange = sourceRange;
		this.file = file;
	}

	public static SyntaxException create(SourceRange sourceRange,
			String msgkey, Object... args) {
		String msg = MessageUtils.format(msgkey, args);
		return new SyntaxException(msg, sourceRange);
	}

	public static SyntaxException create(SourceRange sourceRange, File file,
			String msgkey, Object... args) {
		String msg = MessageUtils.format(msgkey, args);
		return new SyntaxException(msg, sourceRange, file);
	}

	public static SyntaxException create(SourceRange sourceRange,
			EvaluationException ee) {
		String msg = ee.getSimpleMessage();
		return new SyntaxException(msg, sourceRange);
	}

	public SyntaxException addExceptionInfo(SourceRange sourceRange, File file) {
		if (this.sourceRange == null && sourceRange != null) {
			this.sourceRange = sourceRange;
		}
		if (this.file == null && file != null) {
			this.file = file;
		}
		return this;
	}

	public String getSimpleMessage() {
		return super.getMessage();
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder("syntax error [");
		sb.append((file != null) ? file.toString() : "?");
		sb.append(":");
		sb.append((sourceRange != null) ? sourceRange.toString() : "?");
		sb.append("]\n");
		sb.append(super.getMessage());
		sb.append("\n");
		return sb.toString();
	}

}
