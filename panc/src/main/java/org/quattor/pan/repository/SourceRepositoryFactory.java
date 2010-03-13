package org.quattor.pan.repository;

import org.quattor.pan.exceptions.ConfigurationException;

public class SourceRepositoryFactory {

	private SourceRepositoryFactory() {
	}

	public static SourceRepository create(ParameterList parameters)
			throws ConfigurationException {

		return FileSystemSourceRepository.getInstance(parameters);
	}

}
