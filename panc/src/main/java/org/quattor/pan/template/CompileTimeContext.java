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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/template/Context.java $
 $Id: Context.java 3598 2008-08-17 09:19:06Z loomis $
 */

package org.quattor.pan.template;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_VALIDATION_FUNCTION_RETURN_TYPE;
import static org.quattor.pan.utils.MessageUtils.MSG_VALIDATION_FAILED_BECAUSE_OF_EXCEPTION;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quattor.pan.CompilerLogging.LoggingType;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.ReturnValueException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.repository.SourceFile;
import org.quattor.pan.template.Template.TemplateType;
import org.quattor.pan.type.FullType;
import org.quattor.pan.utils.FunctionDefinition;
import org.quattor.pan.utils.GlobalVariable;
import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.Path;
import org.quattor.pan.utils.SourceLocation;
import org.quattor.pan.utils.Term;

/**
 * Contains the global context for the evaluation of a machine profile. Separate
 * hashes are kept for functions, types, and variables allowing them to have
 * identical names. The root element allows properties or resources to be found
 * in the tree of configuration information.
 * 
 * @author loomis
 * 
 */
public class CompileTimeContext implements Context {

	private static final String TPL_VAR = "TEMPLATE";

	// Logger to trace template inclusion.
	private static final Logger callLogger = LoggingType.CALL.logger();

	private LocalVariableMap localVariables;

	private final Stack<SourceLocation> templates;

	private Template currentTemplate;

	private final Template objectTemplate;

	private IteratorMap iteratorMap;

	public final int deprecationLevel;

	private static final Template emptyTemplate;

	static {
		try {
			emptyTemplate = new Template("empty");
		} catch (SyntaxException se) {
			throw CompilerError
					.create(MessageUtils.MSG_CANNOT_CREATE_EMPTY_TEMPLATE);
		}
	}

	/**
	 * Constructs a new context for the given root element. The function, type,
	 * and variable hashes are initially empty. (In particular, the object
	 * variable is not automatically defined.
	 */
	public CompileTimeContext() {

		// Copy in a couple of options used for function evaluation. The
		// compiler will be null for compile-time DML evaluations.
		this.deprecationLevel = -1;

		// Create the empty hashes.
		templates = new Stack<SourceLocation>();

		localVariables = new LocalVariableMap();
		iteratorMap = new IteratorMap();

		// Set the object template and add it as a dependency.
		assert (emptyTemplate != null);
		assert (emptyTemplate.type == TemplateType.OBJECT);
		this.objectTemplate = emptyTemplate;

	}

	/**
	 * Returns the object Template which is the basis of this ObjectContext.
	 */
	public Template getObjectTemplate() {
		return objectTemplate;
	}

	/**
	 * Returns the root element for this context.
	 */
	public HashResource getRoot() {
		// This should never be called.
		return null;
	}

	/**
	 * Retrieve and clear the relative root for this context.
	 * 
	 * @param previousValue
	 *            previous value of the relative root to restore
	 * 
	 * @return value of the relative root which was replaced
	 */
	public HashResource restoreRelativeRoot(HashResource previousValue) {
		// This should never be called.
		return null;
	}

	/**
	 * Initializes the relative root for this context and returns the old
	 * relative root. The old value should be restored when the processing of
	 * the structure template is finished.
	 * 
	 * @return old value of the relative root
	 */
	public HashResource createRelativeRoot() {
		// This should never be called.
		return null;
	}

	/**
	 * Returns an unmodifiable copy of the dependencies.
	 */
	public Set<SourceFile> getDependencies() {
		// This should never be called.
		return null;
	}

	/**
	 * Returns an unmodifiable copy of the object dependencies.
	 */
	public Set<String> getObjectDependencies() {
		// This should never be called!
		return null;
	}

	/**
	 * Turn off the object dependency checking. This should only be turned off
	 * after the build phase is complete.
	 */
	public void turnOffObjectDependencyChecking() {
		// This should never be called.
	}

	/**
	 * A method to load a template from the local cache. Returns null if the
	 * template is not in the cache.
	 */
	public Template localLoad(String name) {
		// This should never be called.
		return null;
	}

	public Template localAndGlobalLoad(String name, boolean lookupOnly) {
		// This should never be called.
		return null;
	}

	/**
	 * A method to load a template from the global cache. This may trigger the
	 * global cache to compile the template.
	 */
	public Template globalLoad(String name) {
		return globalLoad(name, false);
	}

	/**
	 * A method to load a template from the global cache. This may trigger the
	 * global cache to compile the template.
	 */
	public Template globalLoad(String name, boolean lookupOnly) {
		// This should never be called.
		return null;
	}

	public SourceFile lookupFile(String name) {
		// This should never be called.
		return null;
	}

	public LocalVariableMap createLocalVariableMap(ListResource argv) {
		LocalVariableMap oldMap = localVariables;
		localVariables = new LocalVariableMap(argv);
		return oldMap;
	}

	public void restoreLocalVariableMap(LocalVariableMap localVariableHolder) {
		this.localVariables = localVariableHolder;
	}

	public IteratorMap createIteratorMap() {
		IteratorMap oldIteratorMap = iteratorMap;
		iteratorMap = new IteratorMap();
		return oldIteratorMap;
	}

	public void restoreIteratorMap(IteratorMap iteratorMap) {
		this.iteratorMap = iteratorMap;
	}

	/**
	 * Set the name of the object template. Define the necessary variables.
	 */
	public void setObjectAndLoadpath() {

		StringProperty sname = StringProperty.getInstance(objectTemplate.name);

		setGlobalVariable("OBJECT", sname, true);
		setGlobalVariable("LOADPATH", new ListResource(), false);
	}

	/**
	 * Return the function definition associated with the given name or null if
	 * it doesn't exist. It will always return null if the argument is null.
	 * 
	 * @param name
	 *            name of the function to retrieve
	 * 
	 * @return FunctionDefinition associated with the given name or null if it
	 *         doesn't exist
	 */
	public FunctionDefinition getFunction(String name) {
		// Should never be called.
		return null;
	}

	/**
	 * Define the given DML block as a function with the given name in this
	 * context. Note that functions may not be redefined; attempting to do so
	 * will throw an EvaluationException.
	 * 
	 * @param name
	 *            name for the function
	 * @param function
	 *            code for the function as a DML block
	 * @param template
	 *            template in which this function is defined (used for error
	 *            handling)
	 * @param sourceRange
	 *            location in the template where this function is defined (used
	 *            for error handling)
	 * 
	 * @throws EvaluationException
	 *             if a function with the given name already exists
	 */
	public void setFunction(String name, Operation function, Template template,
			SourceRange sourceRange) throws EvaluationException {
		// Should never be called.
	}

	/**
	 * This method returns an unmodifiable, ordered map of the type bindings.
	 * 
	 * @return unmodifiable, ordered map of the type bindings
	 */
	public Map<Path, List<FullType>> getBindings() {
		// This should never be called.
		return null;
	}

	/**
	 * This method associates a type definition to a path. These bindings are
	 * checked as part of the validation process. Note that there can be more
	 * than one binding per path.
	 * 
	 * @param path
	 *            absolute path to bind to the type
	 * @param fullType
	 *            data type to use
	 * @param template
	 *            template where the binding was defined (used for error
	 *            handling)
	 * @param sourceRange
	 *            location in the template where the binding was defined (used
	 *            for error handling)
	 */
	public void setBinding(Path path, FullType fullType, Template template,
			SourceRange sourceRange) {

		// This should never be called.
	}

	/**
	 * Return the type associated with the given name or null if it doesn't
	 * exist. It will always return null if the argument is null.
	 * 
	 * @param name
	 *            name of the type to retrieve
	 * 
	 * @return FullType associated with this name or null if it doesn't exist
	 */
	public FullType getFullType(String name) {
		// This should never be called.
		return null;
	}

	/**
	 * Associate the given type with the given name within this ObjectContext.
	 * This will throw an EvaluationException if the type is already defined.
	 * 
	 * @param name
	 *            name to associate to the type
	 * @param fullType
	 *            data type to use for the definition
	 * @param template
	 *            template where the type is defined (used for error handling)
	 * @param sourceRange
	 *            location in the template where the type is defined (used for
	 *            error handling)
	 * 
	 * @throws EvaluationException
	 *             if there is already a type associated with the given name
	 */
	public void setFullType(String name, FullType fullType, Template template,
			SourceRange sourceRange) throws EvaluationException {

		// This should never be called.
	}

	/**
	 * Set the variable to the given value, preserving the status of the final
	 * flag. This will unconditionally set the value without checking if the
	 * value is final; be careful. The value must already exist.
	 */
	public void setGlobalVariable(String name, Element value) {
		// This should never be called.
	}

	/**
	 * Set the variable to the given GlobalVariable. If variable is null, then
	 * the global variable definition is removed.
	 * 
	 * @param name
	 *            global variable name
	 * @param variable
	 *            GlobalVariable to associate with name
	 */
	public void setGlobalVariable(String name, GlobalVariable variable) {
		// This should never be called.
	}

	/**
	 * Replaces the given global variable with the given value. The flag
	 * indicates whether or not the variable should be marked as final. Note,
	 * however, that this routine does not respect the final flag and replaces
	 * the value unconditionally. The function returns the old value of the
	 * variable or null if it didn't exist.
	 */
	public GlobalVariable replaceGlobalVariable(String name, Element value,
			boolean finalFlag) {
		// This should never be called.
		return null;
	}

	/**
	 * Set the variable to the given value. If the value is null, then the
	 * variable will be removed from the context.
	 * 
	 */
	public void setGlobalVariable(String name, Element value, boolean finalFlag) {
		// This should never be called.
	}

	/**
	 * Register a Resource iterator in the context.
	 */
	public void setIterator(Resource resource, Resource.Iterator iterator) {
		iteratorMap.put(resource, iterator);
	}

	/**
	 * Get the iterator for the given resource or null if the iterator has not
	 * been registered.
	 */
	public Resource.Iterator getIterator(Resource resource) {
		return iteratorMap.get(resource);
	}

	/**
	 * Unconditionally remove a global variable. This should never be called
	 * from user code. It is used to handle special variables like "self".
	 */
	public void removeGlobalVariable(String name) {
		// This should never be called.
	}

	/**
	 * Return the Element which corresponds to the given variable name without
	 * duplicating the value. This is useful when dealing with SELF or with
	 * variables in a context where it is known that the value won't be
	 * modified.
	 */
	public Element getGlobalVariable(String name) {
		// This should never be called.
		return null;
	}

	public GlobalVariable retrieveGlobalVariable(String name) {
		// This should never be called.
		return null;
	}

	public void pushTemplate(Template template, SourceRange sourceRange,
			Level logLevel, String logMessage) {

		SourceLocation location = new SourceLocation(currentTemplate,
				sourceRange);
		templates.push(location);
		currentTemplate = template;

		// Log what template we're entering.
		callLogger.log(logLevel, "ENTER", new Object[] { logMessage,
				currentTemplate.name });

		// Check to see that the call limit has not been exceeded.
		if (templates.size() > this.getCallLimit()) {
			popTemplate(Level.INFO, logMessage);
			throw new EvaluationException("call depth limit (" + getCallLimit()
					+ ") exceeded", sourceRange, this);
		}

	}

	public void popTemplate(Level logLevel, String logMessage) {

		// Log what template we're leaving.
		callLogger.log(logLevel, "EXIT", new Object[] { logMessage,
				currentTemplate.name });

		SourceLocation location = templates.pop();
		currentTemplate = location.template;
	}

	public void printTraceback(SourceRange sourceRange) {
		System.err.println(getTraceback(sourceRange));
	}

	public String getTraceback(SourceRange sourceRange) {

		SourceLocation[] locations = templates
				.toArray(new SourceLocation[templates.size()]);

		StringBuilder sb = new StringBuilder();
		sb.append(">>> call stack trace \n");
		sb.append(">>> ");
		sb
				.append((new SourceLocation(currentTemplate, sourceRange))
						.toString());
		sb.append("\n");
		for (int i = locations.length - 1; i >= 0; i--) {
			sb.append(">>> ");
			sb.append(locations[i].toString());
			sb.append("\n");
		}
		sb.append(">>> ====================\n\n");

		return sb.toString();
	}

	public void setCurrentTemplate(Template template) {
		currentTemplate = template;
	}

	public Template getCurrentTemplate() {
		return currentTemplate;
	}

	/**
	 * Pull the value of an element from a configuration tree. This can either
	 * be an absolute or external path. An EvaluationException will be thrown if
	 * the path cannot be found. This is equivalent to getElement(path, true).
	 * 
	 * @param path
	 *            absolute or external path to lookup
	 * 
	 * @return Element associated to the given path
	 * 
	 * @throws EvaluationException
	 *             if the path cannot be found or the path is relative
	 */
	public Element getElement(Path path) throws EvaluationException,
			ValidationException {
		return getElement(path, true);
	}

	/**
	 * Pull the value of an element from a configuration tree. This can either
	 * be an absolute, relative, or external path.
	 * 
	 * @param path
	 *            path to lookup
	 * @param errorIfNotFound
	 *            if true an EvaluationException will be thrown if the path
	 *            can't be found
	 * 
	 * @return Element associated to the given path
	 * 
	 * @throws EvaluationException
	 *             if the path can't be found and errorIfNotFound is set, or if
	 *             the path is relative and relativeRoot isn't set
	 */
	public Element getElement(Path path, boolean errorIfNotFound)
			throws EvaluationException {

		// This should never be called.
		return null;
	}

	public void putElement(Path path, Element value) {

		// This should never be called.
	}

	public Element executeDmlBlock(Operation dml) {
		Element result = null;

		// Store the old local variables and iterators. Needed because structure
		// templates need to keep the state across template.execute() calls.
		LocalVariableMap oldVariables = createLocalVariableMap(null);
		IteratorMap oldIterators = createIteratorMap();

		// If the TEMPLATE variable is already set, then don't set the value to
		// the current template. This can happen when executing structure
		// templates and we want to keep the outermost value.
		boolean setTemplate = (getGlobalVariable(TPL_VAR) == null);

		// Set the value of TEMPLATE to the current template. Be careful,
		// compile time execution doesn't set the current template. In this
		// case, don't set the global variable.
		if (setTemplate) {
			Template current = getCurrentTemplate();
			if (current != null) {
				StringProperty tname = StringProperty.getInstance(current.name);
				setGlobalVariable(TPL_VAR, tname, true);
			} else {
				setTemplate = false;
			}
		}

		// Run the DML block. Making sure to always restore the previous
		// variables and iterators.
		try {
			result = dml.execute(this);
		} catch (ReturnValueException rve) {
			result = rve.getElement();
		} finally {

			// Remove TEMPLATE variable if we set it above.
			if (setTemplate) {
				removeGlobalVariable(TPL_VAR);
			}

			// Restore the saved local variables and iterators.
			restoreLocalVariableMap(oldVariables);
			restoreIteratorMap(oldIterators);
		}

		return result;
	}

	public boolean executeDmlValidationBlock(Operation dml, Element self)
			throws ValidationException {

		Element result = null;

		// Store the old local variables and iterators. Needed because structure
		// templates need to keep the state across template.execute() calls.
		LocalVariableMap oldVariables = createLocalVariableMap(null);
		IteratorMap oldIterators = createIteratorMap();

		// Initialize the SELF reference to the local self value.
		SelfHolder selfHolder = new ReadOnlySelfHolder(self);
		initializeSelfHolder(selfHolder);

		try {

			result = dml.execute(this);

		} catch (ReturnValueException rve) {

			result = rve.getElement();

		} catch (EvaluationException ee) {

			File objectFile = (objectTemplate != null) ? objectTemplate.source
					: null;
			ValidationException ve = ValidationException
					.create(MSG_VALIDATION_FAILED_BECAUSE_OF_EXCEPTION);
			ve.setObjectTemplate(objectFile);
			ve.initCause(ee);
			throw ve;

		} finally {
			clearSelf();

			restoreLocalVariableMap(oldVariables);
			restoreIteratorMap(oldIterators);
		}

		try {
			BooleanProperty bresult = (BooleanProperty) result;
			return bresult.getValue().booleanValue();
		} catch (ClassCastException cce) {
			File objectFile = (objectTemplate != null) ? objectTemplate.source
					: null;
			ValidationException ve = ValidationException.create(
					MSG_INVALID_VALIDATION_FUNCTION_RETURN_TYPE, result
							.getTypeAsString());
			throw ve.setObjectTemplate(objectFile);
		}
	}

	/**
	 * Return the value associated with a local variable. This method will NOT
	 * attempt to look up a global variable if a local variable of the same name
	 * is not found.
	 * 
	 * @param name
	 *            name of the variable to lookup
	 * 
	 * @return the value of the associated local variable or null if the local
	 *         variable does not exist
	 */
	public Element getLocalVariable(String name) {
		return localVariables.get(name);
	}

	/**
	 * Return the Element which corresponds to the given variable name. It will
	 * first check local variables and then global variables. This method will
	 * return null if the variable doesn't exist or the argument is null. Note
	 * that this will clone the value before returning it, if it came from a
	 * global variable.
	 * 
	 * @param name
	 *            name of the variable to lookup
	 * @return Element corresponding to the given variable name or null if it
	 *         could not be found
	 */
	public Element getVariable(String name) {

		Element result = localVariables.get(name);

		// If the result is null, then try to look up a global variable.
		if (result == null) {
			result = getGlobalVariable(name);
		}

		return result;
	}

	/**
	 * Return the Element which corresponds to the given variable name. It will
	 * first check local variables and then the parent context. This method will
	 * return null if the variable doesn't exist or the argument is null. Note
	 * that this will clone the final value, if it originated from a global
	 * variable.
	 * 
	 * @param name
	 *            name of the variable to lookup
	 * @param lookupOnly
	 *            flag indicating if only a lookup should be done
	 * @param terms
	 *            values for dereferencing the given variable
	 * 
	 * @return Element value of the associated dereferenced variable
	 */
	public Element dereferenceVariable(String name, boolean lookupOnly,
			Term[] terms) throws InvalidTermException {

		boolean duplicate = false;
		Element result = localVariables.get(name);

		// If the result is null, then try to look up a global variable.
		if (result == null) {
			duplicate = true;
			result = getGlobalVariable(name);
		}

		// Now actually dereference the given variable. The caller must deal
		// with any invalid term exceptions or evaluation exceptions. We just
		// pass those on.
		if (result != null) {
			if (!(result instanceof Undef)) {
				// FIXME: Determine if the result needs to be protected.
				result = result.rget(terms, 0, false, lookupOnly);
			} else {
				// Trying to dereference an undefined value. Therefore, the
				// value does not exist; return null to caller.
				result = null;
			}
		}

		// FIXME: This is inefficient and should be avoided. However, one must
		// ensure that global variables are protected against changes.

		// To ensure that global variables are not inadvertently modified via
		// copies in local variables, duplicate the result. Do this only AFTER
		// the dereference to limit the amount of potentially unnecessary
		// cloning.
		if (duplicate && result != null) {
			result = result.duplicate();
		}

		return result;
	}

	/**
	 * Set the local variable to the given value. If the value is null, then the
	 * corresponding variable will be removed. If there is a global variable of
	 * the same name, then an EvaluationException will be thrown. This method
	 * does not allow children of the referenced value to be set.
	 * 
	 * @param name
	 *            name of the local variable
	 * @param value
	 *            value to use or null to remove the variable
	 * @throws EvaluationException
	 *             if there is a global variable with the same name as the local
	 *             variable
	 */
	public void setLocalVariable(String name, Element value)
			throws EvaluationException {

		assert (name != null);

		localVariables.put(name, value);
	}

	/**
	 * Set the local variable to the given value. If the value is null, then the
	 * corresponding variable will be removed. If there is a global variable of
	 * the same name, then an EvaluationException will be thrown.
	 * 
	 * @param name
	 *            name of the local variable
	 * @param terms
	 *            terms used to dereference the variable, or null if the
	 *            variable is to be used directly
	 * @param value
	 *            value to use or null to remove the variable
	 * @throws EvaluationException
	 *             if there is a global variable with the same name as the local
	 *             variable
	 */
	public void setLocalVariable(String name, Term[] terms, Element value)
			throws EvaluationException {

		assert (name != null);

		if (terms == null || terms.length == 0) {

			// Revert back to the simple case that does not require
			// dereferencing.
			setLocalVariable(name, value);

		} else {

			// The more complicated case where we need to dereference the
			// variable. (And also possibly create the parents.)

			// Retrieve the value of the local variable.
			Element var = getLocalVariable(name);

			// If the value is a protected resource, then make a shallow copy
			// and replace the value of the local variable.
			if (var != null && var.isProtected()) {
				var = var.writableCopy();
				setLocalVariable(name, var);
			}

			// If the value does not exist, create a resource of the correct
			// type and insert into variable table.
			if (var == null || var instanceof Undef) {

				Term term = terms[0];
				if (term.isKey()) {
					var = new HashResource();
				} else {
					var = new ListResource();
				}
				setLocalVariable(name, var);

			}

			// Recursively descend to set the value.
			assert (var != null);
			try {
				var.rput(terms, 0, value);
			} catch (InvalidTermException ite) {
				throw new EvaluationException(ite.formatVariableMessage(name,
						terms));
			}
		}
	}

	public int getCallLimit() {
		return 50;
	}

	public int getIterationLimit() {
		return 1000;
	}

	public boolean isFinal(Path p) {
		// This should never be called.
		return false;
	}

	public String getFinalReason(Path p) {
		// This should never be called.
		return null;
	}

	public void setFinal(Path p) {
		// This should never be called.
	}

	/**
	 * This flag indicates if this context is a special one used for the
	 * compile-time evaluation of DML blocks. Such contexts are very limited in
	 * the functionality that is provided.
	 * 
	 * @return flag indicating if this is a compile-time context
	 */
	public boolean isCompileTimeContext() {
		return true;
	}

	public void initializeSelfHolder(SelfHolder selfHolder) {
		// This should never be called.
	}

	public boolean isSelfFinal() {
		// This should never be called.
		return false;
	}

	public Element getSelf() {
		// This should never be called.
		return null;
	}

	public void clearSelf() {
		// This should never be called.
	}

	public SelfHolder saveSelf() {
		// This should never be called.
		return null;
	}

	public void restoreSelf(SelfHolder self) {
		// This should never be called.
	}

	public void resetSelf(Element newValue) {
		// This should never be called.
	}

	public void setRelativeLoadpaths(List<String> rpaths) {
		// Should never be called.
	}

	public List<String> getRelativeLoadpaths() {
		// Should never be called.
		return null;
	}

	public int getDeprecationLevel() {
		return deprecationLevel;
	}

	public boolean getFailOnWarn() {
		return false;
	}

}
