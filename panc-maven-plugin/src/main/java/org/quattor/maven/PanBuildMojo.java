package org.quattor.maven;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerLogging;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.CompilerResults;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.output.Formatter;

/**
 * perform a full build of pan language templates
 * 
 * @goal pan-build
 * @phase compile
 */
public class PanBuildMojo extends AbstractPanMojo {

    /**
     * subdirectory with object templates
     * 
     * @parameter property="panc.profiles" default-value="profiles"
     * @required
     */
    private String profiles = "profiles";

    /**
     * pattern to include templates for debugging
     * 
     * @parameter property="panc.debugNsInclude" default-value=".+"
     */
    private String debugNsInclude = ".+";

    /**
     * pattern to exclude templates for debugging
     * 
     * @parameter property="panc.debugNsExclude" default-value="^$"
     */
    private String debugNsExclude = "^$";

    /**
     * initial data for configuration
     * 
     * @parameter property="panc.initialData"
     */
    private String initialData = null;

    /**
     * directory for generated profiles
     * 
     * @parameter property="panc.outputDir" default-value="${basedir}/target"
     * @required
     */
    private File outputDir;

    /**
     * list of formats for output files (comma-separated list)
     * 
     * @parameter property="panc.formats" default-value="pan,dep"
     * @required
     */
    private String formats = "pan,dep";

    /**
     * maximum number of iterations
     * 
     * @parameter property="panc.maxIteration" default-value=10000
     * @required
     */
    private int maxIteration = 10000;

    /**
     * maximum number of recursions
     * 
     * @parameter property="panc.maxRecursion" default-value=50
     * @required
     */
    private int maxRecursion = 50;

    /**
     * logging types
     * 
     * @parameter property="panc.logging" default-value="none"
     */
    private String logging = "none";

    /**
     * log file
     * 
     * @parameter property="panc.logFile"
     */
    private File logFile = null;

    private Set<Formatter> formatters;

    public void execute() throws MojoExecutionException {

        setFormatters();

        createoutputDir();

        CompilerOptions options = createCompilerOptions();

        // Activate loggers if specified. If the logging is activated but there
        // is no log file, no output will be generated.
        CompilerLogging.activateLoggers(logging);
        CompilerLogging.setLogFile(logFile);

        File profileDirectory = new File(sourceDirectory, profiles);

        Set<File> objects = PluginUtils.collectPanSources(profileDirectory);

        CompilerResults results = Compiler.run(options, null, objects);

        boolean hadError = results.print(verbose);

        if (hadError) {
            throw new MojoExecutionException("pan language syntax check failed");
        }

    }

    private void setFormatters() throws MojoExecutionException {
        formatters = CompilerOptions.getFormatters(formats);
    }

    private void createoutputDir() throws MojoExecutionException {
        if (!outputDir.isDirectory()) {
            if (!outputDir.mkdirs()) {
                throw new MojoExecutionException("error creating " + outputDir);
            }
        }
    }

    private CompilerOptions createCompilerOptions()
            throws MojoExecutionException {

        List<Pattern> debugIncludePatterns = new LinkedList<Pattern>();
        debugIncludePatterns.add(Pattern.compile(debugNsInclude));

        List<Pattern> debugExcludePatterns = new LinkedList<Pattern>();
        debugExcludePatterns.add(Pattern.compile(debugNsExclude));

        int nthread = 0;
        LinkedList<File> includeDirectories = new LinkedList<File>();
        includeDirectories.add(sourceDirectory);

        try {
            return new CompilerOptions(debugIncludePatterns,
                    debugExcludePatterns, maxIteration, maxRecursion,
                    formatters, outputDir, null, includeDirectories, nthread,
                    warningsFromString(warnings), false, null, null,
                    initialData);

        } catch (SyntaxException e) {
            throw new MojoExecutionException(
                    "error creating compiler options: " + e.getMessage());
        }
    }

}
