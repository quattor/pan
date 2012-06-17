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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/tasks/WriteXmlTask.java $
 $Id: WriteXmlTask.java 3732 2008-10-01 19:27:29Z jouvin $
 */

package org.quattor.pan.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerLogging.LoggingType;
import org.quattor.pan.cache.Valid2Cache;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.output.Formatter;
import org.quattor.pan.output.FormatterUtils;

/**
 * Wraps the <code>WriteXmlTask</code> as a <code>Task</code>. This wrapping is
 * done to make sure that the <code>WriteXmlTask</code> is fully constructed
 * before passing it to the <code>FutureTask</code>.
 * 
 * @author loomis
 * 
 */
public class WriteXmlTask extends Task<TaskResult> {

	private static final Logger taskLogger = LoggingType.TASK.logger();

	public WriteXmlTask(Formatter formatter, boolean gzipOutput,
			Compiler compiler, String objectName, File outputDirectory) {
		super(TaskResult.ResultType.XML, objectName, new CallImpl(formatter,
				gzipOutput, compiler, objectName, outputDirectory));
	}

	/**
	 * Wraps the <code>WriteXmlCallable</code> as a <code>Task</code>. This
	 * wrapping is done to make sure that the <code>WriteXmlCallable</code> is
	 * fully constructed before passing it to the <code>FutureTask</code>.
	 * 
	 * @author loomis
	 * 
	 */
	private static class CallImpl implements Callable<TaskResult> {

		private final Formatter formatter;

		private final boolean gzipOutput;

		private final Compiler compiler;

		private final String objectName;

		private final File outputDirectory;

		public CallImpl(Formatter formatter, boolean gzipOutput,
				Compiler compiler, String objectName, File outputDirectory) {

			assert (formatter != null);

			this.formatter = formatter;
			this.gzipOutput = gzipOutput;

			this.compiler = compiler;
			this.objectName = objectName;
			this.outputDirectory = outputDirectory;
		}

		public TaskResult call() throws Exception {

			Valid2Cache v2cache = compiler.getValid2Cache();

			// Now actually retrieve the other object's root, waiting if the
			// result isn't yet available.
			Valid2Result result = (Valid2Result) v2cache
					.waitForResult(objectName);
			Element root = result.getRoot();
			long timestamp = result.timestamp;

			// Mark the beginning of writing XML file.
			taskLogger.log(Level.FINER, "START_XMLFILE", objectName);

			File absolutePath = FormatterUtils.getOutputFile(outputDirectory,
					objectName, formatter.getFileExtension());

			FormatterUtils.createParentDirectories(absolutePath);

			OutputStream os = null;
			if (!gzipOutput) {
				os = new FileOutputStream(absolutePath);
			} else {
				absolutePath = new File(absolutePath.toString() + ".gz");
				os = new GZIPOutputStream(new FileOutputStream(absolutePath));
			}

			PrintWriter ps = new PrintWriter(os);
			formatter.write(root, "profile", ps);
			ps.close();

			// Make sure that the file has the timestamp passed into the
			// constructor.
			if (!absolutePath.setLastModified(timestamp)) {
				// Probably a warning should be emitted here, but currently
				// there are no facilities for warnings in the pan compiler
				// yet.
			}

			// Mark the end of writing XML file.
			taskLogger.log(Level.FINER, "END_XMLFILE", objectName);

			return new TaskResult(TaskResult.ResultType.XML);
		}

	}

}
