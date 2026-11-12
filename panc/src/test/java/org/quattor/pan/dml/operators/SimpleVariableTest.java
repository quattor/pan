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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/VariableTest.java $
 $Id: VariableTest.java 2659 2008-01-07 14:48:07Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collections;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;
import org.quattor.pan.dml.AbstractOperationTestUtils;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.CompileTimeContext;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.BuildContext;
import org.quattor.pan.ttemplate.LocalVariableMap;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.ttemplate.Template;
import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.dml.functions.Function;

public class SimpleVariableTest extends AbstractOperationTestUtils {

	static final SourceRange RANGE = new SourceRange(3, 4, 3, 8);

	static class TraceContext extends BuildContext {
		int localLookups;
		int globalLookups;

		TraceContext(String objectGlob, String variableGlob) throws Exception {
			super(new Compiler(new CompilerOptions(null, null, 1000, 50,
					Collections.emptySet(), null, Collections.<File>emptyList(),
					CompilerOptions.DeprecationWarnings.OFF, null, null, null, 1,
					objectGlob, variableGlob), Collections.<String>emptyList(),
					Collections.<File>emptyList()), new Template("object"));
			setCurrentTemplate(new Template("caller"));
		}

		@Override public Element getLocalVariable(String name) {
			localLookups++;
			return super.getLocalVariable(name);
		}

		@Override public Element getGlobalVariable(String name) {
			globalLookups++;
			return super.getGlobalVariable(name);
		}
	}

	static JsonObject[] events(ByteArrayOutputStream bytes) throws Exception {
		String text = bytes.toString("UTF-8").trim();
		if (text.isEmpty()) return new JsonObject[0];
		String[] lines = text.split("\n");
		JsonObject[] result = new JsonObject[lines.length];
		for (int i = 0; i < lines.length; i++) {
			assertTrue(lines[i].startsWith("PAN_GLOBAL_TRACE "));
			result[i] = new JsonParser().parse(lines[i].substring(17)).getAsJsonObject();
		}
		return result;
	}

	@Test public void tracesGlobalReadAndLookupAbsenceWithSource() throws Exception {
		TraceContext context = new TraceContext("object", "*");
		context.setGlobalVariable("global", LongProperty.getInstance(42), false);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintStream previous = System.err;
		try {
			System.setErr(new PrintStream(bytes, true, "UTF-8"));
			assertEquals(42L, ((LongProperty) new SimpleVariable(RANGE, "global", false)
					.execute(context)).getValue().longValue());
			assertNull(new SimpleVariable(RANGE, "missing", true).execute(context));
			try {
				new SimpleVariable(RANGE, "missing", false).execute(context);
				fail("undefined variable must fail");
			} catch (EvaluationException expected) { }
		} finally { System.setErr(previous); }
		JsonObject[] e = events(bytes);
		assertEquals(2, e.length);
		assertEquals("read", e[0].get("action").getAsString());
		assertEquals("global", e[0].get("variable").getAsString());
		assertEquals("caller", e[0].get("template").getAsString());
		assertEquals(4, e[0].getAsJsonObject("location").get("begin_column").getAsInt());
		assertFalse(e[0].has("access_path"));
		assertEquals(42, e[0].getAsJsonObject("value").get("value").getAsInt());
		assertEquals("absent", e[1].getAsJsonObject("value").get("type").getAsString());
	}

	@Test public void excludesLocalAndLocalMapShadowRead() throws Exception {
		TraceContext context = new TraceContext("object", "*");
		context.setLocalVariable("local", LongProperty.getInstance(1));
		LocalVariableMap shadow = new LocalVariableMap();
		shadow.put("global", LongProperty.getInstance(2));
		context.setGlobalVariable("global", LongProperty.getInstance(3), false);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintStream previous = System.err;
		try {
			System.setErr(new PrintStream(bytes, true, "UTF-8"));
			new SimpleVariable(RANGE, "local", false).execute(context);
			LocalVariableMap original = context.createLocalVariableMap(null);
			context.restoreLocalVariableMap(shadow);
			assertEquals(2L, ((LongProperty) new SimpleVariable(RANGE, "global", false)
					.execute(context)).getValue().longValue());
			context.restoreLocalVariableMap(original);
		} finally { System.setErr(previous); }
		assertEquals(0, events(bytes).length);
	}

	@Test public void unselectedAndDisabledReadsDoNotPerformExtraLocalLookup() throws Exception {
		for (TraceContext context : new TraceContext[] {
				new TraceContext("object", "other"), new TraceContext("different", "*") }) {
			context.setGlobalVariable("global", LongProperty.getInstance(1), false);
			assertNotNull(new SimpleVariable(RANGE, "global", false).execute(context));
			assertEquals(0, context.localLookups);
			assertEquals(1, context.globalLookups);
		}
	}

	@Test public void functionArgumentsAndBodyUseTheirActualTemplates() throws Exception {
		TraceContext context = new TraceContext("object", "*");
		context.setGlobalVariable("argument", LongProperty.getInstance(1), false);
		context.setGlobalVariable("body", LongProperty.getInstance(2), false);
		SourceRange bodyRange = new SourceRange(9, 2, 9, 5);
		context.setFunction("f", new SimpleVariable(bodyRange, "body", false),
				new Template("definition"), bodyRange);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintStream previous = System.err;
		try {
			System.setErr(new PrintStream(bytes, true, "UTF-8"));
			new Function(RANGE, "f", new SimpleVariable(RANGE, "argument", false)).execute(context);
		} finally { System.setErr(previous); }
		JsonObject[] e = events(bytes);
		assertEquals(2, e.length);
		assertEquals("caller", e[0].get("template").getAsString());
		assertEquals("argument", e[0].get("variable").getAsString());
		assertEquals(4, e[0].getAsJsonObject("location").get("begin_column").getAsInt());
		assertEquals("definition", e[1].get("template").getAsString());
		assertEquals("body", e[1].get("variable").getAsString());
		assertEquals(9, e[1].getAsJsonObject("location").get("begin_line").getAsInt());
	}

	@Test
	public void testSimpleValue() throws SyntaxException {

		Context context = new CompileTimeContext();

		long va = 1L;
		context.setLocalVariable("x", LongProperty.getInstance(va));

		// Execute the operations.
		Operation op = new SimpleVariable(null, "x", false);
		Element r1 = op.execute(context);

		// Pull the value off the stack.
		assertTrue(r1 instanceof LongProperty);
		LongProperty s1 = (LongProperty) r1;
		long sresult = s1.getValue().longValue();
		assertTrue(va == sresult);
	}

	@Test
	public void testHashValue() throws SyntaxException, InvalidTermException {

		Context context = new CompileTimeContext();

		HashResource dict = new HashResource();
		context.setLocalVariable("x", dict);

		// Execute the operations.
		Operation op = new SimpleVariable(null, "x", false);
		Element r1 = op.execute(context);

		// Pull the value off the stack.
		assertTrue(r1 instanceof HashResource);
	}

	@Test
	public void testListValue() throws SyntaxException, InvalidTermException {

		Context context = new CompileTimeContext();

		ListResource list = new ListResource();
		context.setLocalVariable("x", list);

		// Execute the operations.
		Operation op = new SimpleVariable(null, "x", false);
		Element r1 = op.execute(context);

		// Pull the value off the stack.
		assertTrue(r1 instanceof ListResource);

	}

	@Test
	public void testUndefinedLookup() throws SyntaxException {
		runDml(new SimpleVariable(null, "x", true));
	}

	@Test(expected = EvaluationException.class)
	public void testUndefinedVariable() throws SyntaxException {
		runDml(new SimpleVariable(null, "x", false));
	}

}
