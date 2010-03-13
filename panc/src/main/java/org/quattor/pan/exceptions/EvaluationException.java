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
 $Id: EvaluationException.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.exceptions;

import java.io.File;

import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.template.Template;
import org.quattor.pan.utils.MessageUtils;

/**
 * Exceptions of this type can be thrown during the construction of the pan
 * language profiles. They can also be thrown during the evaluation of
 * compile-time constants or when setting default values.
 * 
 * @author loomis
 * 
 */
@SuppressWarnings("serial")
public class EvaluationException extends RuntimeException {

	private SourceRange sourceRange;

	private File file;

	private String traceback;

	public EvaluationException(String message) {
		this(message, (SourceRange) null, (File) null);
	}

	public EvaluationException(String message, SourceRange sourceRange) {
		this(message, sourceRange, (File) null);
	}

	public EvaluationException(String message, SourceRange sourceRange,
			File file) {
		this(message, sourceRange, file, null);
	}

	public EvaluationException(String message, SourceRange sourceRange,
			File file, String traceback) {
		super(message);
		this.sourceRange = sourceRange;
		this.file = file;
		this.traceback = traceback;
	}

	public EvaluationException(String message, SourceRange sourceRange,
			Context context) {
		super(message);

		this.sourceRange = sourceRange;

		if (context != null) {
			Template current = context.getCurrentTemplate();
			if (current != null) {
				this.file = current.source;

				if (sourceRange != null) {
					this.traceback = context.getTraceback(sourceRange);
				}
			}
		}

	}

	public static EvaluationException create(SourceRange sourceRange,
			String msgkey, Object... args) {
		String msg = MessageUtils.format(msgkey, args);
		return new EvaluationException(msg, sourceRange);
	}

	public static EvaluationException create(String msgkey, Object... args) {
		String msg = MessageUtils.format(msgkey, args);
		return new EvaluationException(msg);
	}

	public static EvaluationException create(SourceRange sourceRange,
			File file, String msgkey, Object... args) {
		String msg = MessageUtils.format(msgkey, args);
		return new EvaluationException(msg, sourceRange, file);
	}

	public static EvaluationException create(SourceRange sourceRange,
			Context context, String msgkey, Object... args) {
		String msg = MessageUtils.format(msgkey, args);
		return new EvaluationException(msg, sourceRange, context);
	}

	public static EvaluationException create(SourceRange sourceRange,
			EvaluationException ee) {
		String msg = ee.getSimpleMessage();
		return new EvaluationException(msg, sourceRange);
	}

	public EvaluationException addExceptionInfo(SourceRange sourceRange,
			File file, String traceback) {
		if (this.sourceRange == null && sourceRange != null) {
			this.sourceRange = sourceRange;
		}
		if (this.file == null && file != null) {
			this.file = file;
		}
		if (this.traceback == null && traceback != null) {
			this.traceback = traceback;
		}
		return this;
	}

	public EvaluationException addExceptionInfo(SourceRange sourceRange,
			Context context) {

		boolean changed = false;

		// Update the source range if given and it isn't already
		// defined.
		if (this.sourceRange == null && sourceRange != null) {
			this.sourceRange = sourceRange;
			changed = true;
		}

		// Update the current template file if the context is
		// given and it isn't already defined.
		if (file == null && context != null) {
			Template current = context.getCurrentTemplate();
			if (current != null) {
				this.file = current.source;
				changed = true;
			}
		}

		// Only update the traceback if a change has been made to the
		// source location and all information is available.
		if (changed && sourceRange != null && context != null) {
			this.traceback = context.getTraceback(sourceRange);
		}

		return this;
	}

	public String getSimpleMessage() {
		return super.getMessage();
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder("evaluation error [");
		sb.append((file != null) ? file.toString() : "<unknown>");
		sb.append(":");
		sb.append((sourceRange != null) ? sourceRange.toString() : "<unknown>");
		sb.append("]\n");
		sb.append(super.getMessage());
		sb.append("\n");
		if (traceback != null) {
			sb.append(traceback);
			sb.append("\n");
		}
		return sb.toString();
	}

}
