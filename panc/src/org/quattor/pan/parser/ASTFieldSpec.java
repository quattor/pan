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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/parser/ASTFieldSpec.java $
 $Id: ASTFieldSpec.java 3515 2008-07-31 13:20:05Z loomis $
 */

package org.quattor.pan.parser;

import static org.quattor.pan.utils.MessageUtils.MSG_FIELD_MUST_BE_VALID_KEY;
import static org.quattor.pan.utils.MessageUtils.MSG_FIELD_MUST_BE_VALID_PATH;
import static org.quattor.pan.utils.MessageUtils.MSG_FIELD_MUST_BE_VALID_TERM;

import java.util.List;

import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.utils.Path;
import org.quattor.pan.utils.Term;

public class ASTFieldSpec extends SimpleNode {

	private boolean required = false;

	private String path = null;

	private String include = null;

	public ASTFieldSpec(int id) {
		super(id);
	}

	public ASTFieldSpec(PanParser p, int id) {
		super(p, id);
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isRequired() {
		return required;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Term getKey() throws SyntaxException {

		// This method checks that the path is a valid nlist key before
		// returning
		// the value. This check is done here because SyntaxException is not a
		// RuntimeException and the JavaCC generated parser can't handle it
		// correctly. The higher-level code that calls this method can correctly
		// handle the exception.

		// Verify that the path has the correct form.
		Term t = null;
		try {
			Path p = new Path(path);
			List<Term> terms = p.getTerms();
			if (terms.size() != 1) {
				throw SyntaxException.create(getSourceRange(),
						MSG_FIELD_MUST_BE_VALID_TERM, path);
			}
			t = terms.get(0);
			if (!t.isKey()) {
				throw SyntaxException.create(getSourceRange(),
						MSG_FIELD_MUST_BE_VALID_KEY, path);
			}
		} catch (EvaluationException ee) {
			throw SyntaxException.create(getSourceRange(),
					MSG_FIELD_MUST_BE_VALID_PATH, path);
		} catch (SyntaxException se) {
			throw se.addExceptionInfo(getSourceRange(), null);
		}

		return t;
	}

	public void setInclude(String include) {
		this.include = include;
	}

	public String getInclude() {
		return include;
	}

	@Override
	public String toString() {
		String s = "";
		if (include != null) {
			s = s + "include " + include;
		} else {
			s = s + path;
			if (required) {
				s = s + " : ";
			} else {
				s = s + " ? ";
			}
		}
		return s;
	}

}
