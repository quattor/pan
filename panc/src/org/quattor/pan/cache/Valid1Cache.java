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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/cache/ObjectCache.java $
 $Id: ObjectCache.java 3350 2008-07-12 13:21:38Z loomis $
 */

package org.quattor.pan.cache;

import org.quattor.pan.Compiler;
import org.quattor.pan.tasks.Task;
import org.quattor.pan.tasks.TaskResult;
import org.quattor.pan.tasks.Valid1Result;
import org.quattor.pan.tasks.Valid1Task;

/**
 * A global cache of all pan objects that have been through the first
 * validation phase.
 * 
 * @author loomis
 * 
 */
public class Valid1Cache extends AbstractCache<Valid1Result> {

	/**
	 * Creates a new <code>Valid1Cache</code> that has a default, initial 1000 entries.
	 * 
	 * @param compiler
	 *            associated compiler for this cache
	 */
	public Valid1Cache(Compiler compiler) {
		this(compiler, 1000);
	}

	public Valid1Cache(Compiler compiler, int size) {
		super(compiler, size);
	}

	@Override
	protected Task<Valid1Result> createTask(String objectName) {
		return new Valid1Task(compiler, objectName);
	}

	@Override
	protected TaskResult.ResultType getExecutorQueueType() {
		return TaskResult.ResultType.COMPILED;
	}

}
