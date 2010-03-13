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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/IsNull.java $
 $Id: IsNull.java 3107 2008-04-07 07:03:42Z loomis $
 */

package org.quattor.pan.dml.functions;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.SourceRange;

/**
 * Wrapper that creates an is_null function call from the IsOfType class.
 * 
 * @author loomis
 * 
 */
final public class IsNull {

	private IsNull() {
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		return IsOfType.getInstance(sourceRange, Null.class, "is_null",
				operations);
	}
}
