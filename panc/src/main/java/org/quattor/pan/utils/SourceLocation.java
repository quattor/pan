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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/SourceLocation.java $
 $Id: SourceLocation.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.utils;

import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.ttemplate.Template;

/**
 * Represents the source location of a block of characters within a given
 * template. Instances of this class are immutable.
 * 
 * @author loomis
 * 
 */
public class SourceLocation {

	// Safe to expose. A SourceRange is immutable.
	public final SourceRange sourceRange;

	// Save to expose. A Template is immutable.
	public final Template template;

	public SourceLocation(Template template, SourceRange sourceRange) {
		this.template = template;
		this.sourceRange = sourceRange;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("["); //$NON-NLS-1$
		sb.append(template != null ? template.source : "?"); //$NON-NLS-1$
		sb.append(":"); //$NON-NLS-1$
		sb.append(sourceRange != null ? sourceRange.toString() : "?"); //$NON-NLS-1$
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}

}
