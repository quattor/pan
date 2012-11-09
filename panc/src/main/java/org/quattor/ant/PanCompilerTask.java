/*
 Copyright (c) 2006-2012 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
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
import org.quattor.pan.output.PanFormatter;
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

    private Boolean xmlWriteEnabled = null;

    private Boolean depWriteEnabled = null;

    private Formatter formatter;

    private boolean checkDependencies = true;

    private int nthread = 0;

    private boolean verbose = false;

    private int deprecationLevel = 0;

    private boolean failOnWarn = false;

    final protected static String debugIndent = "    ";

    private String logging = "none";

    private File logFile = null;

    private int batchSize = 0;

    private int maxIteration = 10000;

    private int maxRecursion = 50;

    private File outputDir = null;

    private Set<Formatter> formatters;

    private String initialData;

    private CompilerOptions.DeprecationWarnings deprecationWarnings = null;

    public PanCompilerTask() {
        setFormats("pan,dep");
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

        // This can be dropped when the old style parameters are removed.
        if (deprecationWarnings == null) {
            deprecationWarnings = CompilerOptions.getDeprecationWarnings(
                    deprecationLevel, failOnWarn);
        }

        // This can be dropped when the old style parameters are removed.
        setOldStyleOutputFormats();

        // Collect the options for the compilation.
        CompilerOptions options = null;
        try {
            options = new CompilerOptions(debugIncludePatterns,
                    debugExcludePatterns, maxIteration, maxRecursion,
                    formatters, outputDir, null, includeDirectories, nthread,
                    deprecationWarnings, false, null, null, initialData);
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
        if (outputDir != null && checkDependencies) {

            DependencyChecker checker = new DependencyChecker(
                    includeDirectories, outputDir, formatters,
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
        CompilerLogging.activateLoggers(logging);
        CompilerLogging.setLogFile(logFile);

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

    // If any of the old style output parameters are used, then set the various
    // internal parameters correctly.
    private void setOldStyleOutputFormats() {
        if (xmlWriteEnabled != null || depWriteEnabled != null
                || formatter != null) {

            if (xmlWriteEnabled) {
                if (formatter != null) {
                    formatters.add(formatter);
                } else {
                    formatters.add(PanFormatter.getInstance());
                }
            } else {
                formatters.clear();
            }

            if (depWriteEnabled) {
                formatters.add(DepFormatter.getInstance());
            }
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
     * Setting this flag will cause the compiler to turn on debugging for all
     * namespaces. This parameter should not be mixed with the options for
     * setting the debug namespaces explicitly.
     * 
     * @param debug
     *            flag to turn on/off debugging for all namespaces
     */
    public void setDebug(int debug) {
        debugIncludePatterns.clear();
        debugExcludePatterns.clear();
        debugIncludePatterns.add(Pattern.compile(".+"));
        debugExcludePatterns.add(Pattern.compile("^$"));
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
     * Set the regular expression used to include pan namespaces for debugging.
     * 
     * @param pattern
     */
    public void setDebugNsInclude(String pattern) {
        debugIncludePatterns.add(Pattern.compile(pattern));
    }

    /**
     * Set the regular expression used to exclude pan namespaces for debugging.
     * 
     * @param pattern
     */
    public void setDebugNsExclude(String pattern) {
        debugExcludePatterns.add(Pattern.compile(pattern));
    }

    /**
     * Provides an nlist() with a data structure that will be used to initialize
     * all generated profiles.
     * 
     * @param initialData
     */
    public void setInitialData(String initialData) {
        this.initialData = initialData;
    }

    /**
     * Set the output directory for generated machine profiles and dependency
     * files.
     * 
     * @param outputDir
     *            directory for produced files
     */
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Defines the formatters used to generate the output files. The default
     * value is "pan,dep".
     * 
     * @param fmts
     *            comma-separated list of formatters to use
     */
    public void setFormats(String fmts) {
        formatters = CompilerOptions.getFormatters(fmts);
    }

    /**
     * The pan compiler allows an iteration limit to be set to avoid infinite
     * loops. Non-positive values indicate that no limit should be used.
     * 
     * @param maxIteration
     *            maximum number of permitted iterations
     */
    public void setMaxIteration(int maxIteration) {
        this.maxIteration = maxIteration;
    }

    /**
     * Sets the default maximum number of recursions.
     * 
     * @param maxRecursion
     */
    public void setMaxRecursion(int maxRecursion) {
        this.maxRecursion = maxRecursion;
    }

    /**
     * Enable the given types of logging. Note that NONE will take precedence
     * over active logging flags and turn all logging off.
     * 
     * @param loggingFlags
     *            a comma-separated list of logging types to enable
     */
    public void setLogging(String loggingFlags) {
        this.logging = loggingFlags;
    }

    /**
     * Set the log file to use for logging.
     * 
     * @param logFile
     *            file to use for logging
     */
    public void setLogfile(File logFile) {
        this.logFile = logFile;
    }

    /**
     * Determines whether deprecation warnings are emitted and if so, whether to
     * treat them as fatal errors.
     * 
     * @param warnings
     */
    public void setWarnings(String warnings) {
        try {
            deprecationWarnings = CompilerOptions.DeprecationWarnings
                    .fromString(warnings);
        } catch (IllegalArgumentException e) {
            throw new BuildException("invalid value for warnings: " + warnings);
        }
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

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = (batchSize > 0) ? batchSize : 0;
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

    /**
     * Sets the maximum number of recursions. Alias for setMaxRecursion.
     * 
     * @param callDepthLimit
     * @deprecated
     */
    public void setCallDepthLimit(int callDepthLimit) {
        log("parameter 'callDepthLimit' is deprecated; used 'maxRecursion' instead");
        this.maxRecursion = callDepthLimit;
    }

    /**
     * Deprecated alias for setMaxIteration.
     * 
     * @param iterationLimit
     *            maximum number of permitted iterations
     * @deprecated
     */
    public void setIterationLimit(int iterationLimit) {
        log("parameter 'iterationLimit' is deprecated; used 'maxIteration' instead");
        this.maxIteration = iterationLimit;
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
     * @deprecated
     */
    public void addConfiguredDebug(DebugPatterns debugPatterns) {
        log("'debug' element is deprecated; use 'debugNsInclude' and 'debugNsExclude' attributes instead");
        debugIncludePatterns.add(debugPatterns.getInclude());
        debugExcludePatterns.add(debugPatterns.getExclude());
    }

    /**
     * Deprecated alias for setOutputDir.
     * 
     * @param outputDirectory
     *            directory for produced files
     * @deprecated
     */
    public void setOutputDirectory(File outputDirectory) {
        log("parameter 'outputDirectory' is deprecated; use 'outputDir' instead");
        this.outputDir = outputDirectory;
    }

    /**
     * Session directories are no longer supported. This method does nothing.
     * 
     * @param sessionDirectory
     *            session directory to use for the build
     * @deprecated
     */
    public void setSessionDirectory(File sessionDirectory) {
        log("parameter 'sessionDirectory' is deprecated; this functionality has been removed");
    }

    /**
     * Setting this flag will cause the pan compiler to write machine templates
     * to disk. The machine profiles are usually in an XML format; however, any
     * defined formatter can be used.
     * 
     * Deprecated: Use formats option instead.
     * 
     * @param xmlWriteEnabled
     * @deprecated
     */
    public void setXmlWriteEnabled(boolean xmlWriteEnabled) {
        log("parameter 'xmlWriteEnabled' is deprecated; use 'formats' option instead");
        this.xmlWriteEnabled = xmlWriteEnabled;
    }

    /**
     * Setting this flag will cause the compiler to write dependency files for
     * the processed object templates.
     * 
     * @param depWriteEnabled
     *            flag to generate dependency files
     * @deprecated
     */
    public void setDepWriteEnabled(boolean depWriteEnabled) {
        log("parameter 'depWriteEnabled' is deprecated; use 'formats' option instead");
        this.depWriteEnabled = depWriteEnabled;
    }

    /**
     * Sets the formatter to use. This is deprecated; use the setFormatters
     * method instead.
     * 
     * @param name
     *            name of formatter to use
     * @deprecated
     */
    public void setFormatter(String name) {
        log("parameter 'formatter' is deprecated; use 'formats' option instead");
        formatter = FormatterUtils.getFormatterInstance(name);
        if (formatter == null) {
            throw new BuildException("unknown output formatter: " + name);
        }
        formatters = new HashSet<Formatter>();
        formatters.add(formatter);
    }

    /**
     * Set the directory in which the produced annotation files should be saved.
     * 
     * @param annotationDirectory
     * @deprecated
     */
    public void setAnnotationDirectory(File annotationDirectory) {
        log("parameter 'annotationDirectory' is deprecated; use separate annotation task");
    }

    /**
     * Set the directory base directory to use for relative paths for annotation
     * output files.
     * 
     * @param annotationBaseDirectory
     * @deprecated
     */
    public void setAnnotationBaseDirectory(File annotationBaseDirectory) {
        log("parameter 'annotationBaseDirectory' is deprecated; use separate annotation task");
    }

    /**
     * Return the number of threads used to compile the profiles. Actually this
     * is the number of threads to allow in each of three task queues.
     * 
     * @return number of threads in each task queue
     * @deprecated
     */
    public int getNthread() {
        log("function getNthread is deprecated");
        return nthread;
    }

    /**
     * Set the number of threads to use in each of three separate task queues. A
     * non-positive number will use the default value, which is the number of
     * CPUs (or cores) available on the system.
     * 
     * @param nthread
     *            number of threads per task queue
     * @deprecated
     */
    public void setNthread(int nthread) {
        log("parameter 'nthread' is deprecated; remove from your build");
        this.nthread = nthread;
    }

    /**
     * This flag is no longer used. Use instead the names of gzip-enabled
     * formatters.
     * 
     * @param gzipOutput
     *            flag indicating whether to gzip output
     * @deprecated
     */
    public void setGzipOutput(boolean gzipOutput) {
        log("parameter 'gzipOutput' is deprecated; use 'formats' option instead");
    }

    /**
     * Level at which deprecation warnings are issued. If less than zero, then
     * none are printed. If zero, warnings are issued for things that will
     * change in next release. If greater than zero, then other future changes
     * will be flagged.
     * 
     * Deprecated: Use warnings option instead.
     * 
     * @param deprecationLevel
     *            level at which to give deprecation warnings
     * @deprecated
     */
    public void setDeprecationLevel(int deprecationLevel) {
        log("parameter 'deprecationLevel' is deprecated; used 'warnings' instead");
        this.deprecationLevel = deprecationLevel;
    }

    /**
     * Flag to indicate whether or not warnings should be treated as errors.
     * 
     * Deprecated: Use warnings option instead.
     * 
     * @param failOnWarn
     *            setting the value true will cause compilations to fail on
     *            warnings
     * @deprecated
     */
    public void setFailOnWarn(boolean failOnWarn) {
        log("parameter 'fileOnWarn' is deprecated; used 'warnings' instead");
        this.failOnWarn = failOnWarn;
    }

    /**
     * This option no longer has any effect. Remove references to this parameter
     * from your build files.
     * 
     * @param forceBuild
     * @deprecated
     */
    public void setForceBuild(boolean forceBuild) {
        log("parameter 'forceBuild' is deprecated and has no effect; remove from your build");
    }

    /**
     * Deprecated alias for setInitialData.
     * 
     * @param rootElement
     * @deprecated
     */
    public void setRootElement(String rootElement) {
        log("parameter 'rootElement' is deprecated; use 'initialData' instead");
        this.initialData = rootElement;
    }

}
