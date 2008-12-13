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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/IncludeStatement.java $
 $Id: IncludeStatement.java 3600 2008-08-17 14:48:32Z loomis $
 */

package org.quattor.pan.statement;

import static org.quattor.pan.utils.MessageUtils.MSG_DML_MUST_BE_STRING_NULL_OR_UNDEF;

import java.util.logging.Level;
import java.util.regex.Pattern;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.CompileTimeContext;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.template.Template;
import org.quattor.pan.template.Template.TemplateType;

/**
 * Executes another referenced template whose name is either a constant or
 * computed from a DML expression. This superclass is used to collect the common
 * functionality of the static and computed include statements.
 * 
 * @author loomis
 * 
 */
abstract public class IncludeStatement extends Statement {

	private static final long serialVersionUID = -3506119667158615650L;

	/**
	 * A Pattern which defines valid, namespaced template names. The Pattern is
	 * valid for both object and non-object template names.
	 */
	private static final Pattern identifierPattern = Pattern
			.compile("^([\\w][\\w+.-]*)(/[\\w+.-]+)*$");

	/**
	 * Constructor which must be called by subclasses. The IncludeStatement
	 * itself is abstract, so a direct instance of this class cannot be created.
	 * 
	 * @param sourceRange
	 *            source location of this statement
	 */
	protected IncludeStatement(SourceRange sourceRange) {
		super(sourceRange);
	}

	/**
	 * Determine if the given name is a valid template name.
	 * 
	 * @param name
	 *            template name to verify (name may not be null)
	 * 
	 * @return flag indicating if the name is valid
	 */
	static boolean validIdentifier(String name) {
		assert (name != null);
		return identifierPattern.matcher(name).matches();
	}

	static public IncludeStatement newIncludeStatement(SourceRange sourceRange,
			Operation dml) throws SyntaxException {

		Element element = runDefaultDml(dml);

		if (element == null) {
			// Usual case of a real computed include statement. This includes
			// any DML expression that ends with an error.
			return new ComputedIncludeStatement(sourceRange, dml);
		} else if (element != null && element instanceof StringProperty) {
			// Compile-time constant string.
			return new StaticIncludeStatement(sourceRange,
					((StringProperty) element).getValue());
		} else if (element instanceof Null || element instanceof Undef) {
			// Compile-time undef or null. Convert to a no-op.
			return null;
		} else {
			// Compile-time constant that isn't a string, null, or undef. Throw
			// an exception.
			throw SyntaxException.create(sourceRange,
					MSG_DML_MUST_BE_STRING_NULL_OR_UNDEF);
		}
	}

	/**
	 * This is a utility method which performs an include from a fixed template
	 * name. The validity of the name should be checked by the subclass; this
	 * avoids unnecessary regular expression matching.
	 * 
	 * @param context
	 *            evaluation context to use
	 * @param name
	 *            fixed template name
	 * 
	 * @throws EvaluationException
	 */
	protected void executeWithNamedTemplate(Context context, String name)
			throws EvaluationException {

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

		TemplateType includeeType = context.getCurrentTemplate().type;
		TemplateType includedType = template.type;

		// Check that the template type are correct for the inclusion.
		if (!Template.checkValidInclude(includeeType, includedType)) {
			throw new EvaluationException(includeeType
					+ " template cannot include " + includedType + " template",
					getSourceRange());
		}

		// Push the template, execute it, then pop it from the stack.
		// Template loops will be caught by the call depth check when pushing
		// the template.
		context.pushTemplate(template, getSourceRange(), Level.INFO,
				includedType.toString());
		template.execute(runStatic, context);
		context.popTemplate(Level.INFO, includedType.toString());

	}

	// This is essentially a copy of a similar function in the pan parser
	// utilities. Probably should find a better place for this, so that the code
	// can be reused.
	static private Element runDefaultDml(Operation dml) throws SyntaxException {

		// If the argument is null, return null as the value immediately.
		if (dml == null) {
			return null;
		}

		// Create a nearly empty execution context. There are no global
		// variables by default (including no 'self' variable). Only the
		// standard built-in functions are accessible.
		Context context = new CompileTimeContext();

		Element value = null;

		// Execute the DML block. The block must evaluate to an Element. Any
		// error is fatal for the compilation.
		try {
			value = context.executeDmlBlock(dml);
		} catch (EvaluationException consumed) {
			value = null;
		}

		return value;
	}

}
