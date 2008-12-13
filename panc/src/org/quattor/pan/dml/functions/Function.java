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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Function.java $
 $Id: Function.java 3596 2008-08-17 08:35:06Z loomis $
 */

package org.quattor.pan.dml.functions;

import java.util.logging.Level;

import org.quattor.pan.dml.AbstractOperation;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.ReturnValueException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.LocalVariableMap;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.FunctionDefinition;
import org.quattor.pan.utils.GlobalVariable;

/**
 * Implements a function call to a user defined function.
 * 
 * @author loomis
 * 
 */
final public class Function extends AbstractOperation {

	private static final long serialVersionUID = -2196877545584935474L;

	private String name = null;

	public Function(SourceRange sourceRange, String name,
			Operation... operations) {
		super(sourceRange, operations);

		assert (name != null);
		this.name = name;
	}

	@Override
	public Element execute(Context context) {

		// Lookup this function. Throw an error if the function is not found.
		FunctionDefinition fd = context.getFunction(name);
		if (fd == null) {
			throw new EvaluationException("undefined function: " + name,
					sourceRange, context);
		}

		// Create the args array for the function call.
		int nargs = ops.length;
		ListResource argv = new ListResource();
		for (int i = 0; i < nargs; i++) {
			argv.put(i, ops[i].execute(context));
		}

		// Save the old local variables. This will also create and install a new
		// hash with the ARGV and ARGC variables set.
		LocalVariableMap oldLocalVariables = context.createLocalVariableMap(argv);

		// Replace the FUNCTION global variable.
		GlobalVariable oldFunctionVariable = context.replaceGlobalVariable(
				"FUNCTION", StringProperty.getInstance(name), true);

		// Push the template that defined the function onto the stack. Most
		// calls/includes are logged at the INFO level. We log at the CONFIG
		// level to allow function calls to be excluded.
		String logMessage = "FUNCTION " + name;
		context
				.pushTemplate(fd.template, sourceRange, Level.CONFIG,
						logMessage);

		// Execute the function itself.
		Element result = null;
		try {
			result = fd.dml.execute(context);
		} catch (ReturnValueException rve) {
			result = rve.getElement();
		}

		// Put back the old FUNCTION global variable.
		context.setGlobalVariable("FUNCTION", oldFunctionVariable);

		// Pop the function definition template off of the stack. The CONFIG
		// level is used to allow function calls to be excluded, if desired.
		context.popTemplate(Level.CONFIG, logMessage);

		// Restore the old local variable definitions.
		context.restoreLocalVariableMap(oldLocalVariables);

		return result;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + name + "," + ops.length
				+ ")";
	}

}
