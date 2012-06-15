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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/parser/PanParserFullTypeTest.java $
 $Id: PanParserFullTypeTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.parser;

import java.io.StringReader;

import org.junit.Test;

public class PanParserFullTypeTest {

	private void parseFullType(String statement) {
		PanParser parser = new PanParser(new StringReader(statement));
		parser.fullTypeSpec();
	}

	@Test
	public void testValidFullType() {
		parseFullType("extensible { 'one' : long(1..10)[]{2..20}**[..300]{3..} = 1 with {return(true)} 'two' ? long include alpha/beta/gamma }(0..10)");
	}

}
