package org.quattor.pan.repository;

import static org.quattor.pan.utils.MessageUtils.MSG_NON_ABSOLUTE_PATH_IN_INCLUDE_DIRS;
import static org.quattor.pan.utils.MessageUtils.MSG_NON_DIRECTORY_IN_INCLUDE_DIRS;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.quattor.pan.exceptions.EvaluationException;

abstract public class FileSystemSourceRepository implements SourceRepository {

	// When there are no real relative paths in the load path, use a list of
	// relative paths with only the empty string as an element. This avoids
	// having to constantly check whether the relative path is null.
	protected final static List<String> emptyRelativePaths;
	static {
		ArrayList<String> list = new ArrayList<String>(1);
		list.add(""); //$NON-NLS-1$
		list.trimToSize();
		emptyRelativePaths = list;
	}

	protected FileSystemSourceRepository() {
	}

	public static SourceRepository getInstance(ParameterList parameters) {

		if (parameters == null) {
			parameters = new ParameterList();
		}

		File sessionDirectory = null;
		List<File> includeDirectories = new ArrayList<File>();
		for (Parameter parameter : parameters) {
			String name = parameter.getKey();
			if ("sessionDirectory".equals(name)) {
				sessionDirectory = new File(parameter.getValue());
			} else if ("includeDirectory".equals(name)) {
				includeDirectories.add(new File(parameter.getValue()));
			}
		}

		if (sessionDirectory == null) {
			return new FileSystemSourceRepositoryWithoutSessionDir(
					includeDirectories);
		} else {
			return new FileSystemSourceRepositoryWithSessionDir(
					sessionDirectory, includeDirectories);
		}
	}

	abstract public File lookup(String name);

	abstract public File lookup(String name, String suffix);

	abstract public File lookup(String name, List<String> loadpath);

	abstract public File lookup(String name, String suffix,
			List<String> loadpath);

	public SourceFile retrievePanSource(String name) {
		File file = lookup(name);
		return createPanSourceFile(name, file);
	}

	public SourceFile retrievePanSource(String name, List<String> loadpath) {
		File file = lookup(name, loadpath);
		return createPanSourceFile(name, file);
	}

	public SourceFile retrieveTxtSource(String name) {
		File file = lookup(name, "");
		return createTxtSourceFile(name, file);
	}

	public SourceFile retrieveTxtSource(String name, List<String> loadpath) {
		File file = lookup(name, "", loadpath);
		return createTxtSourceFile(name, file);
	}

	private SourceFile createPanSourceFile(String name, File file) {
		if (file != null) {
			return new SourceFile(name, SourceFile.Type.PAN, file);
		} else {
			return new SourceFile(name, SourceFile.Type.MISSING, null);
		}
	}

	private SourceFile createTxtSourceFile(String name, File file) {
		if (file != null) {
			return new SourceFile(name, SourceFile.Type.TXT, file);
		} else {
			return new SourceFile(name, SourceFile.Type.MISSING, null);
		}
	}

	protected List<File> validateAndCopyIncludeDirectories(
			List<File> includeDirectories) {

		ArrayList<File> dirs = new ArrayList<File>();

		// Create a copy to avoid any external modifications. Use the current
		// working directory if paths is null or empty.
		if (includeDirectories != null && includeDirectories.size() != 0) {

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

			dirs.addAll(includeDirectories);

		} else {

			// No values were given. Use current working directory.
			dirs.add(new File(System.getProperty("user.dir"))); //$NON-NLS-1$

		}

		dirs.trimToSize();

		return Collections.unmodifiableList(dirs);
	}

}
