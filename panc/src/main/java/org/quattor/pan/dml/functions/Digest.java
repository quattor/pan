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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/First.java $
 $Id: First.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_FIRST_STRING_ARG_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_DIGEST_ALGORITHM;
import static org.quattor.pan.utils.MessageUtils.MSG_SECOND_STRING_ARG_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_TWO_ARGS_REQ;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;

/**
 * Creates a digest of a string using the specified algorithm.
 *
 * @author loomis
 *
 */
final public class Digest extends BuiltInFunction {

	private Digest(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("digest", sourceRange, operations);

	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Check that exactly two arguments have been provided.
		if (operations.length != 2) {
			throw SyntaxException.create(sourceRange, MSG_TWO_ARGS_REQ,
					"digest");
		}

		// If the algorithm is a static value, then check that it is a string
		// and is a valid algorithm name.
		if (operations[0] instanceof Element) {
			if (operations[0] instanceof StringProperty) {
				String algorithm = ((StringProperty) operations[0]).getValue();
				try {
					MessageDigest.getInstance(algorithm);
				} catch (NoSuchAlgorithmException e) {
					throw SyntaxException.create(sourceRange,
							MSG_INVALID_DIGEST_ALGORITHM, algorithm);
				}
			} else {
				throw SyntaxException.create(sourceRange,
						MSG_FIRST_STRING_ARG_REQ, "digest");
			}
		}

		// Check that the message is a string if it is a constant value.
		if (operations[1] instanceof Element) {
			if (!(operations[1] instanceof StringProperty)) {
				throw EvaluationException.create(sourceRange,
						MSG_SECOND_STRING_ARG_REQ, "digest");
			}
		}

		return new Digest(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		// Retrieve the values of the arguments.
		Element[] args = calculateArgs(context);
		assert (args.length == 2);

		// Check that both of the arguments are strings.
		String algorithm = "";
		String message = "";
		if (args[0] instanceof StringProperty) {
			algorithm = ((StringProperty) args[0]).getValue();
		} else {
			throw EvaluationException.create(sourceRange,
					MSG_FIRST_STRING_ARG_REQ, name);
		}
		if (args[1] instanceof StringProperty) {
			message = ((StringProperty) args[1]).getValue();
		} else {
			throw EvaluationException.create(sourceRange,
					MSG_SECOND_STRING_ARG_REQ, name);
		}

		String digest = "";
		try {
			MessageDigest m = MessageDigest.getInstance(algorithm);
			m.update(message.getBytes(Charset.forName("UTF-8")), 0, message.length());
			digest = new BigInteger(1, m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			throw EvaluationException.create(sourceRange,
					MSG_INVALID_DIGEST_ALGORITHM, algorithm);
		}

		return StringProperty.getInstance(digest);
	}

}
