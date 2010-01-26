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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/tasks/LoadTask.java $
 $Id: LoadTask.java 3455 2008-07-27 05:39:24Z loomis $
 */

package org.quattor.pan.tasks;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quattor.pan.CompilerOptions;
import org.quattor.pan.CompilerLogging.LoggingType;
import org.quattor.pan.cache.CompileCache.PostCompileProcessor;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.exceptions.SystemException;
import org.quattor.pan.parser.ASTTemplate;
import org.quattor.pan.parser.PanParser;
import org.quattor.pan.parser.PanParserAnnotationUtils;
import org.quattor.pan.parser.PanParserAstUtils;
import org.quattor.pan.parser.ParseException;
import org.quattor.pan.template.Template;

import sun.tools.jstat.ParserException;

/**
 * Wraps the <code>CompileCallable</code> as a <code>Task</code>. This wrapping
 * is done to make sure that the <code>CompileCallable</code> is fully
 * constructed before passing it to the <code>FutureTask</code>.
 * 
 * @author loomis
 * 
 */
public class CompileTask extends Task<CompileResult> {

	private static final Logger taskLogger = LoggingType.TASK.logger();

	public CompileTask(String tplfile,
			PostCompileProcessor postCompileProcessor,
			CompilerOptions compilerOptions) {
		super(TaskResult.ResultType.COMPILED, tplfile, new CallImpl(tplfile,
				postCompileProcessor, compilerOptions));
	}

	/**
	 * Compiles the template from scratch. This class may create a task to write
	 * a compiled template to disk. If the compiled template is an object
	 * template, then this may create a task to build the machine configuration.
	 * 
	 * @author loomis
	 * 
	 */
	public static class CallImpl implements Callable<CompileResult> {

		private final File tplfile;

		private final PostCompileProcessor postCompileProcessor;

		private final CompilerOptions compilerOptions;

		public CallImpl(String tplpath,
				PostCompileProcessor postCompileProcessor,
				CompilerOptions compilerOptions) {

			File tplfile = new File(tplpath);

			// Sanity checks.
			assert (tplfile != null);
			assert (tplfile.isAbsolute());
			assert (tplfile.getName().endsWith(".tpl"));

			this.tplfile = tplfile;
			this.postCompileProcessor = postCompileProcessor;
			this.compilerOptions = compilerOptions;
		}

		public CompileResult call() throws Exception {

			// Log the beginning of the template load.
			taskLogger.log(Level.FINER, "START_COMPILE", tplfile
					.getAbsolutePath());

			// Compile the template.
			Template template = compile(tplfile, compilerOptions);

			// Either the load or compilation was successful or an exception was
			// thrown. Hence, we should always have a non-null template value at
			// this point.
			assert (template != null);

			// If this is an object template, then (maybe) start a build.
			postCompileProcessor.process(template);

			// Create the result and return it.
			CompileResult result = new CompileResult(template);

			// Log the end of template load.
			taskLogger.log(Level.FINER, "END_COMPILE", tplfile
					.getAbsolutePath());

			return result;
		}

		/**
		 * Run the compilation.
		 * 
		 * @param tplfile
		 *            file to compile
		 * @param compilerOptions
		 *            compiler options to use for compilation
		 * @throws ParserException
		 *             for low-level parsing errors
		 * @throws SyntaxException
		 *             for files which parse correctly but contain higher-level
		 *             syntax errors
		 * @throws SystemException
		 *             for IO exceptions or unexpected system exceptions; the
		 *             cause is the underlying exception
		 * 
		 */
		public static Template compile(File tplfile,
				CompilerOptions compilerOptions) throws Exception {

			Template template = null;

			// Ensure that the file can actually be read.
			if (!tplfile.canRead()) {
				throw new SystemException("template file cannot be read",
						tplfile);
			}

			// Parse the input file and generate a Template object.
			Reader reader = null;
			try {
				reader = new FileReader(tplfile);

				PanParser parser = new PanParser(reader);
				parser.setFile(tplfile);
				parser.setCompilerOptions(compilerOptions);
				ASTTemplate ast = parser.template();
				template = PanParserAstUtils.convertAstToTemplate(tplfile, ast);

				// TEMPORARY STATEMENT TO TRY OUT ANNOTATION DUMPING.
				// FIXME: Provide dedicated option to activating this.
				PanParserAnnotationUtils.printXML(ast);

			} catch (SyntaxException se) {
				throw se.addExceptionInfo(null, tplfile);
			} catch (ParseException pe) {
				pe.file = tplfile;
				throw pe;
			} catch (java.io.IOException ioe) {
				SystemException se = new SystemException("IO error", tplfile);
				se.initCause(ioe);
				throw se;
			} catch (EvaluationException ee) {
				throw ee.addExceptionInfo(null, tplfile, null);
			} catch (Exception e) {
				SystemException se = new SystemException(
						"unexpected system exception", tplfile);
				se.initCause(e);
				throw se;
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (java.io.IOException consumed) {
					}
				}
			}

			// Unless an exception was thrown, we should always have a non-null
			// value.
			assert (template != null);

			return template;
		}
	}

}
