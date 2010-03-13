/*
 Copyright (c) 2006 Charles A. Loomis, Jr, Cedric Duprilot, and
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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/cache/BuildCache.java $
 $Id: BuildCache.java 3604 2008-08-19 10:17:25Z loomis $
 */

package org.quattor.pan.cache;

import static org.quattor.pan.utils.MessageUtils.MSG_CIRCULAR_OBJECT_DEPENDENCY;

import java.util.HashMap;
import java.util.Map;

import org.quattor.pan.Compiler;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.tasks.BuildResult;
import org.quattor.pan.tasks.BuildTask;
import org.quattor.pan.tasks.Task;
import org.quattor.pan.tasks.TaskResult;

/**
 * Contains a global cache of all pan objects that have been compiled and built
 * during a given compiler run. Referencing an object that does not have an
 * entry will create a new build task. Dependencies are checked between object
 * templates and circular dependencies will generate an exception.
 * 
 * @author loomis
 * 
 */
public class BuildCache extends AbstractCache<BuildResult> {

	/**
	 * This map contains dependencies between objects. This map must be
	 * consulted (by the setDependency() method) before queuing a new object
	 * template for a build. This will prevent deadlock from object templates
	 * which have cyclic dependencies.
	 * 
	 * Reference to this map must never leak out of this class. Equally
	 * important, all read and write access to the map must be done from
	 * synchronized methods.
	 */
	private final Map<String, String> dependencies = new HashMap<String, String>();

	/**
	 * Creates a <code>BuildCache</code> with a default, initial size of 1000.
	 * 
	 * @param compiler
	 *            associated compiler for this cache
	 */
	public BuildCache(Compiler compiler) {
		this(compiler, 1000);
	}

	public BuildCache(Compiler compiler, int size) {
		super(compiler, size);
	}

	@Override
	protected Task<BuildResult> createTask(String objectName) {
		return new BuildTask(compiler, objectName);
	}

	@Override
	protected TaskResult.ResultType getExecutorQueueType() {
		return TaskResult.ResultType.BUILD;
	}

	/**
	 * This method will set the given dependency in the map which holds them.
	 * This method will throw an exception if the specified dependency would
	 * create a cycle in the dependency map. In this case, the dependency will
	 * not be inserted.
	 * 
	 * Note: This method MUST be synchronized to ensure that the entire cycle
	 * calculation occurs with the dependency map in a consistent state.
	 * 
	 * @param objectName
	 *            name of the object which has the dependency
	 * @param dependencyName
	 *            name of the object objectName depends on
	 * 
	 * @throws EvaluationException
	 */
	synchronized public void setDependency(String objectName,
			String dependencyName) throws EvaluationException {

		// Determine if adding this dependency will create a cycle.
		String nextObjectName = dependencies.get(dependencyName);
		while (nextObjectName != null) {
			if (objectName.equals(nextObjectName)) {
				throw EvaluationException.create(
						MSG_CIRCULAR_OBJECT_DEPENDENCY, getCycle(objectName,
								dependencyName));
			}
			nextObjectName = dependencies.get(nextObjectName);
		}

		// If we get to here, then we reached the end of the chain without
		// creating a cycle. It is OK to add this to the dependencies.
		dependencies.put(objectName, dependencyName);

		// To avoid deadlock, ensure that the number of available threads is
		// larger than number of entries in the dependencies.
		compiler.ensureMinimumBuildThreadLimit(dependencies.size() + 1);
	}

	/**
	 * This method creates a string describing a cycle which has been detected.
	 * It should only be called if a cycle with the specified dependency has
	 * actually been detected.
	 * 
	 * @param objectName
	 *            name of the object which has the dependency
	 * @param dependencyName
	 *            name of the object objectName depends on
	 * 
	 * @return String describing the cyclic dependency
	 */
	synchronized private String getCycle(String objectName,
			String dependencyName) {

		// Determine if adding this dependency will create a cycle.
		StringBuilder sb = new StringBuilder();
		sb.append(objectName);
		sb.append(" -> ");
		sb.append(dependencyName);

		String nextObjectName = dependencies.get(dependencyName);
		while (nextObjectName != null && !objectName.equals(nextObjectName)) {
			sb.append(" -> ");
			sb.append(nextObjectName);
			nextObjectName = dependencies.get(nextObjectName);
		}

		sb.append(" -> ");
		sb.append(objectName);

		return sb.toString();
	}

}
