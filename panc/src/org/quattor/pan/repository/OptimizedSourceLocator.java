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
public class OptimizedSourceLocator {

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

	public OptimizedSourceLocator(File sessionDirectory, List<File> includeDirectories) {

		ArrayList<File> dirs = new ArrayList<File>();

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
			dirs.addAll(includeDirectories);
			dirs.trimToSize();

		} else {

			// No values were given. Create a list with only one element, the
			// current working directory, in it.
			dirs.add(new File(System.getProperty("user.dir"))); //$NON-NLS-1$
			dirs.trimToSize();

		}

		dirs.trimToSize();
		this.includeDirectories = Collections.unmodifiableList(dirs);

	}

	public File lookup(String name) {
		return lookup(name, emptyRelativePaths);
	}

	public File lookup(String name, String suffix) {
		return lookup(name, suffix, emptyRelativePaths);
	}

	public File lookup(String name, List<String> loadpath) {
		return lookup(name, ".tpl", loadpath);
	}

	public File lookup(String name, String suffix, List<String> loadpath) {

		// Sanity checking. The loadpath must not be null and must contain at
		// least one element. For an empty loadpath, the list should contain one
		// empty string.
		assert (loadpath != null);
		assert (loadpath.size() > 0);

		String src = name + suffix;

		for (File d : includeDirectories) {
			for (String rpath : loadpath) {

				File dir = new File(d, rpath);
				File sourceFile = new File(dir, src);
				if (sourceFile.exists()) {
					return sourceFile;
				}

			}

		}

		// Return the found template.
		return null;
	}

}
