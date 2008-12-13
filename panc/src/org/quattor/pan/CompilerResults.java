/*
 Copyright (c) 2007 Charles A. Loomis, Jr, Cedric Duprilot, and
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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/CompilerResults.java $
 $Id: CompilerResults.java 3614 2008-08-20 15:53:05Z loomis $
 */
package org.quattor.pan;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.quattor.pan.exceptions.SystemException;

/**
 * Encapsulates the statistics and exceptions (or errors) from a compilation.
 * 
 * @author loomis
 * 
 */
public class CompilerResults {

	private CompilerStatistics stats;

	private Set<Throwable> errors;

	/**
	 * Create an object which contains the results of a compilation: the
	 * statistics and the list of exceptions.
	 * 
	 * @param stats
	 *            Statistics for the compilation (may not be null)
	 * @param errors
	 *            Exceptions or errors thrown during the compilation (may be
	 *            null if none were thrown)
	 * 
	 * @throws IllegalArgumentException
	 *             if stats is null
	 */
	public CompilerResults(CompilerStatistics stats, Set<Throwable> errors) {

		this.stats = stats;
		if (stats == null) {
			throw new IllegalArgumentException(
					"compiler statistics may not be null");
		}
		this.errors = Collections.unmodifiableSet(errors);
		if (errors == null) {
			this.errors = new TreeSet<Throwable>();
		}
	}

	/**
	 * Format the exceptions thrown during the compilation process. A null value
	 * will be returned if no exceptions were thrown.
	 * 
	 * @return String containing exceptions thrown during execution or null if
	 *         none were thrown
	 */
	public String formatErrors() {

		if (errors.size() > 0) {

			StringBuilder results = new StringBuilder();

			for (Throwable t : errors) {
				results.append(t.getMessage());
				results.append("\n");
				if (t instanceof NullPointerException) {
					StackTraceElement[] frames = t.getStackTrace();
					if (frames.length > 0) {
						results.append(frames[0].toString());
						results.append("\n");
					}
				} else if (t instanceof SystemException) {
					Throwable cause = t.getCause();
					if (cause != null) {
						results.append(cause.getMessage());
						results.append("\n");
					}
				}
			}
			return results.toString();
		} else {
			return null;
		}
	}

	/**
	 * Format a summary of the compilation statistics and return the summary.
	 * 
	 * @return String containing compiler statistics
	 */
	public String formatStats() {
		return stats.getResults(errors.size());
	}

	/**
	 * Return a list containing all of the errors and exceptions thrown during
	 * processing.
	 * 
	 * @return set of throwables (exceptions and errors)
	 */
	public Set<Throwable> getErrors() {
		return errors;
	}

}
