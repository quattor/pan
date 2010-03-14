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

import java.io.File;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quattor.pan.CompilerLogging.LoggingType;
import org.quattor.pan.parser.ASTTemplate;
import org.quattor.pan.parser.PanParserAnnotationUtils;

/**
 * Wraps the <code>WriteAnnotationCallable</code> as a <code>Task</code>. This
 * wrapping is done to make sure that the <code>WriteAnnotationCallable</code>
 * is fully constructed before passing it to the <code>FutureTask</code>.
 * 
 * @author loomis
 * 
 */
public class WriteAnnotationTask extends Task<TaskResult> {

	private static final Logger taskLogger = LoggingType.TASK.logger();

	public WriteAnnotationTask(File annotationDirectory, ASTTemplate ast) {
		super(TaskResult.ResultType.ANNOTATION, ast.getIdentifier(),
				new CallImpl(annotationDirectory, ast));
	}

	/**
	 * Writes the annotation file for a given source file.
	 * 
	 * @author loomis
	 * 
	 */
	private static class CallImpl implements Callable<TaskResult> {

		private final File outputDirectory;

		private final String objectName;

		private final ASTTemplate ast;

		public CallImpl(File annotationDirectory, ASTTemplate ast) {

			this.outputDirectory = annotationDirectory;
			this.objectName = ast.getIdentifier();
			this.ast = ast;
		}

		public TaskResult call() throws Exception {

			// Mark the beginning of writing dependency file.
			taskLogger.log(Level.FINER, "START_ANNOTATION_FILE", objectName);

			if (outputDirectory != null) {
				PanParserAnnotationUtils.printXML(outputDirectory, ast);
			}

			// Mark the end of writing dependency file.
			taskLogger.log(Level.FINER, "END_ANNOTATION_FILE", objectName);

			return new TaskResult(TaskResult.ResultType.ANNOTATION);
		}
	}
}
