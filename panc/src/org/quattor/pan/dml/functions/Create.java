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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Create.java $
 $Id: Create.java 3927 2008-11-20 16:47:35Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_FIRST_ARG_CREATE;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_KEY_CREATE;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_KEY_CREATE_STRINGS;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_NO_ARGS_CREATE;

import java.util.logging.Level;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.LocalVariableMap;
import org.quattor.pan.template.SelfHolder;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.template.Template;
import org.quattor.pan.template.Template.TemplateType;
import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.TermFactory;

/**
 * Creates an nlist from the named structure template. Additional pairs of
 * arguments may also be given that will override any entries with the same keys
 * from the structure template.
 * 
 * @author loomis
 * 
 */
final public class Create extends BuiltInFunction {

	private static final long serialVersionUID = 1700204818503882062L;

	private Create(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("create", sourceRange, operations);

		// Ensure that there is an odd number of arguments. There must first be
		// the name of the template followed by optional key-value pairs.
		if ((operations.length & 1) != 1) {
			throw SyntaxException.create(sourceRange,
					MSG_INVALID_NO_ARGS_CREATE);
		}
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {
		return new Create(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		throwExceptionIfCompileTimeContext(context);

		// Retrieve the values of the arguments.
		Element[] args = calculateArgs(context);
		assert ((args.length & 1) == 1);

		// Initialize the relative root.
		HashResource previousRelativeRoot = context.createRelativeRoot();

		// Save the old local variables.
		LocalVariableMap oldLocalVariables = context
				.createLocalVariableMap(null);

		// Include the named template.
		try {
			executeWithNamedStructureTemplate(context,
					((StringProperty) args[0]).getValue());
		} catch (ClassCastException cce) {
			throw new EvaluationException(MessageUtils
					.format(MSG_INVALID_FIRST_ARG_CREATE), getSourceRange(),
					context);
		}

		// Restore the old local variable definitions.
		context.restoreLocalVariableMap(oldLocalVariables);

		// Retrieve the relative root and clear the value.
		HashResource root = context.restoreRelativeRoot(previousRelativeRoot);

		// Add or modify any key/value pairs given explicitly in the function
		// arguments.
		for (int i = 1; i < args.length; i += 2) {

			StringProperty key = null;
			try {
				key = (StringProperty) args[i];
				Element value = args[i + 1];

				root.put(TermFactory.create(key), value);

			} catch (ClassCastException cce) {
				throw new EvaluationException(MessageUtils.format(
						MSG_INVALID_KEY_CREATE_STRINGS, i, args[i]
								.getTypeAsString()), sourceRange);
			} catch (InvalidTermException ite) {
				throw new EvaluationException(MessageUtils.format(
						MSG_INVALID_KEY_CREATE, key));
			}
		}

		return root;
	}

	protected void executeWithNamedStructureTemplate(Context context,
			String name) throws EvaluationException {

		assert (context != null);
		assert (name != null);

		Template template = null;
		boolean runStatic = false;

		try {
			template = context.localLoad(name);
			if (template == null) {
				template = context.globalLoad(name);
				runStatic = true;
			}
		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(getSourceRange(), context);
		}

		// Check that the template was actually loaded.
		if (template == null) {
			throw new EvaluationException("failed to load template: " + name,
					getSourceRange());
		}

		// The included type must be a structure template.
		TemplateType includedType = template.type;
		if (includedType != TemplateType.STRUCTURE) {
			throw new EvaluationException(
					"first argument of create() must be the name of structure template",
					getSourceRange());
		}

		// Push the template, execute it, then pop it from the stack.
		// Template loops will be caught by the call depth check when pushing
		// the template. Note that the actual state of the SELF must be saved
		// (restored) when entering (exiting) a structure template.
		context.pushTemplate(template, getSourceRange(), Level.INFO,
				"STRUCTURE");
		SelfHolder self = context.saveSelf();
		template.execute(runStatic, context);
		context.restoreSelf(self);
		context.popTemplate(Level.INFO, "STRUCTURE");

	}

}
