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
 $Id: BuildTask.java 3230 2008-05-25 12:55:24Z loomis $
 */

package org.quattor.pan.tasks;

import static org.quattor.pan.utils.MessageUtils.MSG_VALUE_AT_PATH_UNDEFINED;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerLogging.LoggingType;
import org.quattor.pan.cache.BuildCache;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ElementUtils;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.ttemplate.Context;
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
public class Valid1Task extends Task<Valid1Result> {

	private static final Logger taskLogger = LoggingType.TASK.logger();

	public Valid1Task(Compiler compiler, String objectName) {
		super(TaskResult.ResultType.VALID1, objectName, new CallImpl(compiler,
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
	private static class CallImpl implements Callable<Valid1Result> {

		private final Compiler compiler;

		private final String objectName;

		public CallImpl(Compiler compiler, String objectName) {
			this.compiler = compiler;
			this.objectName = objectName;
		}

		public Valid1Result call() throws Exception {

			BuildCache bcache = compiler.getBuildCache();

			// Now actually retrieve the other object's root, waiting if the
			// result isn't yet available.
			BuildResult result = (BuildResult) bcache.waitForResult(objectName);
			Context context = result.getObjectContext();

			// Log the beginning of the build phase.
			taskLogger.log(Level.FINER, "START_VALID1", objectName);

			// Run through the building and validation phases of processing.
			validate(objectName, context);

			// Log the end of the build phase.
			taskLogger.log(Level.FINER, "END_VALID1", objectName);

			// Return the actual result. Always protect the final root value.
			// This will force any values coming out of the finished
			// configuration to be protected as well.
			return new Valid1Result(context.getRoot().protect(), context);
		}

		/**
		 * Validate the generated configuration information.
		 *
		 * @param context
		 *            context (and configuration) to validate
		 */
		private void validate(String objectName, Context context)
				throws ValidationException {

			File objectFile = (context.getObjectTemplate() != null) ? (context
					.getObjectTemplate().source) : null;

			// First check that the tree contains no undefined elements. If so,
			// throw a validation error with the returned path. Note that this
			// method only finds the first undefined element.
            String rpath = ElementUtils.locateUndefinedElement(context.getRoot());
			if (rpath != null) {
				ValidationException ve = ValidationException.create(
						MSG_VALUE_AT_PATH_UNDEFINED, rpath);
				throw ve.setObjectTemplate(objectFile);
			}

			// Retrieve the type bindings.
			Map<Path, List<FullType>> bindings = context.getBindings();

			// Loop over each of the type bindings and validate each one.
			for (Map.Entry<Path, List<FullType>> entry : bindings.entrySet()) {

				Path path = entry.getKey();

				// Get the "self" value. Check that this isn't null later when
				// we have more information about the type binding.
				Element self = null;
				try {
					self = context.getElement(path);
				} catch (EvaluationException consumed) {
					// If there was an error on the initial lookup, then leave
					// the value as null. It will be checked below where there
					// is more information on the type definition.
				}

				// There can be more than one binding per path. Loop over all of
				// them.
				for (FullType type : entry.getValue()) {

					// Validate the self element against the given type. An
					// exception will be thrown for any problems. Fill in the
					// details for any validation exceptions.
					try {
						type.validate(context, self);
					} catch (ValidationException ve) {
						throw ve.setPathTypeAndObject(path, type, objectFile);
					}
				}
			}

		}

	}
}
