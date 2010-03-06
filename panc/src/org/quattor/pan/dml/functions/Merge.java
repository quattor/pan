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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Merge.java $
 $Id: Merge.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_KEY_OR_INDEX;
import static org.quattor.pan.utils.MessageUtils.MSG_ONE_OR_MORE_ARG_REQ;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

/**
 * Combine the contents of two resources. If the resources are nlists, they may
 * not have any keys in common.
 * 
 * @author loomis
 * 
 */
final public class Merge extends BuiltInFunction {

	private Merge(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("merge", sourceRange, operations);
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Ensure that there is at least one argument.
		if (operations.length == 0) {
			throw SyntaxException.create(sourceRange, MSG_ONE_OR_MORE_ARG_REQ,
					"merge");
		}

		return new Merge(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		// Retrieve the values of the arguments.
		Element[] args = calculateArgs(context);
		assert (args.length > 0);

		// The result can either be a nlist or list as defined below.
		Element result = null;

		// List can either be all lists or all nlists.
		if (args[0] instanceof HashResource) {

			try {

				// Create the result.
				HashResource nlistResult = new HashResource();
				result = nlistResult;

				// Copy the first nlist as the basis.
				for (Element element : args) {
					HashResource nlist = (HashResource) element;
					for (Term term : nlist.keySet()) {
						try {
							if (nlistResult.put(term, nlist.get(term)) != null) {
								throw new EvaluationException(
										"two (or more) children have the same name in merge(): "
												+ term, getSourceRange(),
										context);
							}
						} catch (InvalidTermException ite) {
							// This exception should never be encountered
							// because come from an existing hash.
							throw CompilerError
									.create(MSG_INVALID_KEY_OR_INDEX);
						}
					}
				}

			} catch (ClassCastException cce) {
				throw new EvaluationException(
						"merge() arguments must be all lists or nlists",
						getSourceRange(), context);
			}

		} else if (args[0] instanceof ListResource) {

			try {

				// Create the result.
				ListResource listResult = new ListResource();
				result = listResult;

				// Copy the first list as the basis.
				int index = 0;
				for (Element element : args) {
					ListResource list = (ListResource) element;
					for (int i = 0; i < list.size(); i++) {
						Term term = TermFactory.create(i);
						try {
							listResult.put(index++, list.get(term));
						} catch (InvalidTermException ite) {
							// This exception should never be encountered
							// because the terms are created with the correct
							// type.
							throw CompilerError
									.create(MSG_INVALID_KEY_OR_INDEX);

						}
					}
				}

			} catch (ClassCastException cce) {
				throw new EvaluationException(
						"merge() arguments must be all lists or nlists",
						getSourceRange(), context);
			}

		} else {
			throw new EvaluationException(
					"merge() arguments must be all lists or nlists",
					getSourceRange(), context);
		}

		return result;
	}
}
