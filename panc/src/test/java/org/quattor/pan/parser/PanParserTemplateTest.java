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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/parser/PanParserTemplateTest.java $
 $Id: PanParserTemplateTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.parser;

import java.io.StringReader;

import org.junit.Test;

public class PanParserTemplateTest {

	private void parseTemplate(String statement) {
		PanParser parser = new PanParser(new StringReader(statement));
		parser.ttemplate();
	}

	@Test
	public void emptyTemplates() {
		parseTemplate("template test;\n");
		parseTemplate("object template test;\n");
		parseTemplate("unique template test;\n");
		parseTemplate("declaration template test;\n");
		parseTemplate("structure template test;\n");
	}

	@Test
	public void validIncludes() {
		// This statement is now deprecated.
		// parseTemplate("template test; include alpha/beta/gamma;");
		parseTemplate("template test; include {'alpha/beta/gamma'};");
	}

	@Test
	public void validDml() {
		parseTemplate("object template x; '/result' = {2*+3/-2; if (1==1) {value=2} else {value=3};};");
	}

	@Test(expected = ParseException.class)
	public void illegalTemplateFlags() {
		parseTemplate("structure unique template test;\n");
	}

}
