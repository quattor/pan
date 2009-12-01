/*
 Copyright (c) 2008 Charles A. Loomis, Jr, Cedric Duprilot, and
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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/TermTest.java $
 $Id: TermTest.java 3550 2008-08-02 14:54:26Z loomis $
 */
package org.quattor.pan.repository;

import static org.quattor.pan.utils.MessageUtils.MSG_NON_ABSOLUTE_PATH_IN_INCLUDE_DIRS;
import static org.quattor.pan.utils.MessageUtils.MSG_NON_DIRECTORY_IN_INCLUDE_DIRS;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.quattor.pan.exceptions.EvaluationException;

/**
 * This class encapsulates the template lookup mechanism. An instance contains
 * static references to the defined session directory (if any) and include
 * directories (if any). If no include directories are supplied, then the
 * current working directory is added as the only include directory.
 * 
 * The implementation currently only works with a file system. Future
 * implementations may extend this to include other storage mechanisms, e.g.
 * databases.
 * 
 * @author loomis
 * 
 */
public class SourceLocator {

	enum SearchResult {
		SRC_FOUND, DEL_FOUND, NOT_FOUND
	};

	private final File sessionDir;

	// References to this list must not be made available to clients. This
	// ensures that instances of this class are immutable without having to
	// create an UnmodifiableList.
	private final List<File> includeDirectories;

	// When there are no real relative paths in the load path, use a list of
	// relative paths with only the empty string as an element. This avoids
	// having to constantly check whether the relative path is null.
	private final static List<String> emptyRelativePaths;
	static {
		ArrayList<String> list = new ArrayList<String>(1);
		list.add(""); //$NON-NLS-1$
		list.trimToSize();
		emptyRelativePaths = list;
	}

	/**
	 * A TemplateLocator encapsulates the template lookup mechanism. A given
	 * instance uses fixed values for the session directory and include
	 * directories. A copy of the include directory list is made to ensure that
	 * no changes are made to the list after the class is instantiated.
	 * 
	 * @param sessionDirectory
	 *            Session directory to use for template lookup. If null, no
	 *            session directory will be used.
	 * @param includeDirectories
	 *            List of include directories to search for templates. If null
	 *            or empty, the current working directory will be used.
	 */
	public SourceLocator(File sessionDirectory, List<File> includeDirectories) {
		this.sessionDir = sessionDirectory;

		// Create a copy to avoid any external modifications. Use the current
		// working directory if paths is null or empty.
		if (includeDirectories != null && includeDirectories.size() != 0) {

			// A list has been given. Ensure values are OK and then make a copy.

			// First verify that all of the provided values are absolute paths
			// and that they represent directories. This also implies that the
			// directory exists.
			for (File d : includeDirectories) {
				if (!d.isAbsolute()) {
					throw EvaluationException
							.create(MSG_NON_ABSOLUTE_PATH_IN_INCLUDE_DIRS, d
									.toString());
				}
				if (!d.isDirectory()) {
					throw EvaluationException.create(
							MSG_NON_DIRECTORY_IN_INCLUDE_DIRS, d.toString());
				}
			}

			// Now actually make the copy. These values will always be accessed
			// in order. Use an ArrayList for fast iteration and low memory
			// requirements.
			ArrayList<File> dirs = new ArrayList<File>(includeDirectories
					.size());
			dirs.addAll(includeDirectories);
			dirs.trimToSize();
			this.includeDirectories = Collections.unmodifiableList(dirs);

		} else {

			// No values were given. Create a list with only one element, the
			// current working directory, in it.
			ArrayList<File> dirs = new ArrayList<File>(1);
			dirs.add(new File(System.getProperty("user.dir"))); //$NON-NLS-1$
			dirs.trimToSize();
			this.includeDirectories = Collections.unmodifiableList(dirs);

		}

	}

	public File lookup(String name) {
		return lookup(name, emptyRelativePaths);
	}

	public File lookup(String name, String suffix) {
		return lookup(name, suffix, emptyRelativePaths);
	}

	/**
	 * Locate a template with the given name using the defined session
	 * directory, include directories, and LOADPATH. The LOADPATH is a list of
	 * relative paths to search below the include directories. This is the value
	 * of the LOADPATH variable in pan.
	 * 
	 * The loadpath cannot be null or empty. If there are no relative paths to
	 * search, then the loadpath list should contain just a single empty string.
	 * Use the single argument version of this method for that.
	 * 
	 * For each directory constructed from the session directory, include
	 * directories, and the LOADPATH, this method tries to find a file with the
	 * extension ".del" or ".tpl" in that order. The method will return null if
	 * the file cannot be found. The ".del" indicates that the file has been
	 * deleted and the processing will stop if such a file is found.
	 * 
	 * @param name
	 *            String giving the name of the template to search for.
	 * @param loadpath
	 *            Relative search paths defined in LOADPATH variable. This
	 *            cannot be null or empty.
	 */
	public File lookup(String name, List<String> loadpath) {
		return lookup(name, ".tpl", loadpath);
	}

	/**
	 * Locate a template with the given name using the defined session
	 * directory, include directories, and LOADPATH. The LOADPATH is a list of
	 * relative paths to search below the include directories. This is the value
	 * of the LOADPATH variable in pan.
	 * 
	 * The loadpath cannot be null or empty. If there are no relative paths to
	 * search, then the loadpath list should contain just a single empty string.
	 * Use the single argument version of this method for that.
	 * 
	 * For each directory constructed from the session directory, include
	 * directories, and the LOADPATH, this method tries to find a file with the
	 * extension ".del" or ".tpl" in that order. The method will return null if
	 * the file cannot be found. The ".del" indicates that the file has been
	 * deleted and the processing will stop if such a file is found.
	 * 
	 * @param name
	 *            String giving the name of the template to search for.
	 * @param loadpath
	 *            Relative search paths defined in LOADPATH variable. This
	 *            cannot be null or empty.
	 */
	public File lookup(String name, String suffix, List<String> loadpath) {

		// Sanity checking. The loadpath must not be null and must contain at
		// least one element. For an empty loadpath, the list should contain one
		// empty string.
		assert (loadpath != null);
		assert (loadpath.size() > 0);

		String del = name + ".del"; //$NON-NLS-1$
		String src = name + suffix;

		// Create an empty FileHolder for the result.
		FileHolder fileHolder = new FileHolder();

		// Search through the loadpath to find the appropriate files. The loop
		// will terminate as soon as a matching source file is found. Finding a
		// matching *.del file will cause the next relative path to be checked.
		//
		// Note that the session directory check cannot be factored out even
		// though it does not depend on the outermost loop. The session
		// directory checks MUST be interleaved for each relative path in the
		// loadpath. Be careful, the algorithm is complicated and uses named
		// breaks and continue statements.
		template_lookup: for (File d : includeDirectories) {
			for (String rpath : loadpath) {

				SearchResult result;

				// If the session directory is not null, then it must be scanned
				// first. Three results are possible: the source file was found,
				// a *.del file was found, or no file was found.
				if (sessionDir != null) {

					result = lookupSingleFile(sessionDir, rpath, del, src,
							fileHolder);

					switch (result) {
					case SRC_FOUND:
						break template_lookup;
					case DEL_FOUND:
						continue;
					case NOT_FOUND:
						// Do nothing and fall through.
					}

				}

				// If we make it to here, then nothing was found in the session
				// directory. Check the usual directories.
				result = lookupSingleFile(d, rpath, del, src, fileHolder);

				switch (result) {
				case SRC_FOUND:
					break template_lookup;
				case DEL_FOUND:
					continue;
				case NOT_FOUND:
					// Do nothing and fall through.
				}

			}

		}

		// Return the found template.
		return fileHolder.file;
	}

	/**
	 * A utility function to check for a single source file. There are three
	 * possible results: the deleted file marker was found, the source file was
	 * found, or nothing was found. The corresponding SearchResult will be
	 * returned. If the source file was found, then the FileHolder will be
	 * updated with the source File.
	 * 
	 * @param root
	 *            base directory to use
	 * @param rpath
	 *            relative directory from the root to use
	 * @param delFileName
	 *            deleted file marker name
	 * @param srcFileName
	 *            source file name
	 * @param fileHolder
	 *            holder for result if source file was found
	 * 
	 * @return SearchResult indicating if the delete marker, source file, or
	 *         nothing was found
	 */
	private static SearchResult lookupSingleFile(File root, String rpath,
			String delFileName, String srcFileName, FileHolder fileHolder) {

		File dir = new File(root, rpath);
		File deletedFileMarker = new File(dir, delFileName);

		if (!deletedFileMarker.exists()) {
			File sourceFile = new File(dir, srcFileName);
			if (sourceFile.exists()) {
				fileHolder.file = sourceFile;
				return SearchResult.SRC_FOUND;
			}
			return SearchResult.NOT_FOUND;
		} else {
			return SearchResult.DEL_FOUND;
		}

	}

	/**
	 * Class to hold a reference to a File so that the value can be modified
	 * within a function call. This is rather ugly but avoids having to
	 * duplicate code.
	 * 
	 * @author loomis
	 * 
	 */
	private static class FileHolder {
		public File file = null;
	}

}
