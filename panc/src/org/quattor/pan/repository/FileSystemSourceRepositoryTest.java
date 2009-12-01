package org.quattor.pan.repository;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FileSystemSourceRepositoryTest {

	@Test
	public void nullParametersOK() {
		SourceRepository repository;
		repository = FileSystemSourceRepository.getInstance(null);

		notNullAndCorrectType(repository);
	}

	@Test
	public void emptyParametersOK() {
		
		ParameterList parameters = new ParameterList();
		
		SourceRepository repository;
		repository = FileSystemSourceRepository.getInstance(parameters);

		notNullAndCorrectType(repository);
	}

	private void notNullAndCorrectType(SourceRepository repository) {
		assertNotNull(repository);
		assertTrue(repository instanceof FileSystemSourceRepository);
	}

}
