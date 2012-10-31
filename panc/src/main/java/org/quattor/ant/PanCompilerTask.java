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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/ant/PanCompilerTask.java $
 $Id: PanCompilerTask.java 4004 2008-12-01 14:18:54Z loomis $
 */

package org.quattor.ant;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerLogging;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.CompilerResults;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.output.DepFormatter;
import org.quattor.pan.output.Formatter;
import org.quattor.pan.output.FormatterUtils;
import org.quattor.pan.repository.SourceType;

/**
 * An ant task which permits calling the pan compiler from an ant build file.
 * This task allows all of the compiler parameters to be accessed and will
 * optionally check dependency files before starting a build. See individual
 * setter methods for the parameters which can be used in the build file.
 * 
 * @author loomis
 * 
 */
public class PanCompilerTask extends Task {

	/* List of files to actually compile and process. Should be object files! */
	private LinkedList<File> objectFiles = new LinkedList<File>();

	/* The root directory for the includes. */
	private File includeroot = null;

	/* A comma- or space-separated list of file globs. */
	private List<DirSet> includes = new LinkedList<DirSet>();

	/* The list of directories to include in search path. */
	private LinkedList<File> includeDirectories = new LinkedList<File>();

	private boolean debugTask = false;

	/* Produce very verbose output */
	private boolean debugVerbose = false;

	private List<Pattern> debugIncludePatterns = new LinkedList<Pattern>();

	private List<Pattern> debugExcludePatterns = new LinkedList<Pattern>();

	private Pattern ignoreDependencyPattern = null;

	private boolean xmlWriteEnabled = true;

	private boolean depWriteEnabled = true;

	private int iterationLimit = 5000;

	private int callDepthLimit = 50;

	private Formatter formatter;
	
	private Set<Formatter> formatters;

	private File outputDirectory = null;

	private File sessionDirectory = null;

	private boolean checkDependencies = true;

	private int nthread = 0;

	private boolean verbose = false;

	private int deprecationLevel = 0;

	private boolean failOnWarn = false;

	private boolean forceBuild = false;

	final protected static String debugIndent = "    ";

	private String loggingFlags = "none";

	private File logfile = null;

	private File annotationDirectory = null;

	private File annotationBaseDirectory = null;

	private int batchSize = 0;

	private String rootElement = null;

	public PanCompilerTask() {
		setFormatter("pan");
	}

	@Override
	public void execute() throws BuildException {

		// If some include globs were specified, then check that the
		// includeroot was specified. Add the necessary paths.
		if (includes.size() > 0) {
			if (includeroot == null) {
				throw new BuildException(
						"includeroot must be specified to use 'includes' parameter");
			}

			Path antpath = new Path(getProject());

			for (DirSet dirset : includes) {
				dirset.setDir(includeroot);
				antpath.addDirset(dirset);
			}
			addPaths(antpath);
		}

		CompilerOptions.DeprecationWarnings deprecationWarnings = CompilerOptions
				.getDeprecationWarnings(deprecationLevel, failOnWarn);

		formatters.add(formatter);

		if (!xmlWriteEnabled) {
			formatters.clear();
		}

		if (depWriteEnabled) {
			formatters.add(DepFormatter.getInstance());
		}

		// Collect the options for the compilation.
		CompilerOptions options = null;
		try {
			options = new CompilerOptions(debugIncludePatterns,
					debugExcludePatterns, iterationLimit, callDepthLimit,
					formatters, outputDirectory, sessionDirectory,
					includeDirectories, nthread, deprecationWarnings,
					forceBuild, annotationDirectory, annotationBaseDirectory,
					rootElement);
		} catch (SyntaxException e) {
			throw new BuildException("invalid root element: " + e.getMessage());
		}

		// If the debugging for the task is enabled, then print out the options
		// and the arguments.
		if (debugTask) {
			System.err.println(options);
		}
		if (debugVerbose) {
			System.err.println("includeroot: " + includeroot);
			System.err.println("Profiles to process : \n");
			for (File objectFile : objectFiles) {
				System.err.println(debugIndent + objectFile + "\n");
			}
		}

		// Determine what object files are outdated. Assume that all are, unless
		// the check is done.
		List<File> outdatedFiles = objectFiles;
		if (outputDirectory != null && checkDependencies) {

			DependencyChecker checker = new DependencyChecker(
					includeDirectories, outputDirectory, formatters,
					ignoreDependencyPattern);

			outdatedFiles = checker.filterForOutdatedFiles(objectFiles);

			if (debugVerbose) {
				System.err.println("Outdated profiles: \n");
				for (File objectFile : outdatedFiles) {
					System.err.println(debugIndent + objectFile + "\n");
				}
			}

		}

		// Print out information on how many files will be processed.
		if (verbose) {
			System.out.println(outdatedFiles.size() + "/" + objectFiles.size()
					+ " object template(s) being processed");
		}

		// Activate loggers if specified. If the logging is activated but there
		// is no log file, no output will be generated.
		CompilerLogging.activateLoggers(loggingFlags);
		CompilerLogging.setLogFile(logfile);

		// Batch the files to process, if requested.
		List<List<File>> batches = batchOutdatedFiles(outdatedFiles);

		boolean hadError = false;
		for (List<File> batch : batches) {

			CompilerResults results = Compiler.run(options, null, batch);

			boolean batchHadError = results.print(verbose);

			if (batchHadError) {
				hadError = true;
			}

		}

		// Stop build if there was an error.
		if (hadError) {
			throw new BuildException("Compilation failed; see messages.");
		}
	}

	/**
	 * Set the directory to use for the include globs. This is required only if
	 * the includes parameter is set.
	 * 
	 * @param includeroot
	 *            File giving the root directory for the include globs
	 */
	public void setIncludeRoot(File includeroot) {

		this.includeroot = includeroot;

		if (!includeroot.exists()) {
			throw new BuildException("includeroot doesn't exist: "
					+ includeroot);
		}
		if (!includeroot.isDirectory()) {
			throw new BuildException("includeroot must be a directory: "
					+ includeroot);
		}
	}

	/**
	 * Set the include globs to use for the pan compiler loadpath.
	 * 
	 * @param includes
	 *            String of comma- or space-separated file globs
	 */
	public void setIncludes(String includes) {

		// Split the string into separate file globs.
		String[] globs = includes.split("[\\s,]+");

		// Loop over these globs and create dirsets from them.
		// Do not set the root directory until the task is
		// executed.
		for (String glob : globs) {
			DirSet dirset = new DirSet();
			dirset.setIncludes(glob);
			this.includes.add(dirset);
		}
	}

	/**
	 * Support nested path elements. This is called by ant only after all of the
	 * children of the path have been processed. These are the include
	 * directories to find non-object templates. Non-directory elements will be
	 * silently ignored.
	 * 
	 * @param path
	 *            a configured Path
	 */
	public void addConfiguredPath(Path path) {
		if (path != null)
			addPaths(path);
	}

	/**
	 * Collect all of the directories listed within enclosed path tags. Order of
	 * the path elements is preserved. Duplicates are included where first
	 * specified.
	 * 
	 * @param p
	 *            Path containing directories to include in compilation
	 */
	private void addPaths(Path p) {

		for (String d : p.list()) {
			File dir = new File(d);
			if (dir.exists() && dir.isDirectory()) {
				if (!includeDirectories.contains(dir))
					includeDirectories.add(dir);
			}
		}
	}

	/**
	 * Support nested fileset elements. This is called by ant only after all of
	 * the children of the fileset have been processed. Collect all of the
	 * selected files from the fileset.
	 * 
	 * @param fileset
	 *            a configured FileSet
	 */
	public void addConfiguredFileSet(FileSet fileset) {
		addFiles(fileset);
	}

	/**
	 * Utility method that adds all of the files in a fileset to the list of
	 * files to be processed. Duplicate files appear only once in the final
	 * list. Files not ending with a valid source file extension are ignored.
	 * 
	 * @param fs
	 *            FileSet from which to get the file names
	 */
	private void addFiles(FileSet fs) {

		// Get the files included in the fileset.
		DirectoryScanner ds = fs.getDirectoryScanner(getProject());

		// The base directory for all files.
		File basedir = ds.getBasedir();

		// Loop over each file creating a File object.
		for (String f : ds.getIncludedFiles()) {
			if (SourceType.hasSourceFileExtension(f)) {
				objectFiles.add(new File(basedir, f));
			}
		}
	}

	/**
	 * The pan compiler can limit the call depth to avoid infinite recursion.
	 * This method sets a value for this limit. The default value is 50;
	 * non-positive numbers indicate no limit.
	 * 
	 * @param callDepthLimit
	 */
	public void setCallDepthLimit(int callDepthLimit) {
		this.callDepthLimit = callDepthLimit;
	}

	/**
	 * Setting this flag will print debugging information from the task itself.
	 * This is primarily useful if one wants to debug a build using the command
	 * line interface.
	 * 
	 * @param debugTask
	 *            flag to print task debugging information
	 */
	public void setDebugTask(int debugTask) {
		this.debugTask = (debugTask != 0);
		this.debugVerbose = (debugTask > 1);
	}

	/**
	 * Add the include and exclude patterns for selectively enabling/disabling
	 * the debugging functions (debug() and traceback()). An embedded element
	 * without any attributes is treated as turning on all debugging. That is,
	 * it is the same as:
	 * 
	 * <debug include=".+" exclude="^$" />
	 * 
	 * @param debugPatterns
	 *            configured instance with desired debug patterns
	 */
	public void addConfiguredDebug(DebugPatterns debugPatterns) {
		debugIncludePatterns.add(debugPatterns.getInclude());
		debugExcludePatterns.add(debugPatterns.getExclude());
	}

	/**
	 * Setting this flag will cause the compiler to write dependency files for
	 * the processed object templates.
	 * 
	 * @param depWriteEnabled
	 *            flag to generate dependency files
	 */
	public void setDepWriteEnabled(boolean depWriteEnabled) {
		this.depWriteEnabled = depWriteEnabled;
	}

	/**
	 * The pan compiler allows an iteration limit to be set to avoid infinite
	 * loops. The default value is 5000 iterations; non-positive values indicate
	 * that no limit should be used.
	 * 
	 * @param iterationLimit
	 *            maximum number of permitted iterations
	 */
	public void setIterationLimit(int iterationLimit) {
		this.iterationLimit = iterationLimit;
	}

	/**
	 * Set the output directory for generated machine profiles and dependency
	 * files.
	 * 
	 * @param outputDirectory
	 *            directory for produced files
	 */
	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	/**
	 * Set the 'session' directory to be used for the compilation. The session
	 * directory allows a parallel tree structure to be consulted before loading
	 * templates. This is useful to isolate changes from a pristine tree.
	 * 
	 * @param sessionDirectory
	 *            session directory to use for the build
	 */
	public void setSessionDirectory(File sessionDirectory) {
		this.sessionDirectory = sessionDirectory;
	}

	/**
	 * Setting this flag will cause the pan compiler to write machine templates
	 * to disk. The machine profiles are usually in an XML format; however, any
	 * defined formatter can be used.
	 * 
	 * @param xmlWriteEnabled
	 */
	public void setXmlWriteEnabled(boolean xmlWriteEnabled) {
		this.xmlWriteEnabled = xmlWriteEnabled;
	}

	/**
	 * This any task can check machine profile dependencies to avoid processing
	 * machine profiles which are already up-to-date. Setting this flag allows
	 * the dependency checking to minimize the number of files which are built.
	 * 
	 * @param checkDependencies
	 */
	public void setCheckDependencies(boolean checkDependencies) {
		this.checkDependencies = checkDependencies;
	}

	/**
	 * Define which formatter will be used to write the machine profiles to
	 * disk. Valid values are "pan", "json", "txt", or "dot".
	 * 
	 * @param name
	 *            name of formatter to use
	 */
	public void setFormatter(String name) {
		formatter = FormatterUtils.getFormatterInstance(name);
		if (formatter == null) {
			throw new BuildException("unknown output formatter: " + name);
		}
		formatters = new HashSet<Formatter>();
		formatters.add(formatter);
	}

	/**
	 * Defines the formatters used to generate the output files.
	 * 
	 * @param fmts
	 *            comma-separated list of formatters to use
	 */
	public void setFormatters(String fmts) {
		formatters = CompilerOptions.getFormatters(fmts);
	}

	/**
	 * Set the directory in which the produced annotation files should be saved.
	 * 
	 * @param annotationDirectory
	 */
	public void setAnnotationDirectory(File annotationDirectory) {
		this.annotationDirectory = annotationDirectory;
	}

	/**
	 * Set the directory base directory to use for relative paths for annotation
	 * output files.
	 * 
	 * @param annotationBaseDirectory
	 */
	public void setAnnotationBaseDirectory(File annotationBaseDirectory) {
		this.annotationBaseDirectory = annotationBaseDirectory;
	}

	/**
	 * Return the number of threads used to compile the profiles. Actually this
	 * is the number of threads to allow in each of three task queues.
	 * 
	 * @return number of threads in each task queue
	 */
	public int getNthread() {
		return nthread;
	}

	/**
	 * Set the number of threads to use in each of three separate task queues. A
	 * non-positive number will use the default value, which is the number of
	 * CPUs (or cores) available on the system.
	 * 
	 * @param nthread
	 *            number of threads per task queue
	 */
	public void setNthread(int nthread) {
		this.nthread = nthread;
	}

	/**
	 * Flag to indicate that extra information should be written to the standard
	 * output. This gives the total number of files which will be processed and
	 * statistics coming from the compilation.
	 * 
	 * @param verbose
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Indicate whether the produced machine profiles (if any) should be gzipped
	 * or not. The default is not to gzip output files.
	 * 
	 * @param gzipOutput
	 *            flag indicating whether to gzip output
	 */
	public void setGzipOutput(boolean gzipOutput) {
		// no-op: use gzip formatter names instead
	}

	/**
	 * Dependencies that must be ignored when selecting the profiles to rebuild.
	 * Value must be a regular expression matching a (namespaced) template name.
	 * 
	 * NOTE: Use of this option may cause incomplete builds. Use this option
	 * with extreme caution.
	 * 
	 * @param ignoreDependencyPattern
	 *            regular expression used to match namespaced template names to
	 *            ignore
	 */
	public void setIgnoreDependencyPattern(String ignoreDependencyPattern) {
		try {
			Pattern pattern = Pattern.compile(ignoreDependencyPattern);
			this.ignoreDependencyPattern = pattern;
		} catch (PatternSyntaxException e) {
			throw new BuildException("invalid ignore dependency pattern: "
					+ e.getMessage());
		}
	}

	/**
	 * Level at which deprecation warnings are issued. If less than zero, then
	 * none are printed. If zero, warnings are issued for things that will
	 * change in next release. If greater than zero, then other future changes
	 * will be flagged.
	 * 
	 * @param deprecationLevel
	 *            level at which to give deprecation warnings
	 */
	public void setDeprecationLevel(int deprecationLevel) {
		this.deprecationLevel = deprecationLevel;
	}

	/**
	 * Flag to indicate whether or not warnings should be treated as errors.
	 * 
	 * @param failOnWarn
	 *            setting the value true will cause compilations to fail on
	 *            warnings
	 */
	public void setFailOnWarn(boolean failOnWarn) {
		this.failOnWarn = failOnWarn;
	}

	/**
	 * Setting this option will force the compiler to build object templates
	 * even if the writing of the XML files and dependency files is turned off.
	 * 
	 * @param forceBuild
	 */
	public void setForceBuild(boolean forceBuild) {
		this.forceBuild = forceBuild;
	}

	/**
	 * Enable the given types of logging. Note that NONE will take precedence
	 * over active logging flags and turn all logging off.
	 * 
	 * @param loggingFlags
	 *            a comma-separated list of logging types to enable
	 */
	public void setLogging(String loggingFlags) {
		this.loggingFlags = loggingFlags;
	}

	/**
	 * Set the log file to use for logging.
	 * 
	 * @param logfile
	 *            file to use for logging
	 */
	public void setLogfile(File logfile) {
		this.logfile = logfile;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = (batchSize > 0) ? batchSize : 0;
	}

	public void setRootElement(String rootElement) {
		this.rootElement = rootElement;
	}

	/**
	 * This utility method will group the file into a set of equal sized batches
	 * (except for possibly the last batch).
	 * 
	 * @param outdatedFiles
	 * 
	 * @return list of batched files
	 */
	private List<List<File>> batchOutdatedFiles(List<File> outdatedFiles) {

		List<List<File>> batches = new LinkedList<List<File>>();

		int total = outdatedFiles.size();

		int myBatchSize = (batchSize <= 0) ? outdatedFiles.size() : batchSize;

		for (int start = 0; start < total; start += myBatchSize) {
			int end = Math.min(start + myBatchSize, total);
			batches.add(outdatedFiles.subList(start, end));
		}

		return batches;
	}

}
