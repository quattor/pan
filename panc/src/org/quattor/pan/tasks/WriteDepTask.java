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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/tasks/WriteDepTask.java $
 $Id: WriteDepTask.java 3732 2008-10-01 19:27:29Z jouvin $
 */

package org.quattor.pan.tasks;

import static org.quattor.pan.utils.MessageUtils.MSG_CANNOT_CREATE_OUTPUT_DIRECTORY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerLogging.LoggingType;
import org.quattor.pan.cache.Valid2Cache;
import org.quattor.pan.exceptions.SystemException;
import org.quattor.pan.template.Template;
import org.quattor.pan.utils.MessageUtils;

/**
 * Wraps the <code>WriteDepCallable</code> as a <code>Task</code>. This wrapping
 * is done to make sure that the <code>WriteDepCallable</code> is fully
 * constructed before passing it to the <code>FutureTask</code>.
 * 
 * @author loomis
 * 
 */
public class WriteDepTask extends Task<TaskResult> {

	private static final Logger taskLogger = LoggingType.TASK.logger();

	public WriteDepTask(Compiler compiler, String objectName,
			File outputDirectory) {
		super(TaskResult.ResultType.DEP, objectName, new CallImpl(compiler,
				objectName, outputDirectory));
	}

	/**
	 * Writes the dependency file for a given machine configuration file to
	 * disk.
	 * 
	 * @author loomis
	 * 
	 */
	private static class CallImpl implements Callable<TaskResult> {

		private final File outputDirectory;

		private final String objectName;

		private final Compiler compiler;

		public CallImpl(Compiler compiler, String objectName,
				File outputDirectory) {

			this.outputDirectory = outputDirectory;
			this.objectName = objectName;
			this.compiler = compiler;
		}

		public TaskResult call() throws Exception {

			Valid2Cache v2cache = compiler.getValid2Cache();

			// This list contains the names of all of the object templates that
			// have already been processed. This is done to prevent reprocessing
			// of object templates and infinite loops from circular
			// dependencies.
			List<String> processed = new LinkedList<String>();

			// This list contains all of the unprocessed object templates. The
			// initial template to process is added to start the process.
			Stack<String> unprocessed = new Stack<String>();
			unprocessed.push(objectName);

			// Use URI instances to operate on the output directory and the
			// object name. The object name can be namespaced, so this extra
			// processing is needed.
			URI odir = outputDirectory.toURI();
			URI oname = new URI(objectName);
			URI resolvedAbsoluteURI = odir.resolve(oname);
			String resolvedAbsolutePath = resolvedAbsoluteURI
					.getSchemeSpecificPart()
					+ ".xml.dep";
			File absolutePath = new File(resolvedAbsolutePath);

			// Extract the parent directory and ensure that it exists. If
			// the creation fails, ignore it. The error will be caught
			// below.
			File parent = absolutePath.getParentFile();
			if (!parent.exists() && !parent.mkdirs()) {
				throw new SystemException(MessageUtils.format(
						MSG_CANNOT_CREATE_OUTPUT_DIRECTORY, parent
								.getAbsolutePath()), parent);
			}

			// This will contain the timestamp of the file. This is a Long to
			// allow the initial value to be null. We will take the timestamp of
			// the first object template processed, which should be the correct
			// one.
			Long timestamp = null;

			// Open the output file for the dependency information.
			PrintStream ps = new PrintStream(absolutePath);

			// Mark the beginning of writing dependency file.
			taskLogger.log(Level.FINER, "START_DEPFILE", objectName);

			// Loop until there are no more unprocessed object templates.
			while (!unprocessed.empty()) {
				String objectToProcess = unprocessed.pop();

				// Only do something if the object template hasn't already been
				// processed.
				if (!processed.contains(objectToProcess)) {
					processed.add(objectToProcess);

					// Get the result from the cache, waiting if necessary.
					Valid2Result result = (Valid2Result) v2cache
							.waitForResult(objectToProcess);

					// Extract the timestamp, if necessary.
					if (timestamp == null) {
						timestamp = Long.valueOf(result.timestamp);
					}

					// Write out the dependencies. Add all of the object
					// templates encountered during the processing to the
					// unprocessed list.
					unprocessed.addAll(writeDependencies(ps, result
							.getDependencies()));

				}

			}

			ps.close();

			// Mark the end of writing dependency file.
			taskLogger.log(Level.FINER, "END_DEPFILE", objectName);

			// Make sure the modification time corresponds to the given
			// timestamp.
			if (!absolutePath.setLastModified(timestamp)) {
				// Probably a warning should be emitted here, but currently
				// there are no facilities for warnings in the pan compiler
				// yet.
			}

			return new TaskResult(TaskResult.ResultType.DEP);
		}

		/**
		 * This method walks through the given dependencies and writes them to
		 * the given stream.
		 * 
		 * @param ps
		 *            PrintStream to write the dependencies to
		 * @param dependencies
		 *            list of dependencies
		 * 
		 * @return list of object templates encountered during processing
		 * 
		 * @throws URISyntaxException
		 * @throws FileNotFoundException
		 */
		private List<String> writeDependencies(PrintStream ps,
				Map<String, Template> dependencies) throws URISyntaxException,
				FileNotFoundException {

			// List to hold names of object templates encountered during
			// processing.
			List<String> objectTemplates = new LinkedList<String>();

			if (dependencies != null) {

				for (Map.Entry<String, Template> entry : dependencies
						.entrySet()) {
					String tname = entry.getKey();
					Template template = entry.getValue();
					String tfile = template.source.toString();

					// If the template is an object template, then save the name
					// for further processing by the parent.
					if (template.type == Template.TemplateType.OBJECT) {
						objectTemplates.add(template.name);
					}

					// Must strip off the template name from the file name.
					String fsname = tname.replace('/', File.separatorChar)
							+ ".tpl";

					// There should always be a match here, so the check
					// should never be executed.
					int index = tfile.lastIndexOf(fsname);
					if (index < 0) {
						index = tfile.length();
					}
					tfile = tfile.substring(0, index);

					ps.printf("\"%s\" \"%s\"%n", tname, tfile);
				}

			}

			return objectTemplates;
		}
	}

}
