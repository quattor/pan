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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/CompilerOptions.java $
 $Id: CompilerOptions.java 3937 2008-11-22 10:31:49Z loomis $
 */

package org.quattor.pan;

import static org.quattor.pan.utils.MessageUtils.MSG_CANNOT_LOCATE_OBJECT_TEMPLATE;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.quattor.pan.exceptions.ConfigurationException;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.output.Formatter;
import org.quattor.pan.repository.ParameterList;
import org.quattor.pan.repository.SourceFile;
import org.quattor.pan.repository.SourceRepository;
import org.quattor.pan.repository.SourceRepositoryFactory;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Encapsulates the global options for the pan compiler. An instance of this
 * class must be passed to the <code>Compiler</code> itself. Instances of this
 * class are immutable and thread-safe.
 * 
 * @author loomis
 * 
 */
public class CompilerOptions {

	/**
	 * Flag to activate the writing of machine profiles to disk. Typically these
	 * are XML files, but any <code>Formatter</code> can be used.
	 */
	public final boolean xmlWriteEnabled;

	/**
	 * Indicates whether or not the dependency files for produced machine
	 * profiles should be written.
	 */
	public final boolean depWriteEnabled;

	/**
	 * Force the build to be done even if no output files are to be written.
	 */
	public final boolean forceBuild;

	/**
	 * The iteration limit during the compilation to avoid infinite loops.
	 */
	public final int iterationLimit;

	/**
	 * The call depth limit which is used to prevent infinite recursion in the
	 * compiler.
	 */
	public final int callDepthLimit;

	/**
	 * The <code>Formatter</code> that will be used to format the machine
	 * profiles.
	 */
	public final Formatter formatter;

	/**
	 * The directory that will contain the produced machine profiles and
	 * dependency files (if any).
	 */
	public final File outputDirectory;

	/**
	 * The number of active threads per task queue to allow.
	 */
	public final int activeThreadsPerQueue;

	/**
	 * Indicate whether the produced machine profile should be gzipped.
	 */
	public final boolean gzipOutput;

	/**
	 * Define the deprecation level for compilation. Less than zero turns this
	 * off, 0 gives warnings for things that will change in next release, and 1
	 * gives warnings for other future changes.
	 */
	public final int deprecationLevel;

	public final SourceRepository sourceRepository;

	/**
	 * Patterns that are matched against a template name to determine if debug()
	 * calls are turned on or off. Template names matching one of the patterns
	 * in the include set will have the debugging turned on UNLESS it matches a
	 * pattern in the exclude set.
	 */
	private final List<Pattern> debugIncludePatterns;
	private final List<Pattern> debugExcludePatterns;

	/**
	 * Temporary option to allow dumping of annotation contents.
	 */
	public final boolean dumpAnnotations;

	/**
	 * Directory that will contain the annotation output files.
	 */
	public final File annotationDirectory;

	/**
	 * Construct a CompilerOptions instance to drive a Compiler run. Instances
	 * of this class are immutable.
	 * 
	 * @param debugIncludePatterns
	 *            patterns to use to turn on debugging for matching templates
	 * @param debugExcludePatterns
	 *            patterns to use to turn off debugging for matching templates
	 * @param xmlWriteEnabled
	 *            write machine configuration files (usually XML files)
	 * @param depWriteEnabled
	 *            write dependency information
	 * @param iterationLimit
	 *            maximum number of iterations (<=0 unlimited)
	 * @param callDepthLimit
	 *            maximum call depth (<=0 unlimited)
	 * @param formatter
	 *            format for machine configuration files (cannot be null if
	 *            writeXmlEnabled is true)
	 * @param outputDirectory
	 *            output directory for machine configuration and dependency
	 *            files (cannot be null if either writeXmlEnable or
	 *            writeDepEnabled is true)
	 * @param sessionDirectory
	 *            session directory
	 * @param includeDirectories
	 *            list of directories to check for template files; directories
	 *            must exist and be absolute
	 * @param nthread
	 *            number of threads to use (<=0 uses default value)
	 * @param gzipOutput
	 *            gzip produced machine profiles
	 * @param deprecationLevel
	 *            level for deprecation warnings (<0 off, =0 next release, >0
	 *            future releases)
	 * @param forceBuild
	 *            force build even if no output files are generated if true
	 * @param dumpAnnotations
	 *            flag to indicate if annotations should be dumped to the
	 *            standard output
	 * @param annotationDirectory
	 *            directory that will contain annotation output files
	 */
	public CompilerOptions(List<Pattern> debugIncludePatterns,
			List<Pattern> debugExcludePatterns, boolean xmlWriteEnabled,
			boolean depWriteEnabled, int iterationLimit, int callDepthLimit,
			Formatter formatter, File outputDirectory, File sessionDirectory,
			List<File> includeDirectories, int nthread, boolean gzipOutput,
			int deprecationLevel, boolean forceBuild, boolean dumpAnnotations,
			File annotationDirectory) {

		// Check that the iteration and call depth limits are sensible. If
		// negative or zero set these effectively to infinity.
		if (iterationLimit <= 0) {
			iterationLimit = Integer.MAX_VALUE;
		}
		if (callDepthLimit <= 0) {
			callDepthLimit = Integer.MAX_VALUE;
		}

		// Check that the output and session directories are sensible if they
		// aren't null.
		if (outputDirectory != null) {
			checkDirectory(outputDirectory, "output");
		}
		if (sessionDirectory != null) {
			checkDirectory(sessionDirectory, "session");
		}

		// Check all of the include directories.
		for (File d : includeDirectories) {
			if (d != null) {
				checkDirectory(d, "include");
			} else {
				throw new IllegalArgumentException(
						"include directory may not be null");
			}
		}

		// If we want the XML or dependency files to be written, the output
		// directory must be specified.
		if ((outputDirectory == null) && (xmlWriteEnabled || depWriteEnabled)) {
			throw new IllegalArgumentException(
					"outputDirectory must be specified if the XML or dependency files are to be written");
		}

		// Must have a formatter if the XML output is desired.
		if (formatter == null && xmlWriteEnabled) {
			throw new IllegalArgumentException(
					"formatter must be specified if XML file is to be written");
		}

		// Everything's OK. Copy the values into this instance.
		this.xmlWriteEnabled = xmlWriteEnabled;
		this.depWriteEnabled = depWriteEnabled;
		this.iterationLimit = iterationLimit;
		this.callDepthLimit = callDepthLimit;
		this.formatter = formatter;
		this.outputDirectory = outputDirectory;
		this.activeThreadsPerQueue = nthread;
		this.gzipOutput = gzipOutput;
		this.deprecationLevel = deprecationLevel;
		this.forceBuild = forceBuild;

		// Setup the debug patterns, ensuring that the debug pattern lists are
		// not null.
		if (debugIncludePatterns == null) {
			this.debugIncludePatterns = new LinkedList<Pattern>();
		} else {
			this.debugIncludePatterns = debugIncludePatterns;
		}
		if (debugExcludePatterns == null) {
			this.debugExcludePatterns = new LinkedList<Pattern>();
		} else {
			this.debugExcludePatterns = debugExcludePatterns;
		}

		ParameterList parameters = new ParameterList();
		if (sessionDirectory != null) {
			parameters.append("sessionDirectory", sessionDirectory.toString());
		}
		for (File f : includeDirectories) {
			parameters.append("includeDirectory", f.toString());
		}

		SourceRepository value = null;
		try {
			value = SourceRepositoryFactory.create(parameters);
		} catch (ConfigurationException ce) {
			throw new RuntimeException(ce.getMessage());
			// TODO: Add correct behavior.
		}
		sourceRepository = value;

		this.dumpAnnotations = dumpAnnotations;

		this.annotationDirectory = annotationDirectory;
		if (annotationDirectory != null) {
			checkDirectory(annotationDirectory, "annotation");
		}

	}

	/**
	 * Create a CompilerOptions object that is appropriate for just doing a
	 * syntax check.
	 * 
	 * @param deprecationLevel
	 *            set the deprecation level, the higher the level the fewer
	 *            deprecation warnings are produced; 0 produces all warnings
	 * @return
	 */
	public static CompilerOptions createCheckSyntaxOptions(int deprecationLevel) {

		List<Pattern> debugIncludePatterns = new LinkedList<Pattern>();
		List<Pattern> debugExcludePatterns = new LinkedList<Pattern>();
		boolean xmlWriteEnabled = false;
		boolean depWriteEnabled = false;
		int iterationLimit = 5000;
		int callDepthLimit = 50;
		Formatter formatter = null;
		File outputDirectory = null;
		File sessionDirectory = null;
		int nthread = 0;
		boolean gzipOutput = false;
		boolean forceBuild = false;
		boolean dumpAnnotations = false;
		File annotationDirectory = null;
		LinkedList<File> includeDirectories = new LinkedList<File>();

		return new CompilerOptions(debugIncludePatterns, debugExcludePatterns,
				xmlWriteEnabled, depWriteEnabled, iterationLimit,
				callDepthLimit, formatter, outputDirectory, sessionDirectory,
				includeDirectories, nthread, gzipOutput, deprecationLevel,
				forceBuild, dumpAnnotations, annotationDirectory);

	}

	/**
	 * A private utility function to verify that the directory is really a
	 * directory, exists, and absolute.
	 * 
	 * @param dirs
	 *            directory to check
	 * @param dtype
	 *            name to use in case of errors
	 */
	private void checkDirectory(File d, String dtype) {

		if (!d.isAbsolute()) {
			throw new IllegalArgumentException(dtype
					+ " directory must be an absolute path");
		}
		if (!d.exists()) {
			throw new IllegalArgumentException(dtype
					+ " directory does not exist");
		}
		if (!d.isDirectory()) {
			throw new IllegalArgumentException(dtype
					+ " directory value is not a directory");
		}

	}

	/**
	 * Resolve a list of object template names and template Files to a set of
	 * files based on this instance's include directories and session directory.
	 * 
	 * @param objectNames
	 *            object template names to lookup
	 * @param tplFiles
	 *            template Files to process
	 * 
	 * @return unmodifiable set of the resolved file names
	 */
	public Set<File> resolveFileList(List<String> objectNames,
			Collection<File> tplFiles) {

		// First just copy the named templates.
		Set<File> filesToProcess = new TreeSet<File>();
		if (tplFiles != null) {
			filesToProcess.addAll(tplFiles);
		}

		// Now loop over all of the object template names, lookup the files, and
		// add them to the set of files to process.
		if (objectNames != null) {
			for (String oname : objectNames) {
				SourceFile source = sourceRepository.retrievePanSource(oname);
				if (!source.isAbsent()) {
					filesToProcess.add(source.getPath());
				} else {
					throw EvaluationException.create((SourceRange) null,
							(Context) null, MSG_CANNOT_LOCATE_OBJECT_TEMPLATE,
							oname);
				}
			}
		}

		return Collections.unmodifiableSet(filesToProcess);
	}

	/**
	 * A utility function that checks a given template name against the list of
	 * debug include and exclude patterns.
	 * 
	 * @param tplName
	 *            name of the template to check
	 * 
	 * @return flag indicating whether debugging should be activated or not
	 */
	public boolean checkDebugEnabled(String tplName) {

		// Check first the exclude patterns. Any matching pattern in the exclude
		// list means that the debugging is disabled for the given template.
		for (Pattern p : debugExcludePatterns) {
			if (p.matcher(tplName).matches()) {
				return false;
			}
		}

		// Now check the include patterns. Any matching pattern here means that
		// the debugging for this template is enabled.
		for (Pattern p : debugIncludePatterns) {
			if (p.matcher(tplName).matches()) {
				return true;
			}
		}

		// If we get here, then the template didn't match anything. By default,
		// the debugging is turned off.
		return false;
	}

	/**
	 * A verbose representation of all of the options in this instance.
	 * 
	 * @return String representation of options
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("debug include patterns: ");
		for (Pattern p : debugIncludePatterns) {
			sb.append(p.toString());
			sb.append("\n");
		}
		sb.append("\n");

		sb.append("debug exclude patterns: ");
		for (Pattern p : debugExcludePatterns) {
			sb.append(p.toString());
			sb.append("\n");
		}
		sb.append("\n");

		sb.append("XML write enabled: ");
		sb.append(xmlWriteEnabled);
		sb.append("\n");

		sb.append("dependency write enabled: ");
		sb.append(depWriteEnabled);
		sb.append("\n");

		sb.append("iteration limit: ");
		sb.append(iterationLimit);
		sb.append("\n");

		sb.append("call depth limit: ");
		sb.append(callDepthLimit);
		sb.append("\n");

		sb.append("gzip output: ");
		sb.append(gzipOutput);
		sb.append("\n");

		sb.append("output directory: ");
		sb.append(outputDirectory);
		sb.append("\n");

		if (formatter != null) {
			sb.append("formatter: ");
			sb.append(formatter.getClass().toString());
			sb.append("\n");
		} else {
			sb.append("formatter: null\n");
		}

		sb.append("source repository: ");
		sb.append(sourceRepository.toString());
		sb.append("\n");

		return sb.toString();
	}

}
