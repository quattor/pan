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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/tasks/TaskResult.java $
 $Id: TaskResult.java 3454 2008-07-26 18:51:06Z loomis $
 */

package org.quattor.pan.tasks;

/**
 * Superclass of all task results that allows different tasks to be mixed on a
 * given executor (queue). This class maintains a set of types to allow
 * statistical information about the types of tasks to be collected.
 * 
 * @author loomis
 * 
 */
public class TaskResult {

	/**
	 * Enumerated type of the possible types of results. Used to generate
	 * statistical information about a given run of the compiler.
	 * 
	 * @author loomis
	 * 
	 */
	public static enum ResultType {
		COMPILED, BUILD, VALID1, VALID2, XML, DEP
	}

	public final ResultType type;

	public TaskResult(ResultType type) {
		this.type = type;
	}

}
