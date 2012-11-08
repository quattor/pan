/*
 Copyright (c) 2006-2012 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.quattor.ant;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.tools.ant.BuildException;

/**
 * Class implements the debug element for the PanCompilerTask object. The debug
 * element takes an include and/or exclude pattern for including or excluding
 * certain templates (based on the namespaced name) from emitting debugging
 * information.
 * 
 * The patterns returned by the getter functions are never null. By default, the
 * DebugPatterns object will accept all template names and exclude none.
 * 
 * @author loomis
 * 
 */
public class DebugPatterns {

	// This pattern matches all template names.
	public final static Pattern ALL = Pattern.compile(".+");

	// This pattern matches only the empty string. As an empty string is an
	// invalid template name, this pattern will never match a valid value.
	public final static Pattern NONE = Pattern.compile("^$");

	private Pattern include;
	private Pattern exclude;

	public DebugPatterns() {
		include = ALL;
		exclude = NONE;
	}

	public Pattern getInclude() {
		return include;
	}

	public void setInclude(String includePattern) {
		include = validatePattern(includePattern);
	}

	public Pattern getExclude() {
		return exclude;
	}

	public void setExclude(String excludePattern) {
		exclude = validatePattern(excludePattern);
	}

	private static Pattern validatePattern(String pattern) {
		try {
			if (pattern != null) {
				return Pattern.compile(pattern);
			} else {
				throw new BuildException("invalid pattern: null");
			}
		} catch (PatternSyntaxException e) {
			throw new BuildException("invalid pattern: " + e.getMessage());
		}
	}

}
