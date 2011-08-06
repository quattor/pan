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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Base64Decode.java $
 $Id: Base64Decode.java 3515 2008-07-31 13:20:05Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ARGS_BASE64_DECODE;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_NO_ARGS_BASE64_DECODE;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.Base64;
import org.quattor.pan.utils.MessageUtils;

/**
 * Decodes a Base64-encoded string.
 * 
 * @author loomis
 * 
 */
final public class Base64Decode extends BuiltInFunction {

	private Base64Decode(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("base64_decode", sourceRange, operations);

		// Ensure that there is exactly one argument. Since the parser does
		// little argument checking for function calls, this explicit check is
		// needed.
		if (operations.length != 1) {
			throw SyntaxException.create(sourceRange,
					MSG_INVALID_NO_ARGS_BASE64_DECODE);
		}
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {
		return new Base64Decode(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 1);

		Element result = ops[0].execute(context);
		try {
			String s = ((StringProperty) result).getValue();
			return StringProperty.getInstance(new String(Base64.decode(s)));
		} catch (ClassCastException cce) {
			throw new EvaluationException(MessageUtils
					.format(MSG_INVALID_ARGS_BASE64_DECODE), sourceRange,
					context);
		}
	}

}
