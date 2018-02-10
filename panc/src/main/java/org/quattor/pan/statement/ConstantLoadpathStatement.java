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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/VariableStatement.java $
 $Id: VariableStatement.java 2728 2008-01-17 20:44:12Z loomis $
 */

package org.quattor.pan.statement;

import java.util.List;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;

public class ConstantLoadpathStatement extends ConstantVariableStatement {

	protected ConstantLoadpathStatement(SourceRange sourceRange, Element value,
			boolean conditional, boolean modifiable) throws SyntaxException {

		super(sourceRange, "LOADPATH", value, conditional, modifiable);
	}

	@Override
	public Element execute(Context context) {

		super.execute(context);

		// Reset the value of the LOADPATH in the context.
		Element element = context.getGlobalVariable("LOADPATH");
		List<String> rpaths = convertLoadpathVariable(element);
		context.setRelativeLoadpaths(rpaths);
        return null;
	}

}
