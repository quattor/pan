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

import static org.quattor.pan.utils.MessageUtils.MSG_OBJECT_DEPENDENCY_RAISED_EXCEPTION;

import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerLogging.LoggingType;
import org.quattor.pan.cache.Valid1Cache;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.repository.SourceFile;
import org.quattor.pan.template.Context;

/**
 * Wraps the <code>Valid2Callable</code> as a <code>Task</code>. This wrapping
 * is done to make sure that the <code>Valid2Callable</code> is fully
 * constructed before passing it to the <code>FutureTask</code>.
 * 
 * @author loomis
 * 
 */
public class Valid2Task extends Task<Valid2Result> {

	private static final Logger taskLogger = LoggingType.TASK.logger();

	public Valid2Task(Compiler compiler, String objectName) {
		super(TaskResult.ResultType.VALID2, objectName, new CallImpl(compiler,
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
	private static class CallImpl implements Callable<Valid2Result> {

		private final Compiler compiler;

		private final String objectName;

		public CallImpl(Compiler compiler, String objectName) {
			this.compiler = compiler;
			this.objectName = objectName;
		}

		public Valid2Result call() throws Exception {

			Valid1Cache v1cache = compiler.getValid1Cache();

			// Now actually retrieve the other object's root, waiting if the
			// result isn't yet available.
			Valid1Result result = (Valid1Result) v1cache
					.waitForResult(objectName);
			Context context = result.getObjectContext();

			// Log the beginning of the build phase.
			taskLogger.log(Level.FINER, "START_VALID2", objectName);

			// Run through the second stage of validation.
			validate(context);

			// Log the end of the build phase.
			taskLogger.log(Level.FINER, "END_VALID2", objectName);

			// Set<SourceFile> allDependencies = resolveAllDependencies();
			Set<SourceFile> allDependencies = new TreeSet<SourceFile>();

			// Return the actual result. Always protect the final root value.
			// This will force any values coming out of the finished
			// configuration to be protected as well.
			return new Valid2Result(objectName, context.getRoot().protect(),
					context.getObjectDependencies(), context.getDependencies(),
					allDependencies);
		}

		/**
		 * Validate the generated configuration information.
		 */
		private void validate(Context context) throws ValidationException {

			// Create the set that will include all of the objects that have
			// already been checked. This is necessary to avoid infinite loops
			// from circular validation dependencies.
			Set<String> checkedObjects = new TreeSet<String>();

			// Create the set of objects that still may need checking.
			Stack<String> queuedObjects = new Stack<String>();

			// Get the object dependencies from the object that we're
			// validating. Make sure to add this object to the list of those
			// that have been checked.
			queuedObjects.addAll(context.getObjectDependencies());
			checkedObjects.add(context.getObjectTemplate().name);

			// Now keep looping over the queued objects until an exception is
			// thrown, or the queue is empty.
			String currentObject = context.getObjectTemplate().name;
			try {
				while (!queuedObjects.isEmpty()) {
					currentObject = queuedObjects.pop();

					// Only need to do something if the object hasn't already
					// been checked.
					if (!checkedObjects.contains(currentObject)) {

						// Get the result from the other compilation.
						Valid1Cache cache = compiler.getValid1Cache();
						Valid1Result result = cache
								.waitForResult(currentObject);
						Context otherContext = result.getObjectContext();

						// Add in the object's dependencies and mark as checked.
						queuedObjects.addAll(otherContext
								.getObjectDependencies());
						checkedObjects.add(currentObject);
					}
				}
			} catch (EvaluationException ee) {
				ValidationException ve = ValidationException.create(
						MSG_OBJECT_DEPENDENCY_RAISED_EXCEPTION, currentObject);
				ve.setObjectTemplate(context.getObjectTemplate().source);
				throw ve;
			}

		}

	}
}
