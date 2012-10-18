package org.quattor.maven;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.CompilerResults;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.output.Formatter;
import org.quattor.pan.output.FormatterComparator;
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
	 * @parameter expression="${panc.outputDir}"
	 *            default-value="${basedir}/target"
	 * @required
	 */
	private File outputDir;

	/**
	 * @description maximum number of iterations
	 * @parameter expression="${panc.maxIteration}" default-value=10000
	 * @required
	 */
	private int maxIteration = 10000;

	/**
	 * @description maximum number of recursions
	 * @parameter expression="${panc.maxRecursion}" default-value=10
	 * @required
	 */
	private int maxRecursion = 10;

	/**
	 * @description list of formats for output files (comma-separated list)
	 * @parameter expression="${panc.formats}" default-value="pan,dep"
	 * @required
	 */
	private String formats = "pan,dep";

	private Set<Formatter> formatters;

	public void execute() throws MojoExecutionException {

		setFormatters();

		createoutputDir();

		CompilerOptions options = createCompilerOptions();

		File profileDirectory = new File(sourceDirectory, profiles);

		Set<File> objects = PluginUtils.collectPanSources(profileDirectory);

		CompilerResults results = Compiler.run(options, null, objects);

		boolean hadError = results.print(verbose);

		if (hadError) {
			throw new MojoExecutionException("pan language syntax check failed");
		}

	}

	private void setFormatters() throws MojoExecutionException {
		formatters = new TreeSet<Formatter>(FormatterComparator.getInstance());
		
		// TODO: Add method to transform formats to formatters.
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
		List<Pattern> debugExcludePatterns = new LinkedList<Pattern>();
		int iterationLimit = 5000;
		int callDepthLimit = 50;
		File sessionDirectory = null;
		int nthread = 0;
		boolean forceBuild = false;
		File annotationDirectory = null;
		File annotationBaseDirectory = null;
		LinkedList<File> includeDirectories = new LinkedList<File>();
		includeDirectories.add(sourceDirectory);

		try {
			return new CompilerOptions(debugIncludePatterns,
					debugExcludePatterns, iterationLimit, callDepthLimit,
					formatters, outputDir, sessionDirectory,
					includeDirectories, nthread,
					CompilerOptions.DeprecationWarnings.OFF, forceBuild,
					annotationDirectory, annotationBaseDirectory, null);

		} catch (SyntaxException e) {
			throw new MojoExecutionException(
					"error creating compiler options: " + e.getMessage());
		}
	}

}
