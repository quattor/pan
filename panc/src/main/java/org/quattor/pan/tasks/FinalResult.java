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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.quattor.pan.Compiler;
import org.quattor.pan.cache.Valid2Cache;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.repository.SourceFile;
import org.quattor.pan.ttemplate.IncludeTrace;

/**
 * Encapsulates the results of the second validation phase.
 * 
 * @author loomis
 * 
 */
public class FinalResult extends TaskResult {

	private final Element root;

	public final long timestamp;

	public final String objectName;

	Set<SourceFile> dependencies;
    private final Map<String, IncludeTrace> includeTraces;
    private final List<java.io.File> includeRoots;

	public FinalResult(Compiler compiler, Valid2Result result) {
		super(ResultType.VALID2);

		this.root = result.getRoot();
		this.timestamp = result.timestamp;
		this.objectName = result.objectName;
        Map<String, IncludeTrace> traces = new TreeMap<String, IncludeTrace>();
        if (result.getIncludeTrace() != null) {
            traces.put(objectName, result.getIncludeTrace());
        }
		this.dependencies = resolveAllDependencies(compiler, traces);
        this.includeTraces = Collections.unmodifiableMap(traces);
        this.includeRoots = compiler == null ? Collections.<java.io.File>emptyList() : compiler.options.includeRoots;
	}

	public Element getRoot() {
		return root;
	}

	public Set<SourceFile> getDependencies() {
		return dependencies;
	}

    /** Returns immutable post-validation traces keyed by separate object profile name. */
    public Map<String, IncludeTrace> getIncludeTraces() {
        return includeTraces;
    }

    /** Returns the compiler's shared immutable include root table. */
    public List<java.io.File> getIncludeRoots() {
        return includeRoots;
    }

	public Set<SourceFile> resolveAllDependencies(Compiler compiler) {
        return resolveAllDependencies(compiler, null);
    }

    private Set<SourceFile> resolveAllDependencies(Compiler compiler, Map<String, IncludeTrace> traces) {

		Set<SourceFile> allDependencies = new TreeSet<SourceFile>();

		// Allowing the compiler to be null allows for testing.
		if (compiler != null) {
			Valid2Cache v2cache = compiler.getValid2Cache();

			List<String> processed = new LinkedList<String>();
			Stack<String> unprocessed = new Stack<String>();
			unprocessed.push(objectName);

			// Loop until there are no more unprocessed object templates.
			while (!unprocessed.empty()) {
				String objectToProcess = unprocessed.pop();

				// Only do something if the object template hasn't already been
				// processed.
				if (!processed.contains(objectToProcess)) {
					processed.add(objectToProcess);

					Valid2Result result = (Valid2Result) v2cache
							.waitForResult(objectToProcess);

					allDependencies.addAll(result.getDependencies());
                    if (traces != null && result.getIncludeTrace() != null) {
                        traces.put(objectToProcess, result.getIncludeTrace());
                    }

					unprocessed.addAll(result.getObjectDependencies());
				}

			}
		}

		return Collections.unmodifiableSet(allDependencies);
	}

}
