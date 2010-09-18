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
import java.util.LinkedList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.CompilerResults;
import org.quattor.pan.repository.SourceType;

/**
 * An ant task which checks the pan language syntax for a specified list of
 * files. See individual setter methods for the parameters which can be used in
 * the build file.
 * 
 * @author loomis
 * 
 */
public class PanCheckSyntaxTask extends Task {

	private LinkedList<File> sourceFiles = new LinkedList<File>();

	private boolean verbose = false;

	private int deprecationLevel = 0;

	@Override
	public void execute() throws BuildException {

		CompilerOptions options = CompilerOptions
				.createCheckSyntaxOptions(deprecationLevel);

		CompilerResults results = Compiler.run(options, null, sourceFiles);

		boolean hadError = results.print(verbose);

		if (hadError) {
			throw new BuildException("Compilation failed; see messages.");
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
				sourceFiles.add(new File(basedir, f));
			}
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

}
