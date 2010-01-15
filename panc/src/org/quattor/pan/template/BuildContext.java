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

import static org.quattor.pan.utils.MessageUtils.MSG_CANNOT_LOCATE_TEMPLATE;
import static org.quattor.pan.utils.MessageUtils.MSG_CANNOT_MODIFY_GLOBAL_VARIABLE_FROM_DML;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ATTEMPT_TO_SET_EXTERNAL_PATH;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ATTEMPT_TO_SET_RELATIVE_PATH;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_EMPTY_RELATIVE_PATH;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_VALIDATION_FUNCTION_RETURN_TYPE;
import static org.quattor.pan.utils.MessageUtils.MSG_NO_VALUE_FOR_PATH;
import static org.quattor.pan.utils.MessageUtils.MSG_VALIDATION_FAILED_BECAUSE_OF_EXCEPTION;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerLogging.LoggingType;
import org.quattor.pan.cache.BuildCache;
import org.quattor.pan.cache.CompileCache;
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
import org.quattor.pan.repository.SourceRepository;
import org.quattor.pan.tasks.BuildResult;
import org.quattor.pan.tasks.CompileResult;
import org.quattor.pan.template.Template.TemplateType;
import org.quattor.pan.type.FullType;
import org.quattor.pan.utils.FinalFlags;
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
public class BuildContext implements Context {

	private static final String TPL_VAR = "TEMPLATE";

	// Logger to trace template inclusion.
	private static final Logger callLogger = LoggingType.CALL.logger();

	private HashResource root;

	private HashResource relativeRoot;

	private final Compiler compiler;

	private final FunctionMap functions;

	private final TypeMap types;

	private final Map<Path, List<FullType>> bindings;

	private final Map<String, GlobalVariable> globalVariables;

	private LocalVariableMap localVariables;

	private final Stack<SourceLocation> templates;

	private Template currentTemplate;

	private final Template objectTemplate;

	private List<String> relativeLoadpaths;

	private SelfHolder self = new SelfHolder();

	private IteratorMap iteratorMap;

	private final FinalFlags flags;

	private final boolean isCompileTimeContext;

	private boolean checkObjectDependencies;

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
	 * This hash contains all of the templates used to build this machine
	 * configuration. It is also used as a cache to avoid reloading templates
	 * more than once for this configuration. The key is the template name.
	 */
	private Map<String, Template> dependencies;

	/**
	 * This set contains "other" dependencies that consist of templates that
	 * were missing or ordinary files that have been included through the
	 * file_contents() function.
	 */
	private Set<SourceFile> otherDependencies;

	/**
	 * This set contains all of the object templates that this machine
	 * configuration depends upon.
	 */
	private Set<String> objectDependencies;

	/**
	 * Constructs a new Context object intended for testing.
	 */
	public BuildContext() {
		this(null, emptyTemplate, false);
	}

	/**
	 * Constructs a new context for the given root element. The function, type,
	 * and variable hashes are initially empty. (In particular, the object
	 * variable is not automatically defined.
	 * 
	 * @param compiler
	 *            reference to the compiler and options
	 * @param objectTemplate
	 *            An "object" Template to construct
	 */
	public BuildContext(Compiler compiler, Template objectTemplate) {

		this(compiler, objectTemplate, false);
	}

	/**
	 * Constructs a new context for the given root element. The function, type,
	 * and variable hashes are initially empty. (In particular, the object
	 * variable is not automatically defined.
	 * 
	 * @param compiler
	 *            reference to the compiler and options
	 * @param objectTemplate
	 *            An "object" Template to construct
	 * @param isCompileTimeContext
	 *            Flag to indicate if this is a compile-time context
	 */
	private BuildContext(Compiler compiler, Template objectTemplate,
			boolean isCompileTimeContext) {

		// Set the root to an empty hash.
		root = new HashResource();

		// Set the compiler.
		this.compiler = compiler;

		// Copy in a couple of options used for function evaluation. The
		// compiler will be null for compile-time DML evaluations.
		if (compiler != null) {
			this.deprecationLevel = compiler.options.deprecationLevel;
		} else {
			this.deprecationLevel = -1;
		}

		// Create the empty hashes.
		functions = new FunctionMap();
		types = new TypeMap();
		globalVariables = new HashMap<String, GlobalVariable>();
		templates = new Stack<SourceLocation>();
		bindings = new TreeMap<Path, List<FullType>>();
		dependencies = new HashMap<String, Template>();
		otherDependencies = new TreeSet<SourceFile>();
		flags = new FinalFlags();

		localVariables = new LocalVariableMap();
		iteratorMap = new IteratorMap();

		// Create set for object dependencies. The list will usually be
		// short, so use a TreeSet to save memory. Iteration performance should
		// be fine.
		objectDependencies = new TreeSet<String>();

		// Set the object template and add it as a dependency.
		assert (objectTemplate != null);
		assert (objectTemplate.type == TemplateType.OBJECT);
		this.objectTemplate = objectTemplate;
		dependencies.put(objectTemplate.name, objectTemplate);
		objectDependencies.add(objectTemplate.name);

		// Set the compile-time flag.
		this.isCompileTimeContext = isCompileTimeContext;

		// Always start with object dependency checking. This should be turned
		// off only after the build phase is complete.
		this.checkObjectDependencies = true;

		// Setup the default for the relative load paths (i.e. the LOADPATH
		// variable in pan language). There must always be at least one entry,
		// the empty string that indicates the current directory.
		relativeLoadpaths = new LinkedList<String>();
		relativeLoadpaths.add("");
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
		return root;
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
		HashResource value = relativeRoot;
		relativeRoot = previousValue;
		return value;
	}

	/**
	 * Initializes the relative root for this context and returns the old
	 * relative root. The old value should be restored when the processing of
	 * the structure template is finished.
	 * 
	 * @return old value of the relative root
	 */
	public HashResource createRelativeRoot() {
		HashResource oldRelativeRoot = relativeRoot;
		relativeRoot = new HashResource();
		return oldRelativeRoot;
	}

	/**
	 * Returns an unmodifiable copy of the dependencies.
	 */
	public Set<SourceFile> getDependencies() {

		Set<SourceFile> sourceFiles = new TreeSet<SourceFile>();

		// Include all of the standard dependencies.
		for (Template t : dependencies.values()) {
			sourceFiles.add(t.sourceFile);
		}

		// Add the files that were looked-up but not found as well as the files
		// included via the file_contents() function.
		sourceFiles.addAll(otherDependencies);

		return Collections.unmodifiableSet(sourceFiles);
	}

	/**
	 * Returns an unmodifiable copy of the object dependencies.
	 */
	public Set<String> getObjectDependencies() {
		return Collections.unmodifiableSet(objectDependencies);
	}

	/**
	 * Turn off the object dependency checking. This should only be turned off
	 * after the build phase is complete.
	 */
	public void turnOffObjectDependencyChecking() {
		checkObjectDependencies = false;
	}

	/**
	 * A method to load a template from the local cache. Returns null if the
	 * template is not in the cache.
	 */
	public Template localLoad(String name) {
		return dependencies.get(name);
	}

	/**
	 * A method to load a template from the global cache. This may trigger the
	 * global cache to compile the template.
	 */
	public Template globalLoad(String name) {
		return globalLoad(name, false);
	}

	public Template localAndGlobalLoad(String name, boolean lookupOnly) {
		Template template = localLoad(name);
		if (template == null) {
			template = globalLoad(name, lookupOnly);
		}
		return template;
	}

	/**
	 * A method to load a template from the global cache. This may trigger the
	 * global cache to compile the template.
	 */
	public Template globalLoad(String name, boolean lookupOnly) {

		// If this context was created without a Compiler object specified, then
		// no global lookups can be done. Just return null indicating that the
		// requested template can't be found.
		if (compiler == null) {
			return null;
		}

		// Use the full lookup to find the correct template file. This must
		// always be done on a global load because the actual template file on
		// disk may be different for different object templates. The raw
		// (unduplicated) value of LOADPATH can be used because it will not be
		// changed by the code below.
		SourceRepository repository = compiler.getSourceRepository();
		SourceFile source = repository.retrievePanSource(name,
				relativeLoadpaths);

		if (source.isAbsent()) {
			if (lookupOnly) {

				// Files that were searched for but not found are still
				// dependencies. Keep track of these so that they can be
				// included in the dependency file and checked when trying to
				// see if profiles are up-to-date.
				otherDependencies.add(source);
				return null;
			} else {

				// The lookupOnly flag was not set, so it is an error if the
				// template has not been found.
				throw EvaluationException.create((SourceRange) null,
						(BuildContext) this, MSG_CANNOT_LOCATE_TEMPLATE, name);
			}
		}

		// Now actually retrieve the other object's root, waiting if the
		// result isn't yet available.
		CompileCache ccache = compiler.getCompileCache();
		CompileResult cresult = ccache.waitForResult(source.getPath()
				.getAbsolutePath());

		Template template = null;
		try {
			// Extract the compiled template and ensure that the name is
			// correct. The template must not be null if no exception is thrown.
			template = cresult.template;
			template.templateNameVerification(name);

			// Found the template. Put this into the dependencies only if we're
			// really going to use it. I.e. if the lookupOnly flag is false.
			if (!lookupOnly) {
				dependencies.put(name, template);
				if (template.type == TemplateType.OBJECT) {
					objectDependencies.add(template.name);
				}
			}

		} catch (SyntaxException se) {

			// This can happen if there is a syntax error while including
			// the given template. If this isn't just a lookup, then convert
			// this into an evaluation exception
			// and throw it.
			if (!lookupOnly) {
				throw new EvaluationException(se.getMessage());
			} else {
				template = null;
			}

		} catch (EvaluationException ee) {

			// Eat the exception if we're only doing a lookup; otherwise,
			// rethrow it.
			if (!lookupOnly) {
				throw ee;
			} else {
				template = null;
			}

		}

		return template;
	}

	public SourceFile lookupFile(String name) {
		SourceRepository repository = compiler.getSourceRepository();
		SourceFile source = repository.retrieveTxtSource(name,
				relativeLoadpaths);
		otherDependencies.add(source);
		return source;
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
		return functions.get(name);
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
		functions.put(name, function, template, sourceRange);
	}

	/**
	 * This method returns an unmodifiable, ordered map of the type bindings.
	 * 
	 * @return unmodifiable, ordered map of the type bindings
	 */
	public Map<Path, List<FullType>> getBindings() {
		return Collections.unmodifiableMap(bindings);
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

		assert (path != null);
		assert (path.isAbsolute());
		assert (fullType != null);

		// Must make sure that all of the subtypes for the given type are
		// defined before adding the binding.
		try {
			fullType.verifySubtypesDefined(types);
		} catch (EvaluationException ee) {
			throw ee.addExceptionInfo(sourceRange, template.source, this
					.getTraceback(sourceRange));
		}

		// Retrieve or create the list of bindings for this path.
		List<FullType> list = bindings.get(path);
		if (list == null) {
			list = new LinkedList<FullType>();
			bindings.put(path, list);
		}

		// Add the binding.
		assert (list != null);
		list.add(fullType);
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
		return types.get(name);
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
		types.put(name, fullType, template, sourceRange);
	}

	/**
	 * Set the variable to the given value, preserving the status of the final
	 * flag. This will unconditionally set the value without checking if the
	 * value is final; be careful. The value must already exist.
	 */
	public void setGlobalVariable(String name, Element value) {

		assert (name != null);

		GlobalVariable gvar = globalVariables.get(name);
		gvar.setValue(value);
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

		assert (name != null);

		if (variable != null) {
			globalVariables.put(name, variable);
		} else {
			globalVariables.remove(name);
		}
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

		assert (name != null);

		GlobalVariable oldVariable = globalVariables.get(name);
		GlobalVariable newVariable = new GlobalVariable(finalFlag, value);
		globalVariables.put(name, newVariable);
		return oldVariable;
	}

	/**
	 * Set the variable to the given value. If the value is null, then the
	 * variable will be removed from the context.
	 * 
	 */
	public void setGlobalVariable(String name, Element value, boolean finalFlag) {

		assert (name != null);

		// Either modify an existing value (with appropriate checks) or add a
		// new one.
		if (globalVariables.containsKey(name)) {

			GlobalVariable gvar = globalVariables.get(name);
			if (!gvar.getFinalFlag()) {
				gvar.setValue(value);
				gvar.setFinalFlag(finalFlag);
			} else {
				throw new EvaluationException(
						"attempt to modify final global variable named " + name);
			}

		} else if (value != null) {
			GlobalVariable gvar = new GlobalVariable(finalFlag, value);
			globalVariables.put(name, gvar);
		}
	}

	/**
	 * Mark the global variable as final.
	 * 
	 */
	public void setGlobalVariableAsFinal(String name) {

		assert (name != null);

		// Normal processing: just pull out the existing variable and set the
		// flag.
		GlobalVariable gvar = globalVariables.get(name);
		try {
			gvar.setFinalFlag(true);
		} catch (NullPointerException npe) {
			throw CompilerError.create(
					MessageUtils.MSG_FINAL_FOR_NON_EXISTANT_VARIABLE, name);
		}
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
		assert (name != null);
		globalVariables.remove(name);
	}

	/**
	 * Return the Element which corresponds to the given variable name without
	 * duplicating the value. This is useful when dealing with SELF or with
	 * variables in a context where it is known that the value won't be
	 * modified.
	 */
	public Element getGlobalVariable(String name) {
		GlobalVariable gvar = globalVariables.get(name);
		return (gvar != null) ? gvar.getValue() : null;
	}

	public void pushTemplate(Template template, SourceRange sourceRange,
			Level logLevel, String logMessage) {

		SourceLocation location = new SourceLocation(currentTemplate,
				sourceRange);
		templates.push(location);
		currentTemplate = template;

		// Log what template we're entering.
		callLogger.log(logLevel, "ENTER", new Object[] { logMessage,
				currentTemplate.name, currentTemplate.source });

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

		// Set the initial node to use.
		Element node = null;

		// The initial element to use depends on the type of path. Define the
		// correct root element.
		switch (path.getType()) {
		case ABSOLUTE:

			// Typical, very easy case. All absolute paths refer to this object.
			node = root;
			break;

		case RELATIVE:

			// Check to see if we are within a structure template by checking if
			// relativeRoot is set. If set, then proceed with lookup, otherwise
			// fail.
			if (relativeRoot != null) {
				node = relativeRoot;
			} else {
				throw new EvaluationException(
						"relative path ('"
								+ path
								+ "') cannot be used to retrieve element in configuration");
			}

			break;

		case EXTERNAL:

			// This is an external path. Check the authority.
			String myObject = objectTemplate.name;
			String externalObject = path.getAuthority();

			if (myObject.equals(externalObject)) {

				// Easy case, this references itself. Just set the initial node
				// to the root of this object.
				node = root;

			} else {

				// FIXME: Review this code. Much can probably be deleted.

				// Harder case, we must lookup the other object template,
				// compiling and building it as necessary.

				// Try loading the template. This may throw an
				// EvaluationException if something goes wrong in the load.
				Template externalTemplate = localAndGlobalLoad(externalObject,
						!errorIfNotFound);

				// Check to see if the template was found.
				if (externalTemplate != null && !errorIfNotFound) {

					// If we asked for only a lookup of the template, then we
					// need to ensure that the referenced template is added to
					// the dependencies.
					dependencies.put(externalObject, externalTemplate);
					objectDependencies.add(externalObject);

				} else if (externalTemplate == null) {

					// Throw an error or return null as appropriate.
					if (errorIfNotFound) {
						throw new EvaluationException("object template "
								+ externalObject + " could not be found", null);
					} else {
						return null;
					}
				}

				// Retrieve the build cache.
				BuildCache bcache = compiler.getBuildCache();

				// Only check the object dependencies if this object is
				// currently in the "build" phase. If this is being validated,
				// then circular dependencies will be handled without problems.
				// If dependencies are checked, check BEFORE waiting for the
				// external object, otherwise the compilation may deadlock.
				if (checkObjectDependencies) {
					bcache.setDependency(myObject, externalObject);
				}

				// Wait for the result and set the node to the external object's
				// root element.
				BuildResult result = (BuildResult) bcache
						.waitForResult(externalObject);
				node = result.getRoot();

			}

			break;
		}

		// Now that the root node is defined, recursively descend through the
		// given terms to retrieve the desired element.
		assert (node != null);
		try {
			node = node.rget(path.getTerms(), 0, node.isProtected(),
					!errorIfNotFound);
		} catch (InvalidTermException ite) {
			throw new EvaluationException(ite.formatMessage(path));
		}

		if (!errorIfNotFound || node != null) {
			return node;
		} else {
			throw new EvaluationException(MessageUtils.format(
					MSG_NO_VALUE_FOR_PATH, path.toString()));
		}

	}

	public void putElement(Path path, Element value) {

		if (path.isAbsolute() || path.isRelative()) {

			Term[] terms = path.getTerms();
			int nterms = terms.length;

			// If the list is empty, then we are trying to modify the root
			// resource directly. Ensure that the value is an instance of a
			// HashResource.
			if (path.isAbsolute() && nterms == 0) {
				if (value == null) {
					throw new EvaluationException(
							"cannot set root element to null");
				}
				try {
					root = (HashResource) value;
					return;
				} catch (ClassCastException cce) {
					throw new EvaluationException(
							"root element cannot be replaced by element of type "
									+ value.getTypeAsString());
				}
			}

			// If the relativeRoot variable is null and this is a relative path,
			// then we are trying to set a relative path from somewhere other
			// than a create() function call. Also check for a relative path
			// with no term. Both indicate an error in the compiler.
			if (path.isRelative()) {
				if (relativeRoot == null) {
					throw CompilerError.create(
							MSG_INVALID_ATTEMPT_TO_SET_RELATIVE_PATH, path
									.toString());
				}
				if (nterms == 0) {
					throw CompilerError.create(MSG_INVALID_EMPTY_RELATIVE_PATH);
				}
			}

			// Start with the appropriate root element.
			Resource node = (path.isAbsolute()) ? (Resource) root
					: (Resource) relativeRoot;

			assert (node != null) : "root or relativeRoot is unexpectedly null";

			// Need to ensure that the root isn't itself protected. If so,
			// create writable copy and put in back in the correct place. The
			// node must be a HashResource, so one can safely cast to this type.
			if (node.isProtected()) {
				HashResource unprotected = (HashResource) node.writableCopy();
				node = unprotected;
				if (path.isAbsolute()) {
					root = unprotected;
				} else {
					relativeRoot = unprotected;
				}
			}

			try {
				node.rput(terms, 0, value);
			} catch (InvalidTermException ite) {
				throw new EvaluationException(ite.formatMessage(path));
			}

		} else {

			// Any errors of this type should have been caught in the the
			// template compilation or in the assignment statement itself. Throw
			// a CompilerError if this occurs.
			throw CompilerError.create(
					MSG_INVALID_ATTEMPT_TO_SET_EXTERNAL_PATH, path.toString());
		}

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
		initializeSelf(self);

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

		// Only truly local variables can be set via this method. Throw an
		// exception if a global variable is found which matches the name.
		if (globalVariables.containsKey(name)) {
			throw new EvaluationException(MessageUtils.format(
					MSG_CANNOT_MODIFY_GLOBAL_VARIABLE_FROM_DML, name));
		}

		// Ok, save the value.
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

		// Only truly local variables can be set via this method. Throw an
		// exception if a global variable is found which matches the name.
		if (globalVariables.containsKey(name)) {
			throw new EvaluationException(MessageUtils.format(
					MSG_CANNOT_MODIFY_GLOBAL_VARIABLE_FROM_DML, name));
		}

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
		return (compiler != null) ? compiler.options.callDepthLimit : 50;
	}

	public int getIterationLimit() {
		return (compiler != null) ? compiler.options.iterationLimit : 1000;
	}

	public boolean isFinal(Path p) {
		return flags.isFinal(p);
	}

	public String getFinalReason(Path p) {
		return flags.getFinalReason(p);
	}

	public void setFinal(Path p) {
		flags.setFinal(p);
	}

	/**
	 * This flag indicates if this context is a special one used for the
	 * compile-time evaluation of DML blocks. Such contexts are very limited in
	 * the functionality that is provided.
	 * 
	 * @return flag indicating if this is a compile-time context
	 */
	public boolean isCompileTimeContext() {
		return isCompileTimeContext;
	}

	public Element initializeSelf(Path path) {

		assert (path != null);

		// Create a new self structure if there isn't one already.
		if (self == null) {
			self = new SelfHolder();
		}

		// Since we're initializing to a path, the variable reference is null
		// and the SELF variable is modifiable.
		self.variable = null;
		self.unmodifiable = false;

		// Set SELF to the value of the path, using undef if there is no value
		// yet.
		self.path = path;
		self.element = getElement(path, false);
		if (self.element == null) {
			self.element = Undef.VALUE;
			putElement(path, self.element);
		}

		assert (self.element != null);
		assert (self.path != null);

		return self.element;
	}

	public Element initializeSelf(String vname) {
		assert (vname != null);

		self.path = null;
		self.unmodifiable = false;

		self.variable = globalVariables.get(vname);
		if (self.variable == null) {
			self.variable = new GlobalVariable(false, Undef.VALUE);
			globalVariables.put(vname, self.variable);
		}

		// Retrieve an unprotected value of the variable. Since we're going to
		// modify SELF anyway, there is no need to force a copy if the value is
		// modified.
		self.element = self.variable.getUnprotectedValue();

		assert (self.element != null);
		assert (self.variable != null);

		return self.element;
	}

	public Element initializeSelf(Element e) {
		self.element = e;
		self.path = null;
		self.variable = null;
		self.unmodifiable = true;

		return self.element;
	}

	public boolean isSelfFinal() {
		return self.unmodifiable;
	}

	public Element getSelf() {
		return self.element;
	}

	public void clearSelf() {
		self.variable = null;
		self.path = null;
		self.element = null;
	}

	public SelfHolder saveSelf() {
		return self.clone();
	}

	public void restoreSelf(SelfHolder self) {
		this.self = self;
	}

	public void resetSelf(Element newValue) {
		if (self.element != newValue) {
			self.element = newValue;

			if (self.variable != null) {
				self.variable.setValue(newValue);
			} else if (self.path != null) {
				putElement(self.path, newValue);
			} else {
				throw new EvaluationException(
						"cannot modify SELF from validation function");
			}
		}
	}

	public void setRelativeLoadpaths(List<String> rpaths) {
		relativeLoadpaths = rpaths;
	}

	public List<String> getRelativeLoadpaths() {
		return relativeLoadpaths;
	}

	public int getDeprecationLevel() {
		return deprecationLevel;
	}

}
