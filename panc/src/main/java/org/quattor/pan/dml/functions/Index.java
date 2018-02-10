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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Index.java $
 $Id: Index.java 3597 2008-08-17 09:08:57Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_2_OR_3_ARGS;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;

/**
 * Determines the index for a substring within a string or a value within a
 * Resource.
 * 
 * @author loomis
 * 
 */
final public class Index extends BuiltInFunction {

	private Index(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("index", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// There can be two or three arguments.
		if (operations.length < 2 || operations.length > 3) {
			throw SyntaxException.create(sourceRange, MSG_2_OR_3_ARGS, "index");
		}

		return new Index(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 2 || ops.length == 3);

		Element[] args = calculateArgs(context);

		// In all cases, the third argument (if it exists) must be a long. Check
		// this first.
		int fromIndex = 0;
		if (args.length == 3) {
			try {
				fromIndex = ((LongProperty) args[2]).getValue().intValue();
				if (fromIndex < 0) {
					fromIndex = 0;
				}
			} catch (ClassCastException cce) {
				throw new EvaluationException(
						"third argument of index() must be a long",
						getSourceRange(), context);
			}
		}

		if (args[0] instanceof StringProperty
				&& args[1] instanceof StringProperty) {

			// Search for the substring within a string.
			String str = ((StringProperty) args[0]).getValue();
			String target = ((StringProperty) args[1]).getValue();

			return LongProperty.getInstance((long) target.indexOf(str,
					fromIndex));

		} else if (args[1] instanceof ListResource) {

			// Look for a given element within the Resource.
			Resource resource = (Resource) args[1];
			int count = 0;
			for (Resource.Entry entry : resource) {
				if (count >= fromIndex) {
					Property key = entry.getKey();
					Element value = entry.getValue();
					if (args[0].equals(value)) {
						return key;
					}
				}
				count++;
			}

			return LongProperty.getInstance(-1L);

		} else if (args[1] instanceof HashResource) {

			// Look for a given element within the Resource.
			Resource resource = (Resource) args[1];
			int count = 0;
			for (Resource.Entry entry : resource) {
				Property key = entry.getKey();
				Element value = entry.getValue();
				if (args[0].equals(value)) {
					if (count < fromIndex) {
						count++;
					} else {
						return key;
					}
				}
			}

			return StringProperty.getInstance("");

		} else {

			// Something is wrong with the arguments.
			throw new EvaluationException(
					"invalid argument(s) in call to index()", getSourceRange(),
					context);
		}

	}
}
