package org.quattor.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Goal which runs a pan syntax check on source files.
 * 
 * @goal check
 * 
 * @phase process-sources
 */
public class CheckMojo extends AbstractMojo {

	/**
	 * Root directory of the pan sources to check.
	 * 
	 * @parameter expression="${panc.sourceDirectory}" default-value="${basedir}/src/main/pan"
	 * @required
	 */
	private File sourceDirectory;

	public void execute() throws MojoExecutionException {

		File f = outputDirectory;

		FileWriter w = null;
		try {
			w = new FileWriter(touch);

			w.write("touch.txt");
		} catch (IOException e) {
			throw new MojoExecutionException("Error creating file " + touch, e);
		} finally {
			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	public void collectPanSources(Collection sources, File directory) {
		
		if (directory.exists()) {
			collectPanFiles(sources, directory);

			List<File> subdirs = collectSubdirs(directory);
			for (File subdir : subdirs) {
				collectPanSources(sources, subdir);
			}
		}

	}
	
	public void collectPanFiles(Collection sources, File directory) {
		if (directory.exists()) {
			return directory.listFiles(null);
		} else {
			return new File[0];
		}
	}
	
	public File[] collectSubdirs(File directory) {
		if (directory.exists()) {
			return directory.listFiles(new DirectoryFileFilter());
		} else {
			return new File[0];
		}
	}
	
	public static class DirectoryFileFilter {		
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	}

	public static class PanSourceFileFilter {		
		public boolean accept(File pathname) {
			String fname = pathname.getName();
			return name.endsWith(".pan") || name.endsWith(".tpl");
		}
	}
}
