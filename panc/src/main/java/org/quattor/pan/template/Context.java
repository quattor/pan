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
 $Id: Context.java 3927 2008-11-20 16:47:35Z loomis $
 */

package org.quattor.pan.template;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.quattor.pan.CompilerOptions;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.repository.SourceFile;
import org.quattor.pan.type.FullType;
import org.quattor.pan.utils.FunctionDefinition;
import org.quattor.pan.utils.GlobalVariable;
import org.quattor.pan.utils.Path;
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
public interface Context {

	public static final String TPL_VAR = "TEMPLATE";

	/**
	 * Returns the object Template which is the basis of this ObjectContext.
	 */
	public Template getObjectTemplate();

	/**
	 * Returns the root element for this context.
	 */
	public HashResource getRoot();

	/**
	 * Retrieve and clear the relative root for this context.
	 * 
	 * @param previousValue
	 *            previous value of the relative root to restore
	 * 
	 * @return value of the relative root which was replaced
	 */
	public HashResource restoreRelativeRoot(HashResource previousValue);

	/**
	 * Initializes the relative root for this context and returns the old
	 * relative root. The old value should be restored when the processing of
	 * the structure template is finished.
	 * 
	 * @return old value of the relative root
	 */
	public HashResource createRelativeRoot();

	/**
	 * Returns an unmodifiable copy of the dependencies.
	 */
	public Set<SourceFile> getDependencies();

	/**
	 * Returns an unmodifiable copy of the object dependencies.
	 */
	public Set<String> getObjectDependencies();

	/**
	 * Turn off the object dependency checking. This should only be turned off
	 * after the build phase is complete.
	 */
	public void turnOffObjectDependencyChecking();

	/**
	 * A method to load a template from the local cache. Returns null if the
	 * template is not in the cache.
	 */
	public Template localLoad(String name);

	/**
	 * A method to load a template from the global cache. This may trigger the
	 * global cache to compile the template.
	 */
	public Template globalLoad(String name);

	public Template localAndGlobalLoad(String name, boolean lookupOnly);

	/**
	 * A method to load a template from the global cache. This may trigger the
	 * global cache to compile the template.
	 */
	public Template globalLoad(String name, boolean lookupOnly);

	public SourceFile lookupFile(String name);

	public LocalVariableMap createLocalVariableMap(ListResource argv);

	public void restoreLocalVariableMap(LocalVariableMap localVariableHolder);

	public IteratorMap createIteratorMap();

	public void restoreIteratorMap(IteratorMap iteratorMap);

	/**
	 * Set the name of the object template. Define the necessary variables.
	 */
	public void setObjectAndLoadpath();

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
	public FunctionDefinition getFunction(String name);

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
			SourceRange sourceRange) throws EvaluationException;

	/**
	 * This method returns an unmodifiable, ordered map of the type bindings.
	 * 
	 * @return unmodifiable, ordered map of the type bindings
	 */
	public Map<Path, List<FullType>> getBindings();

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
			SourceRange sourceRange);

	/**
	 * Return the type associated with the given name or null if it doesn't
	 * exist. It will always return null if the argument is null.
	 * 
	 * @param name
	 *            name of the type to retrieve
	 * 
	 * @return FullType associated with this name or null if it doesn't exist
	 */
	public FullType getFullType(String name);

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
			SourceRange sourceRange) throws EvaluationException;

	/**
	 * Set the variable to the given value, preserving the status of the final
	 * flag. This will unconditionally set the value without checking if the
	 * value is final; be careful. The value must already exist.
	 */
	public void setGlobalVariable(String name, Element value);

	/**
	 * Set the variable to the given GlobalVariable. If variable is null, then
	 * the global variable definition is removed.
	 * 
	 * @param name
	 *            global variable name
	 * @param variable
	 *            GlobalVariable to associate with name
	 */
	public void setGlobalVariable(String name, GlobalVariable variable);

	/**
	 * Replaces the given global variable with the given value. The flag
	 * indicates whether or not the variable should be marked as final. Note,
	 * however, that this routine does not respect the final flag and replaces
	 * the value unconditionally. The function returns the old value of the
	 * variable or null if it didn't exist.
	 */
	public GlobalVariable replaceGlobalVariable(String name, Element value,
			boolean finalFlag);

	/**
	 * Set the variable to the given value. If the value is null, then the
	 * variable will be removed from the context.
	 * 
	 */
	public void setGlobalVariable(String name, Element value, boolean finalFlag);

	/**
	 * Register a Resource iterator in the context.
	 */
	public void setIterator(Resource resource, Resource.Iterator iterator);

	/**
	 * Get the iterator for the given resource or null if the iterator has not
	 * been registered.
	 */
	public Resource.Iterator getIterator(Resource resource);

	/**
	 * Unconditionally remove a global variable. This should never be called
	 * from user code. It is used to handle special variables like "self".
	 */
	public void removeGlobalVariable(String name);

	/**
	 * Return the Element which corresponds to the given variable name without
	 * duplicating the value. This is useful when dealing with SELF or with
	 * variables in a context where it is known that the value won't be
	 * modified.
	 */
	public Element getGlobalVariable(String name);

	public GlobalVariable retrieveGlobalVariable(String name);

	public void pushTemplate(Template template, SourceRange sourceRange,
			Level logLevel, String logMessage);

	public void popTemplate(Level logLevel, String logMessage);

	public void printTraceback(SourceRange sourceRange);

	public String getTraceback(SourceRange sourceRange);

	public void setCurrentTemplate(Template template);

	public Template getCurrentTemplate();

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
			ValidationException;

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
			throws EvaluationException;

	public void putElement(Path path, Element value);

	public Element executeDmlBlock(Operation dml);

	public boolean executeDmlValidationBlock(Operation dml, Element self)
			throws ValidationException;

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
	public Element getLocalVariable(String name);

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
	public Element getVariable(String name);

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
			Term[] terms) throws InvalidTermException;

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
			throws EvaluationException;

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
			throws EvaluationException;

	public int getCallLimit();

	public int getIterationLimit();

	public boolean isFinal(Path p);

	public String getFinalReason(Path p);

	public void setFinal(Path p);

	/**
	 * This flag indicates if this context is a special one used for the
	 * compile-time evaluation of DML blocks. Such contexts are very limited in
	 * the functionality that is provided.
	 * 
	 * @return flag indicating if this is a compile-time context
	 */
	public boolean isCompileTimeContext();

	public void initializeSelfHolder(SelfHolder selfHolder);

	public boolean isSelfFinal();

	public Element getSelf();

	public void clearSelf();

	public SelfHolder saveSelf();

	public void restoreSelf(SelfHolder self);

	public void resetSelf(Element newValue);

	public void setRelativeLoadpaths(List<String> rpaths);

	public List<String> getRelativeLoadpaths();

	public CompilerOptions.DeprecationWarnings getDeprecationWarnings();
}
