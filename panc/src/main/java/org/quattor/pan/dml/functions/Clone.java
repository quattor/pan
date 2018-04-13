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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Clone.java $
 $Id: Clone.java 3515 2008-07-31 13:20:05Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_NO_ARGS_CLONE;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;

/**
 * This function will clone the given element. In practice this will really only
 * clone Resources because Properties are immutable and can be shared. Cloning
 * of elements is usually done automatically by the compiler; users should
 * rarely, if ever, need to call this explicitly.
 * 
 * @author loomis
 * 
 */
final public class Clone extends BuiltInFunction {

	private Clone(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("clone", sourceRange, operations);

		// Check the number of arguments and the types.
		if (operations.length != 1) {
			throw SyntaxException
					.create(sourceRange, MSG_INVALID_NO_ARGS_CLONE);
		}
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {
		return new Clone(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {
		Element result = ops[0].execute(context);
		return result.duplicate();
	}

}
