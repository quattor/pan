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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/tasks/Task.java $
 $Id: Task.java 3604 2008-08-19 10:17:25Z loomis $
 */

package org.quattor.pan.tasks;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Superclass of all <code>Task</code> objects that requires all subclasses to
 * define the its executor type (queue).
 * 
 * @author loomis
 * 
 */
public abstract class Task<T extends TaskResult> extends FutureTask<T> {

	public final TaskResult.ResultType resultType;

	public final String info;

	public Task(TaskResult.ResultType executorType, String info,
			Callable<T> callable) {
		super(callable);
		this.resultType = executorType;
		this.info = info;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + resultType + ", "
				+ Thread.currentThread().getId() + ", " + info + ")";
	}

}
