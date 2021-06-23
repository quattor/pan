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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/exceptions/ValidationException.java $
 $Id: ValidationException.java 3454 2008-07-26 18:51:06Z loomis $
 */

package org.quattor.pan.exceptions;

import java.io.File;
import java.util.LinkedList;

import org.quattor.pan.dml.data.Property;
import org.quattor.pan.type.FullType;
import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.Path;
import org.quattor.pan.utils.Term;

/**
 * Exceptions of this type can be thrown during the validation phase of the
 * processing.
 *
 * @author loomis
 *
 */
@SuppressWarnings("serial")
public class ValidationException extends RuntimeException {

	private File objectTemplate;

	private Path path;

	private FullType boundType;

	private LinkedList<Term> terms;

	private LinkedList<String> typeStack;

	private Object value;

	private ValidationException(String message) {
		super(message);

		this.objectTemplate = null;
		path = null;
		boundType = null;

		terms = new LinkedList<Term>();
		typeStack = new LinkedList<String>();
		value = null;
	}

	public static ValidationException create(String msgkey, Object... args) {
		String msg = MessageUtils.format(msgkey, args);
		return new ValidationException(msg);
	}

    /* create exception with value from data */
	public static ValidationException createv(Object data, String msgkey, Object... args) {
		String msg = MessageUtils.format(msgkey, args);
		ValidationException ve = new ValidationException(msg);
        ve.setValue(data);
        return ve;
	}

	public ValidationException setObjectTemplate(File objectTemplate) {
		this.objectTemplate = objectTemplate;
		return this;
	}

	@Override
	public String getMessage() {

		// This is the path of the element that caused the validation error.
		Path rPath = null;
		if (path != null) {
			try {
				rPath = new Path(path, terms.toArray(new Term[terms.size()]));
			} catch (SyntaxException consumed) {
				// This may cause the reported path to be incorrect, but this
				// information is probably better than nothing.
				rPath = path;
			}
		}

		StringBuilder sb = new StringBuilder("validation error [");
		sb.append((objectTemplate != null) ? objectTemplate.toString()
				: "?");
		sb.append("]\n");
		sb.append(super.getMessage());
		sb.append("\n");

		if (rPath != null) {
			sb.append("element path: '");
			sb.append(rPath.toString());
			sb.append("'\n");
		}
		if (value != null) {
            sb.append("element value: ");
            sb.append(value.toString());
            sb.append("\n");
		}

		for (String s : typeStack) {
			sb.append(s);
		}

		if (path != null && boundType != null) {
			sb.append("path '");
			sb.append(path);
			sb.append("' bound to type ");
			sb.append(boundType.getTypeName());
			sb.append(" in [");
			sb.append(boundType.getSource());
			sb.append(":");
			sb.append(boundType.getSourceRange());
			sb.append("]\n");
		}

		if (getCause() != null) {
			sb.append("caused by: ");
			sb.append(getCause().getMessage());
		}

		return sb.toString();
	}

	public ValidationException setPathTypeAndObject(Path path, FullType type,
			File objectTemplate) {
		this.path = path;
		this.boundType = type;
		if (this.objectTemplate == null && objectTemplate != null) {
			this.objectTemplate = objectTemplate;
		}
		return this;
	}

	public ValidationException addTerm(Term term) {
		assert (term != null);
		terms.addFirst(term);
		return this;
	}

	public ValidationException addTypeToStack(String name, FullType type) {

		String source = "?";
		String sourceRange = "?";

		if (type != null) {
			source = type.getSource();
			sourceRange = type.getSourceRange();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("type: '");
		sb.append(name);
		sb.append("' [");
		sb.append(source);
		sb.append(":");
		sb.append(sourceRange);
		sb.append("]\n");

		typeStack.add(sb.toString());

		return this;
	}

	public ValidationException setValue(Object data) {
		this.value = data;
		return this;
	}

}
