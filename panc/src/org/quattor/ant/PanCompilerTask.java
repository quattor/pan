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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
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
import org.quattor.pan.output.Formatter;
import org.quattor.pan.output.FormatterUtils;

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

	/* The pattern in the dependency files. (Two double-quoted strings). */
	private static Pattern depline = Pattern.compile("\"(.*)\"\\s+\"(.*)\"");

	/* A pattern that includes everything. For debugging includes/excludes. */
	private static Pattern allPattern = Pattern.compile(".*");

	/* List of files to actually compile and process. */
	private LinkedList<File> files = new LinkedList<File>();

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

	private boolean xmlWriteEnabled = true;

	private boolean depWriteEnabled = true;

	private int iterationLimit = 5000;

	private int callDepthLimit = 50;

	private Formatter formatter;

	private File outputDirectory = null;

	private File sessionDirectory = null;

	private boolean checkDependencies = true;

	private int nthread = 0;

	private boolean verbose = false;

	private boolean gzipOutput = false;

	private int deprecationLevel = 0;

	private boolean forceBuild = false;

	protected static String debugIdent = "    ";

	private String loggingFlags = "none";

	private File logfile = null;

	private boolean dumpAnnotations = false;

	private FileStatCache statCache = null;
	
	/**
	 * Constructor
	 */
	public PanCompilerTask() {
		// Set profile output format
		setFormatter("xmldb");

		// Create a cache for the modification times of the templates. In the
		// case where all of the files are up to date, this will save repeated
		// disk reads to determine the state of files as dependencies are in
		// common within a cluster.
		statCache = new FileStatCache();
	}

	/**
	 * Launch a build with the pan compiler using the parameters and list of
	 * file set via ant.
	 * 
	 * @throws BuildException
	 *             if the 'includeroot' parameter isn't specified when using the
	 *             'includes' parameter
	 */
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

		// Collect the options for the compilation.
		CompilerOptions options = new CompilerOptions(debugIncludePatterns,
				debugExcludePatterns, xmlWriteEnabled, depWriteEnabled,
				iterationLimit, callDepthLimit, formatter, outputDirectory,
				sessionDirectory, includeDirectories, nthread, gzipOutput,
				deprecationLevel, forceBuild, dumpAnnotations);

		// If the debugging for the task is enabled, then print out the options
		// and the arguments.
		if (debugTask) {
			System.err.println(options);
		}
		if (debugVerbose) {
			System.err.println("includeroot: " + includeroot);
			System.err.println("Profiles to process : \n");
			for (File f : files) {
				System.err.println(debugIdent + f.toString() + "\n");
			}
		}

		// Optionally check the profile dependency files and remove files which
		// are up-to-date.
		int total = files.size();
		if (outputDirectory != null && checkDependencies) {
			removeCurrentProfiles();
		}

		// Print out information on how many files will be processed.
		if (verbose) {
			System.out.println(files.size() + "/" + total
					+ " object template(s) being processed");
		}

		// Activate loggers if specified. If the logging is activated but there
		// is no log file, no output will be generated.
		CompilerLogging.activateLoggers(loggingFlags);
		CompilerLogging.setLogFile(logfile);

		// Run a compilation/build on the given object templates.
		CompilerResults results = Compiler.run(options, null, files);

		// Print out the results.
		String errors = results.formatErrors();
		if (errors != null) {
			System.err.println(errors);
		}
		if (verbose) {
			System.out.println(results.formatStats());
		}

		// Stop build if there was an error.
		if (errors != null) {
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

		// Do some parameter checking. The parameter must be an
		// existing directory.
		if (!includeroot.exists()) {
			throw new BuildException("includeroot doesn't exist: "
					+ includeroot);
		}
		if (!includeroot.isDirectory()) {
			throw new BuildException("includeroot must be a directory: "
					+ includeroot);
		}
		this.includeroot = includeroot;
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
		if (fileset != null)
			addFiles(fileset);
	}

	/**
	 * Utility method that adds all of the files in a fileset to the list of
	 * files to be processed. Duplicate files appear only once in the final
	 * list. Files not ending with '.tpl' are ignored.
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
			if (f.endsWith(".tpl")) {
				files.add(new File(basedir, f));
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
		if (debugTask != 0)
			this.debugTask = true;

		if (debugTask > 1)
			this.debugVerbose = true;
		
		statCache.setDebugLevel(this.debugTask, this.debugVerbose);
	}

	/**
	 * Add the include and exclude patterns for selectively enabling/disabling
	 * the debugging functions. (These are debug() and traceback().) An embedded
	 * element without any attributes is treated as turning on all debugging.
	 * That is, it is the same as:
	 * 
	 * <debug include=".*" />
	 * 
	 * @param debug
	 */
	public void addConfiguredDebug(Debug debug) {
		Pattern include = debug.getInclude();
		Pattern exclude = debug.getExclude();
		if (include != null) {
			debugIncludePatterns.add(include);
		}
		if (exclude != null) {
			debugExcludePatterns.add(exclude);
		}
		if (include == null && exclude == null) {
			debugIncludePatterns.add(allPattern);
		}
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
	 * disk. Valid values are "pan", "xmldb", "txt", or "dot".
	 * 
	 * @param name
	 *            name of formatter to use
	 */
	public void setFormatter(String name) {
		this.formatter = FormatterUtils.getFormatterInstance(name);
		if (formatter == null) {
			throw new BuildException("unknown output formatter: " + name);
		}
	}

	/**
	 * Set the flag to indicate whether annotations should be dumped to the
	 * standard output or not. This is a temporary option and may disappear in
	 * the future.
	 */
	public void setDumpAnnotations(boolean dumpAnnotations) {
		this.dumpAnnotations = dumpAnnotations;
	}

	/**
	 * This method will check to see if the current profiles are up-do-date. It
	 * does this by reading the dependency file and looking to see if any of the
	 * constituent templates have been modified. The number returned is the
	 * number of templates which are up-to-date.
	 * 
	 * @return number of profiles that are current
	 */
	private int removeCurrentProfiles() {

		// Loop over all of the files and create a list of those which
		// are current.
		LinkedList<File> current = new LinkedList<File>();
		for (File f : files) {

			// Map the file into the output file and the dependency
			// file.
			String name = f.getName();
			if (debugTask) {
				System.err.println("Processing " + name);
			}

			File t, d;
			if (name.endsWith(".tpl")) {
				t = new File(outputDirectory, name.substring(0,
						name.length() - 4)
						+ ".xml");
				d = new File(outputDirectory, name.substring(0,
						name.length() - 4)
						+ ".xml.dep");
			} else {
				t = new File(outputDirectory, name + ".xml");
				d = new File(outputDirectory, name + ".xml.dep");
			}

			// Only do detailed checking if both the output file and
			// the dependency file exist.
			if (!(t.exists() && d.exists())) {
				if (debugTask)
					System.err.println(debugIdent
							+ "Missing profile or dependency file for " + name);
				continue;
			}

			// The modification time of the target xml file.
			long targetTime = t.lastModified();

			// The dependency file must have been generated at the
			// same time or after the xml file.
			if (d.lastModified() < t.lastModified()) {
				System.out.println("Dependency file not current: " + d);
				continue;
			} else if (!d.canRead()) {
				System.out.println("Can't read dependency file: " + d);
				continue;
			}

			// Compare the target file with the youngest of the dependencies
			// (i.e. the largest modification time). Also check that none
			// of the files has changed position in the load path.
			boolean outOfDate = false;
			try {

				Scanner scanner = new Scanner(d);
				while (!outOfDate && scanner.hasNextLine()) {

					// Get the next line. This should never throw an exception
					// because we've check that there actually is a line above.
					String line = scanner.nextLine();

					// Extract file information from the line.
					Matcher matcher = depline.matcher(line);
					if (matcher.matches()) {

						// Create the template file name from the template name.
						// Ensure that the correct file separator is used for
						// namespaced templates.
						String templateName = matcher.group(1).replace('/',
								File.separatorChar)
								+ ".tpl";
						String templatePath = matcher.group(2);

						File dep = new File(templatePath + templateName)
								.getAbsoluteFile();

						// Check that the dependency exists and hasn't been
						// modified after the output file modification time,
						// except if it matches the ignoreDependency regexp
						if (statCache.isMissingOrModifiedAfter(dep, targetTime)) {
							outOfDate = true;

							// There is no point in continuing to check other
							// dependencies because we already know the file is
							// out of date.
							break;
						}							

						// Check that the location hasn't changed in the
						// path. If it has changed, then profile isn't
						// current.
						for (File pathdir : includeDirectories) {
							File check = new File(pathdir, templateName);
							if (statCache.exists(check)) {

								// If this isn't the correct dependency, then
								// flag that this file is out of date.
								if (!dep.equals(check)) {

									outOfDate = true;
									if (debugTask) {
										System.err.println(debugIdent
												+ "Template " + dep
												+ " moved (new=" + check + ")");
									}
								}

								// Can stop at the first one found. Either this
								// is the correct dependency or the template has
								// moved. In either case, we know the answer at
								// this point.
								break;
							}
						}

						// If the file hasn't been found at all, then do
						// nothing. The file may not have been found on
						// the load path for a couple of reasons: 1) it
						// is the object file itself which may not be on
						// the load path and 2) the internal loadpath
						// variable may be used to find the file. In the
						// second case, rely on the explicit list of
						// dependencies to pick up changes. NOTE: this
						// check isn't 100% correct. It is possible to
						// move templates around in the "internal" load
						// path; these changes will not be picked up
						// correctly.
					}
				}

			} catch (java.io.FileNotFoundException fnfe) {

				// This catch will be reached only if the Scanner finds that the
				// dependency file doesn't exist. This, however, should be
				// impossible because the existence of the dependency file was
				// checked before this block of code.
				outOfDate = true;

				if (debugTask) {
					System.err.println(debugIdent
							+ "Template dependency file (" + d
							+ ") doesn't exist");
				}

			}

			// The output file is current if it is younger than the youngest
			// dependency.
			if (!outOfDate) {
				if (debugTask) {
					System.err.println("Profile " + name + " up to date");
				}
				current.add(f);
			}
		}

		// Delete current files from the list of profiles to process.
		files.removeAll(current);

		// Send back the number which are up-to-date.
		return current.size();
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
		this.gzipOutput = gzipOutput;
	}

	/**
	 * Dependencies that must be ignored when selecting the profiles to rebuild. Value must be a regexp matching a template name
	 * relative to the load path.
	 * 
	 * MUST BE USED WITH CAUTION as it can lead to some profiles not being rebuilt. Mainly intended for use with RPM repostiory
	 * templates.
	 * 
	 * @param ignoreDependency
	 *            regexp matching a template name relative to load path
	 */
	public void setIgnoreDependency(String ignoreDependency) {
		if (debugTask) {
			System.err.println(debugIdent
					+ "Ignoring templates matching " + ignoreDependency.toString());
		}

		statCache.setIgnoreDependency(ignoreDependency);
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

	public static class Debug {

		private Pattern include;
		private Pattern exclude;

		public Debug() {
			include = null;
			exclude = null;
		}

		public void setInclude(String includePattern) {
			try {
				Pattern pattern = Pattern.compile(includePattern);
				include = pattern;
			} catch (PatternSyntaxException e) {
				throw new BuildException("invalid include pattern: "
						+ e.getMessage());
			}
		}

		public Pattern getInclude() {
			return include;
		}

		public void setExclude(String excludePattern) {
			try {
				Pattern pattern = Pattern.compile(excludePattern);
				exclude = pattern;
			} catch (PatternSyntaxException e) {
				throw new BuildException("invalid exclude pattern: "
						+ e.getMessage());
			}
		}

		public Pattern getExclude() {
			return exclude;
		}
	}

	private static class FileStatCache {

		private HashMap<File, Long> cachedTimes = new HashMap<File, Long>();

		private Pattern ignoreDependency = null;
		
		private boolean debugTask = false;
		
		private boolean debugVerbose = false;

		/**
		 * Setting this flag will print debugging information from this class.
		 * This is primarily useful if one wants to debug a build using the command
		 * line interface.
		 * 
		 * @param debugTask, debugVerbose
		 *            flag to print task debugging information or set verbosity
		 */
		public void setDebugLevel(boolean debugTask, boolean debugVerbose) {
			this.debugTask = debugTask;
			this.debugVerbose = debugVerbose;
		}

		/**
		 * Method called to actually set the dependencies to ignore, specified as a regexp
		 */
		public void setIgnoreDependency(String ignoreDependency) {
			this.ignoreDependency = Pattern.compile(ignoreDependency);
		}

		/**
		 * Method will return true if the named file exists and has a
		 * modification time after the epoch.
		 * 
		 * @param file
		 *            File to check
		 * @return true if the named file exists and has a modification time
		 *         after the epoch
		 */
		public boolean exists(File file) {
			return (getModificationTime(file) > 0L);
		}

		/**
		 * Method will return true if the named file exists and was modified
		 * after the given target time.
		 * 
		 * @param file
		 *            File to check
		 * @param targetTime
		 *            time (in milliseconds since the epoch) to use for the
		 *            comparison
		 * @return true if the named file exists and was modified after the
		 *         target time
		 */
		public boolean isModifiedAfter(File file, long targetTime) {
			long modtime = getModificationTime(file);
			return ((modtime != 0L) && (modtime > targetTime));
		}

		/**
		 * Method will return true if the named file does not exist or was
		 * modified after the given target time.
		 * 
		 * @param file
		 *            File to check
		 * @param targetTime
		 *            time (in milliseconds since the epoch) to use for the
		 *            comparison
		 * @return true if the named file doesn't exist or was modified after
		 *         the target time
		 */
		public boolean isMissingOrModifiedAfter(File file, long targetTime) {
			long modtime = getModificationTime(file);
			return ((modtime == 0L) || (modtime > targetTime));
		}

		/**
		 * Extracts the modification time for a file from the cache. If the
		 * value does not yet exist, it is inserted into the cache.
		 * 
		 * @param file
		 *            File for which modification time is requested
		 * 
		 * @return long value representing the modification time or 0L if the
		 *         file does not exist (or an IO error occurred)
		 */
		private long getModificationTime(File file) {
			Long modtime = cachedTimes.get(file);
			if (modtime == null) {
				Matcher ignoreMatcher = ignoreDependency.matcher(file.getName());
				if (ignoreMatcher.matches()) {
					// Use a non-zero fake time when the dependency must be ignored
					modtime = Long.valueOf(1);
					if (debugTask) {
						System.err.println(debugIdent
								+ "Dependency file " + file.getName()
								+ " added to ignored list");
					}
				} else {
					modtime = Long.valueOf(file.lastModified());
				}
				cachedTimes.put(file, modtime);
			}
			return modtime.longValue();
		}
	}

}

