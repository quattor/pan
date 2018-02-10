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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Debug.java $
 $Id: Debug.java 3606 2008-08-19 15:52:17Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_ONE_STRING_ARG_REQ;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;

/**
 * This is a suppressed debug() function call. This will be called when a
 * debug() function call is encountered, but the given debug patter does not
 * match the name of the containing template. This simply replaces the call with
 * an undef value.
 * 
 * @author loomis
 * 
 */
final public class DebugSuppressed extends BuiltInFunction {

	private DebugSuppressed(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("debug", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Even though the call will be deleted, do error checking on the
		// argument.
		if (operations.length != 1) {
			throw SyntaxException.create(sourceRange, MSG_ONE_STRING_ARG_REQ,
					"debug");
		}
		if (operations[0] instanceof Element) {
			if (!(operations[0] instanceof StringProperty)) {
				throw SyntaxException.create(sourceRange,
						MSG_ONE_STRING_ARG_REQ, "debug");
			}
		}

		return Undef.VALUE;
	}

	@Override
	public Element execute(Context context) {
		// This can never be called because the operation is always replaced
		// with an undef value.
		assert (false);
		return null;
	}

}
