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
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.quattor.pan.repository.FileSystemSourceRepository;
import org.quattor.pan.repository.SourceFile.Type;
import org.quattor.pan.utils.FileStatCache;

public class DependencyChecker {

	private final List<File> includeDirectories;

	private final Pattern ignoreDependencyPattern;

	// Create a cache for the modification times of the templates. In the
	// case where all of the files are up to date, this will save repeated
	// disk reads to determine the state of files as dependencies are in
	// common within a cluster.
	private final FileStatCache statCache = new FileStatCache();

	// Collect all of the possible source file types (*.tpl, *.pan, etc.).
	private static final List<String> sourceFileExtensions;
	static {

		ArrayList<String> extensions = new ArrayList<String>();

		for (Type type : Type.values()) {
			if (type.isSource()) {
				extensions.add(type.getExtension());
			}
		}

		extensions.trimToSize();

		sourceFileExtensions = Collections.unmodifiableList(extensions);
	};

	public DependencyChecker(List<File> includeDirectories,
			Pattern ignoredDependencyPattern) {

		ArrayList<File> dirs = new ArrayList<File>();

		if (includeDirectories != null) {
			dirs.addAll(includeDirectories);
		} else {
			String cwd = System.getProperty("user.dir");
			File file = new File(cwd).getAbsoluteFile();
			dirs.add(file);
		}
		dirs.trimToSize();
		this.includeDirectories = Collections.unmodifiableList(dirs);

		if (ignoredDependencyPattern != null) {
			this.ignoreDependencyPattern = ignoredDependencyPattern;
		} else {
			this.ignoreDependencyPattern = Pattern.compile("^$");
		}
	}

	public List<File> outdatedObjectFiles(List<File> objectFiles,
			File outputDirectory) {

		LinkedList<File> outdated = new LinkedList<File>();

		for (File objectFile : objectFiles) {

			// Map the file into the output file and the dependency
			// file.
			String name = objectFile.getName();
			name = stripPanExtensions(name);

			File t = new File(outputDirectory, name + ".xml");
			File d = new File(outputDirectory, name + ".xml.dep");

			// Only do detailed checking if both the output file and
			// the dependency file exist.
			if (!(t.exists() && d.exists())) {
				outdated.add(objectFile);
				continue;
			}

			// The modification time of the target xml file.
			long targetTime = t.lastModified();

			// The dependency file must have been generated at the
			// same time or after the xml file.
			if (d.lastModified() < t.lastModified()) {
				outdated.add(objectFile);
				continue;
			}

			// Do detailed checking of the full dependency file.
			if (processDependencyFile(d, targetTime)) {
				outdated.add(objectFile);
			}
		}

		// Send back the number which are up-to-date.
		return outdated;
	}

	public boolean processDependencyFile(File dependencyFile, Long targetTime) {

		boolean outdated = false;

		Scanner scanner = null;

		try {
			scanner = new Scanner(dependencyFile);

			while (scanner.hasNextLine() && !outdated) {
				outdated = processDependencyLine(scanner.nextLine(), targetTime);
			}

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

	public boolean processDependencyLine(String line, Long targetTime) {

		boolean outdated = false;

		// Format is a whitespace-separated line. The items are 1)
		// template name (or full file name), 2) file type, and 3) full
		// URI for parent directory. The third element is only there is
		// the file wasn't absent.
		String[] depInfo = line.split("\\s+");

		if (depInfo.length >= 2) {

			// Extract the template name.
			String tplName = depInfo[0];

			// Only processing of the dependency if it is not being
			// ignored.
			if (!ignoreDependencyPattern.matcher(tplName).matches()) {

				// Whether a given source file is out of date depends on
				// the type of the source file.
				Type type = Type.valueOf(depInfo[1]);
				switch (type) {

				case TPL: // fall through
				case PAN: // fall through
				case PANX: { // all sources handled the same way

					String templatePath = depInfo[2];

					outdated = isSourceDependencyOutdated(tplName, type,
							templatePath, targetTime);

					break;
				}

				case TEXT: {

					String templatePath = depInfo[2];

					outdated = isTextDependencyOutdated(tplName, type,
							templatePath, targetTime);

					break;
				}

				case ABSENT_SOURCE:

					outdated = (lookupSourceFile(tplName) != null);
					break;

				case ABSENT_TEXT:

					outdated = (lookupTextFile(tplName) != null);
					break;

				default:
					throw new BuildException("unknown file type: " + type);
				}

			}
		}

		return outdated;
	}

	public boolean isSourceDependencyOutdated(String tplName, Type type,
			String templatePath, long targetTime) {

		File dep = reconstructSingleDependency(templatePath, tplName, type);
		if (isSingleDependencyOutdated(dep, targetTime)) {
			return true;
		}

		// Check that the location hasn't changed in the path. If it has
		// changed, then profile isn't current.
		File foundFile = lookupSourceFile(tplName);
		return isSingleDependencyDifferent(dep, foundFile);

	}

	public boolean isTextDependencyOutdated(String tplName, Type type,
			String templatePath, long targetTime) {

		File dep = reconstructSingleDependency(templatePath, tplName, type);
		if (isSingleDependencyOutdated(dep, targetTime)) {
			return true;
		}

		// Check that the location hasn't changed in the path. If it has
		// changed, then profile isn't current.
		File foundFile = lookupTextFile(tplName);
		return isSingleDependencyDifferent(dep, foundFile);

	}

	public boolean isSingleDependencyOutdated(File dep, long targetTime) {

		if (dep != null) {
			return statCache.isMissingOrModifiedAfter(dep, targetTime);
		} else {
			return true;
		}

	}

	public File lookupSourceFile(String tplName) {

		String localTplName = FileSystemSourceRepository.localizeName(tplName);

		String[] sourceFiles = new String[sourceFileExtensions.size()];
		for (int i = 0; i < sourceFileExtensions.size(); i++) {
			sourceFiles[i] = localTplName + sourceFileExtensions.get(i);
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

		String localTplName = FileSystemSourceRepository.localizeName(tplName);

		for (File pathdir : includeDirectories) {

			File check = new File(pathdir, localTplName);
			if (statCache.exists(check)) {
				return check;
			}
		}

		return null;
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
	
		for (Type type : Type.values()) {
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
			String tplName, Type type) {
	
		try {
	
			URI path = new URI(templatePath);
			URI fullname = new URI(tplName + type.getExtension());
			URI fullpath = path.resolve(fullname);
	
			return new File(fullpath).getAbsoluteFile();
	
		} catch (URISyntaxException e) {
			return null;
		}
	}

}
