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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.CompilerOptions.DeprecationWarnings;
import org.quattor.pan.output.DepFormatter;
import org.quattor.pan.output.Formatter;
import org.quattor.pan.parser.ASTTemplate;
import org.quattor.pan.repository.SourceType;
import org.quattor.pan.tasks.CompileTask;
import org.quattor.pan.utils.FileStatCache;
import org.quattor.pan.utils.FileUtils;

public class DependencyChecker {

	private final List<File> includeDirectories;

	private final Pattern ignoreDependencyPattern;

	private final FileStatCache statCache = new FileStatCache();

	private final static Pattern NONE = Pattern.compile("^$");

	private final Set<Formatter> formatters;

	private final CompilerOptions options = CompilerOptions
			.createCheckSyntaxOptions(DeprecationWarnings.OFF);

	private final URI outputDirectoryURI;

	private final Formatter depFormatter = DepFormatter.getInstance();

	public DependencyChecker(List<File> includeDirectories,
			File outputDirectory, Set<Formatter> formatters,
			Pattern ignoredDependencyPattern) {

		ArrayList<File> dirs = new ArrayList<File>();

		if (includeDirectories != null) {
			dirs.addAll(includeDirectories);
		} else {
			String userDir = System.getProperty("user.dir");
			File file = new File(userDir).getAbsoluteFile();
			dirs.add(file);
		}
		dirs.trimToSize();
		this.includeDirectories = Collections.unmodifiableList(dirs);

		if (ignoredDependencyPattern != null) {
			this.ignoreDependencyPattern = ignoredDependencyPattern;
		} else {
			this.ignoreDependencyPattern = NONE;
		}

		this.formatters = addDepFormatter(formatters);

		if (outputDirectory == null) {
			String userDir = System.getProperty("user.dir");
			File file = new File(userDir).getAbsoluteFile();
			this.outputDirectoryURI = file.toURI();
		} else {
			this.outputDirectoryURI = outputDirectory.toURI();
		}
	}

	public List<File> filterForOutdatedFiles(List<File> objectFiles) {

		LinkedList<File> outdated = new LinkedList<File>();

		for (File objectFile : objectFiles) {
			if (isOutdated(objectFile)) {
				outdated.add(objectFile);
			}
		}

		return outdated;
	}

	public boolean isOutdated(File objectFile) {

		String objectName = extractLocalizedTemplateName(objectFile, options);

		// The object name may be null if there was a problem parsing the
		// template. In this case, assume that the file is outdated and allow
		// the error to resurface later.
		if (objectName == null) {
			return true;
		}

		List<File> outputFiles = resolveOutputFiles(objectName,
				outputDirectoryURI, formatters);

		// Output file missing?
		for (File outputFile : outputFiles) {
			if (!statCache.exists(outputFile)) {
				return true;
			}
		}

		// Get the EARLIEST modification time for all of the output files.
		long targetTime = Long.MAX_VALUE;
		for (File outputFile : outputFiles) {
			long t = statCache.getModificationTime(outputFile);
			if (t < targetTime) {
				targetTime = t;
			}
		}

		// Check dependency file was generated at the same time or after the
		// target files.
		File depFile = resolveOutputFile(objectName, outputDirectoryURI,
				depFormatter);
		if (statCache.isMissingOrModifiedBefore(depFile, targetTime)) {
			return true;
		}

		// All simple checks passed, so now to the detailed checking of the full
		// dependency file.
		return isDependencyListOutdated(depFile, targetTime);
	}

	public static String extractLocalizedTemplateName(File sourceFile,
			CompilerOptions options) {

		try {

			ASTTemplate ast = CompileTask.CallImpl.compile(sourceFile, options);
			String name = ast.getIdentifier();
			return FileUtils.localizeFilename(name);

		} catch (Exception e) {
			return null;
		}

	}

	public boolean isDependencyListOutdated(File dependencyFile, Long targetTime) {

		boolean outdated = false;

		Scanner scanner = null;

		try {
			scanner = new Scanner(dependencyFile);

			while (scanner.hasNextLine() && !outdated) {
				if (isDependencyOutdated(scanner.nextLine(), targetTime)) {
					outdated = true;
					break;
				}
			}

		} catch (IllegalArgumentException e) {

			// This is usually the result of reading a dependency file from an
			// old version of the compiler. Assume that the profile needs to be
			// compiled.
			System.err.println("Warning: Outdated dependency file ("
					+ dependencyFile.toString() + "); compiling profile");
			outdated = true;

		} catch (IOException e) {

			// If there's a problem finding or reading the file, then assume
			// that the dependency is outdated.
			outdated = true;

		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}

		return outdated;
	}

	public boolean isDependencyOutdated(String line, Long targetTime) {

		DependencyInfo info = new DependencyInfo(line);

		if (ignoreDependencyPattern.matcher(info.name).matches()) {
			return false;
		}

		switch (info.type) {

		case TPL:
			return isSourceDependencyOutdated(info, targetTime);
		case PAN:
			return isSourceDependencyOutdated(info, targetTime);
		case TEXT:
			return isTextDependencyOutdated(info, targetTime);
		case ABSENT_SOURCE:
			return (lookupSourceFile(info.name) != null);
		case ABSENT_TEXT:
			return (lookupTextFile(info.name) != null);
		default:
			throw new BuildException("unknown file type: " + info.type);
		}

	}

	public boolean isSourceDependencyOutdated(DependencyInfo info,
			long targetTime) {

		if (isSingleDependencyOutdated(info.file, targetTime)) {
			return true;
		}

		// Check that the location hasn't changed in the path. If it has
		// changed, then profile isn't current.
		File foundFile = lookupSourceFile(info.name);
		return isSingleDependencyDifferent(info.file, foundFile);

	}

	public boolean isTextDependencyOutdated(DependencyInfo info, long targetTime) {

		if (isSingleDependencyOutdated(info.file, targetTime)) {
			return true;
		}

		// Check that the location hasn't changed in the path. If it has
		// changed, then profile isn't current.
		File foundFile = lookupTextFile(info.name);
		return isSingleDependencyDifferent(info.file, foundFile);

	}

	public boolean isSingleDependencyOutdated(File dep, long targetTime) {

		if (dep != null) {
			return statCache.isMissingOrModifiedAfter(dep, targetTime);
		} else {
			return true;
		}

	}

	public File lookupSourceFile(String tplName) {

		String localTplName = FileUtils.localizeFilename(tplName);

		List<String> sourceFiles = new ArrayList<String>();
		for (String extension : SourceType.getExtensions()) {
			sourceFiles.add(localTplName + extension);
		}

		for (File pathdir : includeDirectories) {
			for (String sourceFile : sourceFiles) {

				File check = new File(pathdir, sourceFile);
				if (statCache.exists(check)) {
					return check;
				}
			}
		}

		return null;
	}

	public File lookupTextFile(String tplName) {

		String localTplName = FileUtils.localizeFilename(tplName);

		for (File pathdir : includeDirectories) {

			File check = new File(pathdir, localTplName);
			if (statCache.exists(check)) {
				return check;
			}
		}

		return null;
	}

	public static Set<Formatter> addDepFormatter(Set<Formatter> formatters) {

		boolean mustIncludeDepFormatter = true;

		Set<Formatter> f = new HashSet<Formatter>();
		f.addAll(formatters);

		for (Formatter formatter : formatters) {
			if (formatter instanceof DepFormatter) {
				mustIncludeDepFormatter = false;
			}
		}

		if (mustIncludeDepFormatter) {
			f.add(DepFormatter.getInstance());
		}

		return Collections.unmodifiableSet(f);
	}

	public static File resolveOutputFile(String objectName, URI uri,
			Formatter formatter) {

		URI outputURI = uri.resolve(formatter.getResultURI(objectName));
		return new File(outputURI);
	}

	public static List<File> resolveOutputFiles(String objectName, URI uri,
			Set<Formatter> formatters) {

		List<File> outputFiles = new ArrayList<File>();

		for (Formatter f : formatters) {
			outputFiles.add(resolveOutputFile(objectName, uri, f));
		}

		return outputFiles;
	}

	public static boolean isSingleDependencyDifferent(File dep, File foundFile) {
		if (foundFile != null) {
			return (!dep.equals(foundFile));
		} else {

			// SPECIAL CASE:
			//
			// If the file hasn't been found at all, then assume the file is
			// up to date. The file may not have been found on the load path
			// because the internal loadpath variable may be used to find
			// the file. In this case, rely on the explicit
			// list of dependencies to pick up changes. NOTE: this check
			// isn't 100% correct. It is possible to move templates around
			// in the "internal" load path; these changes will not be picked
			// up correctly.

			return false;
		}
	}

	public static String stripPanExtensions(String name) {

		for (SourceType type : SourceType.values()) {
			String extension = type.getExtension();
			if (!"".equals(extension)) {
				if (name.endsWith(extension)) {
					int index = name.lastIndexOf(extension);
					return name.substring(0, index);
				}
			}
		}

		return name;
	}

	public static File reconstructSingleDependency(String templatePath,
			String tplName, SourceType type) throws URISyntaxException {

		URI path = new URI(templatePath);
		URI fullname = new URI(tplName + type.getExtension());
		URI fullpath = path.resolve(fullname);

		return new File(fullpath).getAbsoluteFile();

	}

	public static class DependencyInfo {

		public final String name;

		public final SourceType type;

		public final File file;

		public DependencyInfo(String dependencyLine) {

			// Format is a whitespace-separated line. The items are 1)
			// template name (or full file name), 2) file type, and 3) full
			// URI for parent directory. The third element is only there if
			// the file wasn't absent.
			String[] fields = dependencyLine.split("\\s+");

			if (fields.length != 2 && fields.length != 3) {
				throw new BuildException("malformed dependency line");
			}

			name = fields[0];
			type = SourceType.valueOf(fields[1]);

			if (fields.length == 3) {

				try {
					file = reconstructSingleDependency(fields[2], name, type);
				} catch (URISyntaxException e) {
					throw new BuildException(e.getMessage());
				}

			} else {
				file = null;
			}

			validate();

		}

		private void validate() {

			if (file == null && !type.isAbsent()) {
				throw new BuildException(
						"missing path information for dependency");
			}

			if (file != null && type.isAbsent()) {
				throw new BuildException("path information for absent file");
			}

		}

	}

}
