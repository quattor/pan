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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/FunctionDefinition.java $
 $Id: FunctionDefinition.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.utils;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.ttemplate.Template;

/**
 * Holds a function definition as well as source information about where the
 * function was defined.
 * 
 * @author loomis
 * 
 */
public class FunctionDefinition {

	// SourceRange is immutable and can be exposed.
	public final SourceRange sourceRange;

	// Template is immutable and can be exposed.
	public final Template template;

	// Operation is immutable and can be exposed.
	public final Operation dml;

	public FunctionDefinition(Template template, SourceRange sourceRange,
			Operation dml) {
		this.template = template;
		this.sourceRange = sourceRange;
		this.dml = dml;
	}

	@Override
	public String toString() {
		return template.name + " " + sourceRange + "\n" + dml + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
