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

import java.io.File;
import java.util.List;

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
public class FileSystemSourceRepositoryWithSessionDir extends
		FileSystemSourceRepository {

	enum SearchResult {
		SRC_FOUND, DEL_FOUND, NOT_FOUND
	};

	private final File sessionDir;

	private final List<File> includeDirectories;

	public FileSystemSourceRepositoryWithSessionDir(File sessionDirectory,
			List<File> includeDirectories) {

		this.sessionDir = sessionDirectory;
		this.includeDirectories = validateAndCopyIncludeDirectories(includeDirectories);

	}

	@Override
	public File lookupText(String name) {
		return lookupText(name, emptyRelativePaths);
	}

	@Override
	public File lookupText(String name, List<String> loadpath) {

		assert (loadpath != null);
		assert (loadpath.size() > 0);

		String localName = localizeName(name);

		String del = localName + ".del"; //$NON-NLS-1$

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

					result = lookupSingleTextFile(sessionDir, rpath, del,
							localName, fileHolder);

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
				result = lookupSingleTextFile(d, rpath, del, localName,
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

		}

		// Return the found template.
		return fileHolder.file;
	}

	@Override
	public File lookupSource(String name) {
		return lookupSource(name, emptyRelativePaths);
	}

	@Override
	public File lookupSource(String name, List<String> loadpath) {

		assert (loadpath != null);
		assert (loadpath.size() > 0);

		String localName = localizeName(name);

		String del = localName + ".del"; //$NON-NLS-1$

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

					result = lookupSingleSourceFile(sessionDir, rpath, del,
							localName, fileHolder);

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
				result = lookupSingleSourceFile(d, rpath, del, localName,
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
	 *            localized deleted file marker name
	 * @param name
	 *            localized template name
	 * @param fileHolder
	 *            holder for result if source file was found
	 * 
	 * @return SearchResult indicating if the delete marker, source file, or
	 *         nothing was found
	 */
	private SearchResult lookupSingleSourceFile(File root, String rpath,
			String delFileName, String name, FileHolder fileHolder) {

		File dir = new File(root, rpath);
		File deletedFileMarker = new File(dir, delFileName);

		if (!deletedFileMarker.exists()) {

			for (String suffix : sourceFileExtensions) {
				File sourceFile = new File(dir, name + suffix);
				if (sourceFile.exists()) {
					fileHolder.file = sourceFile;
					return SearchResult.SRC_FOUND;
				}
			}
			return SearchResult.NOT_FOUND;
		} else {
			return SearchResult.DEL_FOUND;
		}

	}

	private SearchResult lookupSingleTextFile(File root, String rpath,
			String delFileName, String name, FileHolder fileHolder) {

		File dir = new File(root, rpath);
		File deletedFileMarker = new File(dir, delFileName);

		if (!deletedFileMarker.exists()) {

			File sourceFile = new File(dir, name);
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
