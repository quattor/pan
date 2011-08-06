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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Key.java $
 $Id: Key.java 3107 2008-04-07 07:03:42Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_TWO_ARGS_REQ;

import java.util.Arrays;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Returns the key associated with the entry given by the index. Note that
 * nlists are ordered lexically by their keys.
 * 
 * @author loomis
 * 
 */
final public class Key extends BuiltInFunction {

	private Key(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("key", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Ensure that there is exactly two arguments.
		if (operations.length != 2) {
			throw SyntaxException.create(sourceRange, MSG_TWO_ARGS_REQ, "key");
		}

		return new Key(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		// Retrieve the values of the arguments.
		Element[] args = calculateArgs(context);
		assert (args.length == 2);

		// Ensure that the arguments are of the correct type.
		HashResource nlist = null;
		int index = 0;
		try {
			nlist = (HashResource) args[0];
		} catch (ClassCastException cce) {
			throw new EvaluationException(
					"first argument to key() must be a hash/nlist",
					getSourceRange(), context);
		}
		try {
			long tindex = ((LongProperty) args[1]).getValue().longValue();
			if (tindex > ((long) Integer.MAX_VALUE)) {
				throw new EvaluationException("index exceeds maximum value ("
						+ Integer.MAX_VALUE + "): " + tindex, getSourceRange(),
						context);
			}
			if ((tindex < 0) || (tindex > (nlist.size() - 1))) {
				throw new EvaluationException("index out of bounds in key(): "
						+ tindex, getSourceRange(), context);
			}
			index = (int) tindex;
		} catch (ClassCastException cce) {
			throw new EvaluationException(
					"second argument to key() must be a long",
					getSourceRange(), context);
		}

		StringProperty[] keys = (StringProperty[]) nlist.keySet().toArray(
				new StringProperty[nlist.size()]);
		Arrays.sort(keys);

		return keys[index];
	}
}
