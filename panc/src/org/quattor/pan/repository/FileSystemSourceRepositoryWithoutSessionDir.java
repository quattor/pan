package org.quattor.pan.repository;

import java.io.File;
import java.util.List;

public class FileSystemSourceRepositoryWithoutSessionDir extends
		FileSystemSourceRepository {

	private final List<File> includeDirectories;

	public FileSystemSourceRepositoryWithoutSessionDir(
			List<File> includeDirectories) {
		this.includeDirectories = validateAndCopyIncludeDirectories(includeDirectories);
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

		return null;
	}

}
