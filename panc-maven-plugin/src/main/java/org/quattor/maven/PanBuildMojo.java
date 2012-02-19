package org.quattor.maven;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.CompilerResults;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.output.Formatter;
import org.quattor.pan.output.FormatterUtils;

/**
 * @description perform a full build of pan language templates
 * @goal pan-build
 * @phase compile
 */
public class PanBuildMojo extends AbstractPanMojo {

    /**
     * @description subdirectory with object templates
     * @parameter expression="${panc.profiles}" default-value="profiles"
     * @required
     */
    private String profiles = "profiles";

    /**
     * @description directory for generated profiles
     * @parameter expression="${panc.outputDirectory}"
     *            default-value="${basedir}/target"
     * @required
     */
    private File outputDirectory;

    /**
     * @description formatter to use
     * @parameter expression="${panc.formatterName}" default-value="pan"
     * @required
     */
    private String formatterName = "pan";

    private Formatter formatter;

    public void execute() throws MojoExecutionException {

        setFormatter();

        createOutputDirectory();

        CompilerOptions options = createCompilerOptions();

        File profileDirectory = new File(sourceDirectory, profiles);

        Set<File> objects = PluginUtils.collectPanSources(profileDirectory);

        CompilerResults results = Compiler.run(options, null, objects);

        boolean hadError = results.print(verbose);

        if (hadError) {
            throw new MojoExecutionException("pan language syntax check failed");
        }

    }

    private void setFormatter() throws MojoExecutionException {
        formatter = FormatterUtils.getFormatterInstance(formatterName);
        if (formatter == null) {
            throw new MojoExecutionException("unknown formatter: "
                    + formatterName);
        }
    }

    private void createOutputDirectory() throws MojoExecutionException {
        if (!outputDirectory.isDirectory()) {
            if (!outputDirectory.mkdirs()) {
                throw new MojoExecutionException("error creating "
                        + outputDirectory);
            }
        }
    }

    private CompilerOptions createCompilerOptions()
            throws MojoExecutionException {

        List<Pattern> debugIncludePatterns = new LinkedList<Pattern>();
        List<Pattern> debugExcludePatterns = new LinkedList<Pattern>();
        boolean xmlWriteEnabled = true;
        boolean depWriteEnabled = true;
        int iterationLimit = 5000;
        int callDepthLimit = 50;
        File sessionDirectory = null;
        int nthread = 0;
        boolean gzipOutput = false;
        boolean forceBuild = false;
        File annotationDirectory = null;
        File annotationBaseDirectory = null;
        LinkedList<File> includeDirectories = new LinkedList<File>();
        includeDirectories.add(sourceDirectory);

        try {
            return new CompilerOptions(debugIncludePatterns,
                    debugExcludePatterns, xmlWriteEnabled, depWriteEnabled,
                    iterationLimit, callDepthLimit, formatter, outputDirectory,
                    sessionDirectory, includeDirectories, nthread, gzipOutput,
                    deprecationLevel, forceBuild, annotationDirectory,
                    annotationBaseDirectory, failOnWarn, null);

        } catch (SyntaxException e) {
            throw new MojoExecutionException(
                    "error creating compiler options: " + e.getMessage());
        }
    }

}
