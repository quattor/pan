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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/VariableStatementTest.java $
 $Id: VariableStatementTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.statement;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collections;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.output.DepxFormatter;
import org.quattor.pan.output.Formatter;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.ttemplate.BuildContext;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.ttemplate.Template;

public class VariableStatementTest extends StatementTestUtils {

    private static final SourceRange RANGE = new SourceRange(1, 1, 1, 1);

    private static class TestContext extends BuildContext {
        Element result;
        int evaluations;
        boolean fail;
        boolean mutateSelf;

        TestContext(String objectGlob, String variableGlob) throws Exception {
            this(objectGlob, variableGlob, false);
        }

        TestContext(String objectGlob, String variableGlob, boolean depx) throws Exception {
            super(new Compiler(new CompilerOptions(null, null, 1000, 50,
                    depx ? Collections.<Formatter>singleton(DepxFormatter.getInstance()) : Collections.<Formatter>emptySet(),
                    depx ? new File(System.getProperty("user.dir")) : null, Collections.<File>emptyList(),
                    CompilerOptions.DeprecationWarnings.OFF, null, null, null, 1,
                    objectGlob, variableGlob), Collections.<String>emptyList(),
                    Collections.<File>emptyList()), new Template("object"));
            setCurrentTemplate(new Template("current"));
        }

        @Override public Element executeDmlBlock(Operation dml) {
            evaluations++;
            if (fail) throw new EvaluationException("RHS executed");
            if (mutateSelf) resetSelf(result);
            return result;
        }
    }

    @Test public void computedAssignmentAndSkippedDefaultsCountAfterNestedReadSaturatesCapture() throws Exception {
        TestContext context = new TestContext("object", "*", true) {
            @Override public Element executeDmlBlock(Operation dml) {
                for (int i = 0; i < 10000; i++) {
                    traceGlobalVariableRead("nested", RANGE, null, LongProperty.getInstance(i));
                }
                return LongProperty.getInstance(2);
            }
        };
        context.setGlobalVariable("x", LongProperty.getInstance(1), false);
        new ComputedVariableStatement(RANGE, "x", null, false, true).execute(context);
        assertEquals(10000, context.getGlobalVariableTraceCapture().getEvents().size());
        assertEquals(1, context.getGlobalVariableTraceCapture().getDroppedEvents());
        assertFalse(context.isGlobalVariableTracePayloadRequired("x"));
        new ComputedVariableStatement(RANGE, "x", null, true, true).execute(context);
        new ConstantVariableStatement(RANGE, "x", LongProperty.getInstance(3), false, true).execute(context);
        context.traceGlobalVariableRead("nested", RANGE, null, new ListResource() {
            @Override public org.quattor.pan.dml.data.Resource.Iterator iterator() {
                fail("snapshot requested after saturation");
                return null;
            }
        });
        assertEquals(4, context.getGlobalVariableTraceCapture().getDroppedEvents());
    }

    private static JsonObject[] events(ByteArrayOutputStream bytes) throws Exception {
        String text = bytes.toString("UTF-8").trim();
        if (text.isEmpty()) return new JsonObject[0];
        String[] lines = text.split("\n");
        JsonObject[] events = new JsonObject[lines.length];
        for (int i = 0; i < lines.length; i++) {
            assertTrue(lines[i].startsWith("PAN_GLOBAL_TRACE "));
            events[i] = new JsonParser().parse(lines[i].substring(17)).getAsJsonObject();
        }
        return events;
    }

    @Test public void tracesAssignmentsDefaultsAndSelfSnapshot() throws Exception {
        TestContext context = new TestContext("object", "*");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        try {
            System.setErr(new PrintStream(bytes, true, "UTF-8"));
            new ConstantVariableStatement(new SourceRange(1, 2, 1, 4), "x",
                    LongProperty.getInstance(1), true, true).execute(context);
            context.result = LongProperty.getInstance(2);
            context.mutateSelf = true;
            new ComputedVariableStatement(RANGE, "x", null, false, true).execute(context);
            context.fail = true;
            new ComputedVariableStatement(RANGE, "x", null, true, false).execute(context);
            context.setGlobalVariable("undefined", Undef.VALUE, false);
            new ConstantVariableStatement(RANGE, "undefined", Null.VALUE, true, true).execute(context);
            new ConstantVariableStatement(RANGE, "undefined", LongProperty.getInstance(3), true, true).execute(context);
            context.fail = false;
            context.mutateSelf = false;
            context.result = LongProperty.getInstance(4);
            context.setGlobalVariable("computed_undefined", Undef.VALUE, false);
            new ComputedVariableStatement(RANGE, "computed_undefined", null, true, true).execute(context);
        } finally { System.setErr(previous); }
        JsonObject[] e = events(bytes);
        assertEquals(6, e.length);
        assertEquals("assign", e[0].get("action").getAsString());
        assertEquals("absent", e[0].getAsJsonObject("old").get("type").getAsString());
        assertEquals("current", e[0].get("template").getAsString());
        assertEquals(2, e[0].getAsJsonObject("location").get("begin_column").getAsInt());
        assertEquals("overwrite", e[1].get("action").getAsString());
        assertEquals(1, e[1].getAsJsonObject("old").get("value").getAsInt());
        assertEquals(2, e[1].getAsJsonObject("new").get("value").getAsInt());
        assertEquals("skip_default", e[2].get("action").getAsString());
        assertTrue(e[2].get("conditional").getAsBoolean());
        assertTrue(e[2].get("final").getAsBoolean());
        assertTrue(context.retrieveGlobalVariable("x").getFinalFlag());
        assertEquals(2, context.evaluations);
        assertEquals("undef", e[3].getAsJsonObject("old").get("type").getAsString());
        assertEquals("null", e[3].getAsJsonObject("new").get("type").getAsString());
        assertEquals("skip_default", e[4].get("action").getAsString());
        assertEquals("null", e[4].getAsJsonObject("existing").get("type").getAsString());
        assertEquals("assign", e[5].get("action").getAsString());
        assertEquals("undef", e[5].getAsJsonObject("old").get("type").getAsString());
        assertEquals(4, e[5].getAsJsonObject("new").get("value").getAsInt());
    }

    @Test public void computedAssignmentSnapshotsListBeforeInPlaceSelfMutation() throws Exception {
        final ListResource original = new ListResource(new Element[] { LongProperty.getInstance(1) });
        TestContext context = new TestContext("object", "list") {
            @Override public Element executeDmlBlock(Operation dml) {
                evaluations++;
                ListResource self = (ListResource) getSelf();
                assertSame(original, self);
                self.put(0, LongProperty.getInstance(2));
                return self;
            }
        };
        context.setGlobalVariable("list", original, false);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        try {
            System.setErr(new PrintStream(bytes, true, "UTF-8"));
            new ComputedVariableStatement(RANGE, "list", null, false, true).execute(context);
        } finally { System.setErr(previous); }

        JsonObject[] e = events(bytes);
        assertEquals(1, e.length);
        assertEquals("overwrite", e[0].get("action").getAsString());
        assertEquals(1, e[0].getAsJsonObject("old").getAsJsonArray("value")
                .get(0).getAsJsonObject().get("value").getAsInt());
        assertEquals(2, e[0].getAsJsonObject("new").getAsJsonArray("value")
                .get(0).getAsJsonObject().get("value").getAsInt());
        assertEquals(2L, ((LongProperty) ((ListResource) context.getGlobalVariable("list"))
                .iterator().next().getValue()).getValue().longValue());
        assertEquals(1, context.evaluations);
    }

    @Test public void skippedAlreadyFinalDefaultPreservesFinalityAndDoesNotEvaluateRhs() throws Exception {
        TestContext context = new TestContext("object", "final");
        context.setGlobalVariable("final", LongProperty.getInstance(1), true);
        context.fail = true;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        try {
            System.setErr(new PrintStream(bytes, true, "UTF-8"));
            new ComputedVariableStatement(RANGE, "final", null, true, true).execute(context);
        } finally { System.setErr(previous); }

        JsonObject[] e = events(bytes);
        assertEquals(1, e.length);
        assertEquals("skip_default", e[0].get("action").getAsString());
        assertTrue(e[0].get("final").getAsBoolean());
        assertTrue(context.retrieveGlobalVariable("final").getFinalFlag());
        assertEquals(1L, ((LongProperty) context.getGlobalVariable("final")).getValue().longValue());
        assertEquals(0, context.evaluations);
    }

    @Test public void failedWritesAndInternalLookupsDoNotEmitEvents() throws Exception {
        TestContext context = new TestContext("object", "*");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        try {
            System.setErr(new PrintStream(bytes, true, "UTF-8"));
            context.setGlobalVariable("final", LongProperty.getInstance(1), true);
            context.setGlobalVariable("typed", LongProperty.getInstance(1), false);
            for (String name : new String[] { "final", "typed" }) {
                try {
                    new ConstantVariableStatement(RANGE, name, StringProperty.getInstance("x"), false, true)
                            .execute(context);
                    fail("assignment must fail: " + name);
                } catch (EvaluationException expected) { }
            }
            context.fail = true;
            try {
                new ComputedVariableStatement(RANGE, "rhs", null, false, true).execute(context);
                fail("RHS must fail");
            } catch (EvaluationException expected) { }
            context.getGlobalVariable("typed");
            context.removeGlobalVariable("typed");
        } finally { System.setErr(previous); }
        assertEquals(0, events(bytes).length);
    }

    @Test public void loadpathWritesButNotInternalReadsAreTraced() throws Exception {
        TestContext context = new TestContext("object", "LOADPATH");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        try {
            System.setErr(new PrintStream(bytes, true, "UTF-8"));
            new ConstantLoadpathStatement(RANGE, new ListResource(), false, true).execute(context);
            context.result = new ListResource(new Element[] { StringProperty.getInstance("relative") });
            new ComputedLoadpathStatement(RANGE, null, false, true).execute(context);
            new ConstantVariableStatement(RANGE, "other", LongProperty.getInstance(1), false, true).execute(context);
        } finally { System.setErr(previous); }
        JsonObject[] e = events(bytes);
        assertEquals(2, e.length);
        assertEquals("assign", e[0].get("action").getAsString());
        assertEquals("overwrite", e[1].get("action").getAsString());
        assertEquals("relative", context.getRelativeLoadpaths().get(1));
    }

    @Test public void unselectedObjectEmitsNothing() throws Exception {
        TestContext context = new TestContext("different", "*");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        try {
            System.setErr(new PrintStream(bytes, true, "UTF-8"));
            new ConstantVariableStatement(RANGE, "x", LongProperty.getInstance(1), false, true).execute(context);
        } finally { System.setErr(previous); }
        assertEquals(0, events(bytes).length);
    }

    @Test public void explicitReadHookFiltersAndUsesCurrentTemplate() throws Exception {
        TestContext context = new TestContext("object", "selected");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        try {
            System.setErr(new PrintStream(bytes, true, "UTF-8"));
            context.traceGlobalVariableRead("other", RANGE, null, LongProperty.getInstance(1));
            context.traceGlobalVariableRead("selected", RANGE, null, LongProperty.getInstance(2));
        } finally { System.setErr(previous); }
        JsonObject[] e = events(bytes);
        assertEquals(1, e.length);
        assertEquals("read", e[0].get("action").getAsString());
        assertEquals("current", e[0].get("template").getAsString());
        assertEquals(2, e[0].getAsJsonObject("value").get("value").getAsInt());
        assertFalse(new BuildContext().isGlobalVariableTraceSelected("selected"));
    }

	@Test
	public void variableDefinition() throws Exception {

		Context context = setupTemplateToRun2("vs1", "variable x = 1;", false);

		Element result = context.getGlobalVariable("x");
		assertTrue(result != null);
		assertTrue(result instanceof LongProperty);
		assertTrue(((LongProperty) result).getValue().longValue() == 1L);
	}

}
