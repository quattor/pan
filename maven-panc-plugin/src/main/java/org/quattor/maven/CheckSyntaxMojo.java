package org.quattor.maven;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.CompilerResults;

/**
 * Goal which runs a pan syntax check on source files.
 * 
 * @goal check-syntax
 * 
 * @phase process-sources
 */
public class CheckSyntaxMojo extends AbstractMojo {

	/**
	 * Root directory of the pan sources to check.
	 * 
	 * @parameter expression="${panc.sourceDirectory}"
	 *            default-value="${basedir}/src/main/pan"
	 * @required
	 */
	private File sourceDirectory;

	/**
	 * If the verbose flag is set, then a summary will be printed.
	 * 
	 * @parameter expression="${panc.verbose}" default-value=false
	 * @required
	 */
	private boolean verbose;

	/**
	 * Level at which deprecation warnings are issued. If less than zero, then
	 * none are printed. If zero, warnings are issued for things that will
	 * change in next release. If greater than zero, then other future changes
	 * will be flagged.
	 * 
	 * @parameter expression="${panc.deprecationLevel}" default-value=0
	 * @required
	 */
	private int deprecationLevel = 0;

	/**
	 * If this flag is set, then warnings will be treated as errors.
	 * 
	 * @parameter expression="${panc.failOnWarn}" default-value=false
	 * @required
	 */
	private boolean failOnWarn = false;

	public void execute() throws MojoExecutionException {

		CompilerOptions options = CompilerOptions.createCheckSyntaxOptions(
				deprecationLevel, failOnWarn);

		Set<File> sources = new TreeSet<File>();
		collectPanSources(sources, sourceDirectory);

		CompilerResults results = Compiler.run(options, null, sources);

		boolean hadError = results.print(verbose);

		if (hadError) {
			throw new MojoExecutionException("pan language syntax check failed");
		}

	}

	public void collectPanSources(Set<File> sources, File directory) {

		if (directory.exists()) {

			for (File file : panSourceFiles(directory)) {
				sources.add(file);
			}

			for (File subdir : subdirectories(directory)) {
				collectPanSources(sources, subdir);
			}
		}

	}

	public File[] panSourceFiles(File directory) {
		if (directory.exists() && directory.isDirectory()) {
			return directory.listFiles(PanSourceFileFilter.getInstance());
		} else {
			return new File[0];
		}
	}

	public File[] subdirectories(File directory) {
		if (directory.exists() && directory.isDirectory()) {
			return directory.listFiles(DirectoryFileFilter.getInstance());
		} else {
			return new File[0];
		}
	}

	public static class DirectoryFileFilter implements FileFilter {

		public static DirectoryFileFilter singleton = new DirectoryFileFilter();

		private DirectoryFileFilter() {

		}

		public static DirectoryFileFilter getInstance() {
			return singleton;
		}

		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	}

	public static class PanSourceFileFilter implements FileFilter {

		public static PanSourceFileFilter singleton = new PanSourceFileFilter();

		private PanSourceFileFilter() {

		}

		public static PanSourceFileFilter getInstance() {
			return singleton;
		}

		public boolean accept(File pathname) {
			String fname = pathname.getName();
			return fname.endsWith(".pan") || fname.endsWith(".tpl");
		}
	}
}
