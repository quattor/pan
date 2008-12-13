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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/cache/TemplateCache.java $
 $Id: TemplateCache.java 3456 2008-07-27 06:37:13Z loomis $
 */

package org.quattor.pan.cache;

import java.io.File;
import java.util.concurrent.Future;

import org.quattor.pan.Compiler;
import org.quattor.pan.output.Formatter;
import org.quattor.pan.tasks.CompileResult;
import org.quattor.pan.tasks.CompileTask;
import org.quattor.pan.tasks.Task;
import org.quattor.pan.tasks.TaskResult;
import org.quattor.pan.tasks.WriteDepTask;
import org.quattor.pan.tasks.WriteXmlTask;
import org.quattor.pan.template.Template;
import org.quattor.pan.template.Template.TemplateType;

/**
 * Contains a global cache of all compiled templates.
 * 
 * @author loomis
 * 
 */
public class CompileCache extends AbstractCache<CompileResult> {

	private final CompileCache.PostCompileProcessor postCompileProcessor;

	/**
	 * Creates a new <code>CompileCache</code> with a default, initial size of
	 * 1000 entries.
	 * 
	 * @param compiler
	 *            associated compiler for this cache
	 */
	public CompileCache(Compiler compiler) {
		this(compiler, 1000);
	}

	public CompileCache(Compiler compiler, int size) {
		super(compiler, size);

		// Setup the post compilation processor.
		postCompileProcessor = new CompileCache.PostCompileProcessor(compiler,
				compiler.options.xmlWriteEnabled,
				compiler.options.depWriteEnabled);
	}

	/**
	 * Unconditionally compiles the template, but does not put it into the
	 * cache. This is useful for (re)compilations of a large number of template
	 * where keeping them for a build is not necessary.
	 * 
	 * @param tplfile
	 *            absolute path of the file to compile
	 * 
	 * @return return a result that contains the compiled template
	 */
	public Future<CompileResult> compile(String tplfile) {
		Task<CompileResult> task = createTask(tplfile);
		compiler.submit(task);
		return task;
	}

	/**
	 * This class encapsulates the post compilation behavior of the cache. If
	 * output is requested, then the processor will submit object templates
	 * discovered during the build process to also be built.
	 * 
	 * @author loomis
	 * 
	 */
	public static class PostCompileProcessor {

		private final Compiler compiler;
		private final boolean doXML;
		private final boolean doDep;

		public PostCompileProcessor(Compiler compiler, boolean doXML,
				boolean doDep) {
			this.compiler = compiler;
			this.doXML = doXML;
			this.doDep = doDep;
		}

		public void process(Template template) {
			if (template.type == TemplateType.OBJECT) {

				String objectName = template.name;

				if (doXML) {
					Formatter formatter = compiler.options.formatter;
					File outputDirectory = compiler.options.outputDirectory;
					Task<TaskResult> task = new WriteXmlTask(formatter,
							compiler.options.gzipOutput, compiler, objectName,
							outputDirectory);
					compiler.submit(task);
				}

				if (doDep) {
					File outputDirectory = compiler.options.outputDirectory;
					Task<TaskResult> task = new WriteDepTask(compiler,
							objectName, outputDirectory);
					compiler.submit(task);
				}

			}
		}
	}

	@Override
	protected CompileTask createTask(String tplfile) {
		return new CompileTask(tplfile, postCompileProcessor, compiler.options);

	}

	@Override
	protected TaskResult.ResultType getExecutorQueueType() {
		return TaskResult.ResultType.COMPILED;
	}

}
