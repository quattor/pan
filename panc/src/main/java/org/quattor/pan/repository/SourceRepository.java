package org.quattor.pan.repository;

import java.util.List;

public interface SourceRepository {

	public SourceFile retrievePanSource(String name);

	public SourceFile retrievePanSource(String name, List<String> loadpath);

	public SourceFile retrieveTxtSource(String fullname);

	public SourceFile retrieveTxtSource(String fullname, List<String> loadpath);

}
