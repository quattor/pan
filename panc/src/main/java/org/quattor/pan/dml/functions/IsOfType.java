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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/IsDefined.java $
 $Id: IsDefined.java 2862 2008-02-07 13:22:08Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_ONE_ARG_REQ;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.operators.Variable;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.SourceRange;

/**
 * This is the parent class for those classes that implement the type checking
 * functions of pan (e.g. exists(), is_string(), is_boolean(), etc.). This
 * abstract class will choose a subclass based on the given arguments.
 * 
 * @author loomis
 * 
 */
abstract public class IsOfType extends BuiltInFunction {

	protected Class<? extends Element> type;

	protected IsOfType(String name, SourceRange sourceRange,
			Class<? extends Element> type, Operation... operations)
			throws SyntaxException {
		super(name, sourceRange, operations);
		this.type = type;
	}

	public static Operation getInstance(SourceRange sourceRange,
			Class<? extends Element> type, String name, Operation... operations)
			throws SyntaxException {

		// Ensure that there is exactly one argument. Since the parser does
		// little argument checking for function calls, this explicit check is
		// needed.
		if (operations.length != 1) {
			throw SyntaxException.create(sourceRange, MSG_ONE_ARG_REQ, name);
		}

		Operation op = null;
		if (operations[0] instanceof Variable) {
			op = IsVariableOfType.getInstance(sourceRange, type, name,
					operations);
		} else {
			op = IsValueOfType.getInstance(sourceRange, type, name, operations);
		}
		return op;
	}

}
