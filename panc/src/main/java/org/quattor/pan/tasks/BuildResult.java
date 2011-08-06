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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/tasks/BuildResult.java $
 $Id: BuildResult.java 3199 2008-05-17 15:26:38Z loomis $
 */

package org.quattor.pan.tasks;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.template.Context;

/**
 * Encapsulates the results of a build, which consist of the root element of the
 * machine configuration and, optionally, the context used to build it. The
 * context is usually only saved for testing and debugging. The context should
 * not be routinely saved because it maintains references to a large number of
 * objects and may cause a large increase in the memory utilization.
 * 
 * @author loomis
 * 
 */
public class BuildResult extends TaskResult {

	private final Element root;

	private final Context context;

	public BuildResult(Element root, Context context) {
		super(ResultType.BUILD);

		this.root = root;
		this.context = context;
	}

	public Element getRoot() {
		return root;
	}

	public Context getObjectContext() {
		return context;
	}

}
