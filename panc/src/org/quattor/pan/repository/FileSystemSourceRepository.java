package org.quattor.pan.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class FileSystemSourceRepository implements SourceRepository {

	private final SourceLocator sourceLocator;

	private FileSystemSourceRepository(File sessionDirectory,
			List<File> includeDirectories) {
		sourceLocator = new SourceLocator(sessionDirectory, includeDirectories);
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
		return new FileSystemSourceRepository(sessionDirectory,
				includeDirectories);
	}

	public SourceFile retrievePanSource(String name) {
		File file = sourceLocator.lookup(name);
		return createPanSourceFile(name, file);
	}

	public SourceFile retrievePanSource(String name, List<String> loadpath) {
		File file = sourceLocator.lookup(name, loadpath);
		return createPanSourceFile(name, file);
	}

	public SourceFile retrieveTxtSource(String name) {
		File file = sourceLocator.lookup(name, "");
		return createTxtSourceFile(name, file);
	}

	public SourceFile retrieveTxtSource(String name, List<String> loadpath) {
		File file = sourceLocator.lookup(name, "", loadpath);
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

}
