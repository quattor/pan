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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/parser/PanParserStatementTest.java $
 $Id: PanParserStatementTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.parser;

import java.io.StringReader;

import org.junit.Test;

public class PanParserStatementTest {

	private void parseStatement(String statement) {
		PanParser parser = new PanParser(new StringReader(statement));
		parser.statement();
	}

	@Test
	public void testValidStatements() {

		// Check empty statement.
		parseStatement(";");

		// Include statements.
		// These statements are not deprecated.
		// parseStatement("include alpha;");
		// parseStatement("include alpha/beta;");
		parseStatement("include {'alpha'};");
		parseStatement("include {null};");
		parseStatement("include {undef};");

		// Validation statements.
		parseStatement("valid '/alpha/beta' = true;");
		parseStatement("valid '/alpha/beta' = {false};");

		// Bind statements.
		parseStatement("bind '/alpha/beta' = string = 'default' with {true};");
		parseStatement("bind '/alpha/beta' = string = 'default';");
		parseStatement("bind '/alpha/beta' = string with {true};");
		parseStatement("bind '/alpha/beta' = string;");

		// These statements are now deprecated.
		//parseStatement("type '/alpha/beta' = string = 'default' with {true};")
		// ;
		// parseStatement("type '/alpha/beta' = string = 'default';");
		// parseStatement("type '/alpha/beta' = string with {true};");
		// parseStatement("type '/alpha/beta' = string;");

		// Delete statements.
		parseStatement("'/alpha/beta' = null;");

		// Assignment statements.
		parseStatement("'/alpha/beta' = 1;");
		parseStatement("'/alpha/beta' ?= 1;");
		parseStatement("final '/alpha/beta' = 1;");
		parseStatement("final '/alpha/beta' ?= 1;");

		// Global variable.
		parseStatement("variable x = 1;");
		parseStatement("final variable x = 1;");
		parseStatement("variable x = 1;");
		parseStatement("final variable x = 1;");

		parseStatement("variable x ?= 1;");
		parseStatement("final variable x ?= 1;");
		parseStatement("variable x ?= 1;");
		parseStatement("final variable x ?= 1;");

		// Type definitions.
		parseStatement("type x = string = 'default' with {true};");
		parseStatement("type x = string = 'default';");
		parseStatement("type x = string with {true};");
		parseStatement("type x = string;");

		// Function definition.
		parseStatement("function x = {'result'};");
	}

	@Test(expected = ParseException.class)
	public void illegalInclude1() {
		parseStatement("include 'alpha';");
	}

	@Test(expected = ParseException.class)
	public void illegalValidStatement() {
		parseStatement("valid /alpha/beta = true;");
	}

	@Test(expected = ParseException.class)
	public void illegalBind4() {
		parseStatement("bind /alpha/beta = string;");
	}

	@Test(expected = ParseException.class)
	public void illegalTypeBind4() {
		parseStatement("type /alpha/beta = string;");
	}

	@Test(expected = ParseException.class)
	public void illegalDelete() {
		parseStatement("/alpha/beta = null;");
	}

	@Test(expected = ParseException.class)
	public void illegalAssignment1() {
		parseStatement("/alpha/beta = 1;");
	}

	@Test(expected = ParseException.class)
	public void illegalAssignment2() {
		parseStatement("/alpha/beta ?= 1;");
	}

	@Test(expected = ParseException.class)
	public void illegalAssignment3() {
		parseStatement("final /alpha/beta = 1;");
	}

	@Test(expected = ParseException.class)
	public void illegalAssignment4() {
		parseStatement("final /alpha/beta ?= 1;");
	}

	@Test(expected = ParseException.class)
	public void illegalGlobalVariable1() {
		parseStatement("variable 'x' = 1;");
	}

	@Test(expected = ParseException.class)
	public void illegalGlobalVariable2() {
		parseStatement("final variable 'x' = 1;");
	}

	@Test(expected = ParseException.class)
	public void illegalGlobalVariable3() {
		parseStatement("variable 'x' = 1;");
	}

	@Test(expected = ParseException.class)
	public void illegalGlobalVariable4() {
		parseStatement("final variable 'x' = 1;");
	}

	@Test(expected = ParseException.class)
	public void illegalGlobalVariable6() {
		parseStatement("variable 'x' ?= 1;");
	}

	@Test(expected = ParseException.class)
	public void illegalGlobalVariable7() {
		parseStatement("final variable 'x' ?= 1;");
	}

	@Test(expected = ParseException.class)
	public void illegalGlobalVariable8() {
		parseStatement("variable 'x' ?= 1;");
	}

	@Test(expected = ParseException.class)
	public void illegalGlobalVariable9() {
		parseStatement("final variable 'x' ?= 1;");
	}

	@Test(expected = ParseException.class)
	public void illegalGlobalVariable10() {
		parseStatement("final define variable x ?= 1;");
	}

	@Test(expected = ParseException.class)
	public void illegalType6() {
		parseStatement("type x = string with {true} = 'default';");
	}

	@Test(expected = ParseException.class)
	public void illegalDefineType6() {
		parseStatement("type x = string with {true} = 'default';");
	}

	@Test(expected = ParseException.class)
	public void illegalFunction1() {
		parseStatement("function 'x' = {'result'};");
	}

}
