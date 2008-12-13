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

import java.util.Map;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.template.Template;

/**
 * Encapsulates the results of the second validation phase.
 * 
 * @author loomis
 * 
 */
public class Valid2Result extends TaskResult {

	private final Element root;

	public final long timestamp;

	Map<String, Template> dependencies;

	public Valid2Result(Element root, long timestamp,
			Map<String, Template> dependencies) {
		super(ResultType.VALID2);

		this.root = root;
		this.timestamp = timestamp;
		this.dependencies = dependencies;
	}

	public Element getRoot() {
		return root;
	}

	public Map<String, Template> getDependencies() {
		return dependencies;
	}

}
