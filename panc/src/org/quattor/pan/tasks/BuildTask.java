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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/tasks/BuildTask.java $
 $Id: BuildTask.java 3604 2008-08-19 10:17:25Z loomis $
 */

package org.quattor.pan.tasks;

import static org.quattor.pan.utils.MessageUtils.MSG_CANNOT_LOCATE_OBJECT_TEMPLATE;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerLogging.LoggingType;
import org.quattor.pan.cache.CompileCache;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.repository.SourceFile;
import org.quattor.pan.template.BuildContext;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.template.Template;
import org.quattor.pan.type.FullType;
import org.quattor.pan.utils.Path;

/**
 * Wraps the <code>BuildCallable</code> as a <code>Task</code>. This wrapping is
 * done to make sure that the <code>BuildCallable</code> is fully constructed
 * before passing it to the <code>FutureTask</code>.
 * 
 * @author loomis
 * 
 */
public class BuildTask extends Task<BuildResult> {

	private static final Logger callLogger = LoggingType.CALL.logger();

	private static final Logger taskLogger = LoggingType.TASK.logger();

	public BuildTask(Compiler compiler, String objectName) {
		super(TaskResult.ResultType.BUILD, objectName, new CallImpl(compiler,
				objectName));
	}

	/**
	 * Builds a machine configuration by starting with an object template and
	 * running it through the execution, default setting, and validation phases.
	 * This class may create tasks to write the machine configuration or
	 * dependency files to disk.
	 * 
	 * @author loomis
	 * 
	 */
	private static class CallImpl implements Callable<BuildResult> {

		private final Compiler compiler;

		private final String objectName;

		public CallImpl(Compiler compiler, String objectName) {
			this.compiler = compiler;
			this.objectName = objectName;
		}

		public BuildResult call() throws Exception {

			// Locate the object template.
			File tplFile = null;
			SourceFile source = compiler.getSourceRepository()
					.retrievePanSource(objectName);

			// Throw an exception if the object file cannot be found. Even if it
			// was given on the command line, it must be accessible from the
			// include directories otherwise inconsistencies can occur.
			if (SourceFile.Type.MISSING.equals(source.getType())) {
				throw EvaluationException.create((SourceRange) null,
						(Context) null, MSG_CANNOT_LOCATE_OBJECT_TEMPLATE,
						objectName);
			} else {
				tplFile = source.getPath();
			}

			// Now actually retrieve the other object's root, waiting if the
			// result isn't yet available.
			CompileCache ccache = compiler.getCompileCache();
			CompileResult cresult = ccache.waitForResult(tplFile
					.getAbsolutePath());

			// Extract the compiled template and ensure that the name is
			// correct.
			Template template = cresult.template;
			template.templateNameVerification(objectName);

			// Log the beginning of the build phase.
			taskLogger.log(Level.FINER, "START_BUILD", objectName);

			// For inclusion logging, also add the object template itself.
			callLogger.log(Level.INFO, "ENTER", new Object[] { "OBJECT",
					objectName });

			Context context = new BuildContext(compiler, template);

			// Run through the building and validation phases of processing.
			execute(context, objectName);
			setDefaults(context, objectName);

			// The build has been successful. Turn off the object dependency
			// checking for validation phases.
			context.turnOffObjectDependencyChecking();

			// For inclusion logging, log also when the build phase ends.
			callLogger.log(Level.INFO, "EXIT", new Object[] { "OBJECT",
					objectName });

			// Log the end of the build phase.
			taskLogger.log(Level.FINER, "END_BUILD", objectName);

			// Return the actual result. Always protect the final root value.
			// This will force any values coming out of the finished
			// configuration to be protected as well.
			return new BuildResult(context.getRoot().protect(), context);
		}

		/**
		 * Execute the object template and any included templates.
		 */
		private void execute(Context context, String objectName) {

			// Log the beginning of the build phase.
			taskLogger.log(Level.FINER, "START_EXECUTE", objectName);

			try {
				context.setObjectAndLoadpath();
				context.setCurrentTemplate(context.getObjectTemplate());
				context.getObjectTemplate().execute(true, context);
			} catch (EvaluationException ee) {
				throw ee.addExceptionInfo(null,
						context.getCurrentTemplate().source, null);
			}

			// Log the end of the build phase.
			taskLogger.log(Level.FINER, "END_EXECUTE", objectName);

		}

		/**
		 * Set defaults in the tree.
		 */
		private void setDefaults(Context context, String objectName) {

			// Log the beginning of the set defaults phase.
			taskLogger.log(Level.FINER, "START_DEFAULTS", objectName);

			// Retrieve the type bindings.
			Map<Path, List<FullType>> bindings = context.getBindings();

			// Loop over each of the type bindings and validate each one.
			for (Map.Entry<Path, List<FullType>> entry : bindings.entrySet()) {

				Path path = entry.getKey();

				// Lookup the "self" value. (Do not allow it to throw an
				// exception for a non-existing path.) If this throws an
				// exception for another reason, let it slide. It will be picked
				// up in the validation phase.
				Element self = null;
				try {
					self = context.getElement(path, false);
				} catch (EvaluationException consumed) {
					break;
				}

				// If it doesn't exist or has the undef value, then try to find
				// a default value. This must be done before trying to set the
				// defaults on any children.
				if (self == null || self instanceof Undef) {

					// There can be more than one binding per path. Loop over
					// all of them, but stop at the first one which defines a
					// default value.
					for (FullType type : entry.getValue()) {

						// Determine if the type has a default value.
						Element defaultValue = type.findDefault(context);

						// If something was found, set the value, then break out
						// of
						// the loop.
						if (defaultValue != null) {
							self = defaultValue;
							context.putElement(path, defaultValue);
							break;
						}
					}
				}

				// Now if the value exists, loop over all of the type bindings
				// and call the method setDefaults. This will set the default
				// values for any child elements.
				if (self != null && !(self instanceof Undef)) {

					// There can be more than one binding per path.
					for (FullType type : entry.getValue()) {
						Element replacement = type.setDefaults(context, self);
						if (replacement != null) {
							self = replacement;
							context.putElement(path, replacement);
						}
					}
				}

			}

			// Log the end of the set defaults phase.
			taskLogger.log(Level.FINER, "END_DEFAULTS", objectName);

		}
	}
}
