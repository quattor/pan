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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Hash.java $
 $Id: Hash.java 3107 2008-04-07 07:03:42Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_DUPLICATE_KEY;
import static org.quattor.pan.utils.MessageUtils.MSG_EVEN_NUMBER_OF_ARGS;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_KEY_HASH;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_KEY_TYPE;

import java.util.HashSet;
import java.util.Set;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

/**
 * Creates a dict from the function's arguments. There must be an even number
 * of arguments. This function is available as <code>dict</code> in the pan
 * language.
 * 
 * @author loomis
 * 
 */
final public class Hash extends BuiltInFunction {

	private Hash(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("dict", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Ensure that there is an even number of arguments. Since the parser
		// does little argument checking for function calls, this explicit check
		// is needed.
		if ((operations.length % 2) != 0) {
			throw SyntaxException.create(sourceRange, MSG_EVEN_NUMBER_OF_ARGS);
		}

		// Do a detailed check of the keys. They must be valid and there can be
		// no duplicates.
		try {
			Set<Term> keys = new HashSet<Term>();
			for (int i = 0; i < operations.length; i += 2) {
				if (operations[i] instanceof Element) {
					Term key = createKey((Element) operations[i]);
					if (keys.contains(key)) {
						throw EvaluationException
								.create(MSG_DUPLICATE_KEY, key);
					}
					keys.add(key);
				}
			}
		} catch (EvaluationException ee) {
			throw SyntaxException.create(sourceRange, ee);
		}

		return new Hash(sourceRange, operations);
	}

	private static HashResource createHashFromArgs(Element[] args) {

		assert ((args.length % 2) == 0);

		// Create a new hash for the result.
		HashResource result = new HashResource();

		// Fill up the hash.
		for (int i = 0; i < args.length; i += 2) {

			Term t = null;
			try {
				t = createKey(args[i]);

				Element value = args[i + 1];

				Element old = result.put(t, value);

				if (old != null) {
					throw EvaluationException.create(MSG_DUPLICATE_KEY, t);
				}

			} catch (InvalidTermException ite) {
				// This should actually not be reachable because the type of the
				// term is checked with the createKey() method.
				throw EvaluationException.create(MSG_INVALID_KEY_HASH, t);
			}
		}

		return result;
	}

	private static Term createKey(Element stringProperty) {

		Term result = null;

		try {

			StringProperty key = (StringProperty) stringProperty;
			result = TermFactory.create(key);

		} catch (ClassCastException cce) {
			throw EvaluationException.create(MSG_INVALID_KEY_TYPE,
					stringProperty.getTypeAsString());
		}

		if (!result.isKey()) {
			throw EvaluationException.create(MSG_INVALID_KEY_HASH,
					stringProperty);
		}

		return result;
	}

	@Override
	public Element execute(Context context) {

		// Retrieve the values of the arguments.
		Element[] args = calculateArgs(context);
		assert ((args.length % 2) == 0);

		// Create a new hash. Catch any evaluation errors and add appropriate
		// location information.
		try {
			return createHashFromArgs(args);
		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(sourceRange, context);
		}

	}

}
