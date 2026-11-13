package org.quattor.pan.ttemplate;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.regex.Pattern;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.operators.SimpleVariable;
import org.quattor.pan.statement.VariableStatement;
import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerOptions;
import java.util.Collections;
import org.quattor.pan.utils.Term;

public class GlobalVariableTraceTest {
    private static class NeverRenderList extends ListResource {
        @Override public Resource.Iterator iterator() { fail("snapshot rendered value"); return null; }
        @Override public String toString() { fail("value rendered as text"); return null; }
    }

    private static class NoSnapshotContext extends BuildContext {
        NoSnapshotContext(String objectGlob, String variableGlob) throws Exception {
            super(new Compiler(new CompilerOptions(null, null, 1000, 50,
                    Collections.emptySet(), null, Collections.<File>emptyList(),
                    CompilerOptions.DeprecationWarnings.OFF, null, null, null, 1,
                    objectGlob, variableGlob), Collections.<String>emptyList(), Collections.<File>emptyList()),
                    new Template("object"));
        }
        @Override public JsonObject snapshotGlobalVariableTraceValue(Element value) {
            fail("snapshot requested");
            return null;
        }
    }

    @Test public void disabledAndNonmatchingObjectsAvoidTracerAndValueRendering() throws Exception {
        Field field = BuildContext.class.getDeclaredField("globalVariableTrace");
        field.setAccessible(true);
        for (String objectGlob : new String[] { null, "other" }) {
            NoSnapshotContext context = new NoSnapshotContext(objectGlob, "*");
            assertNull(field.get(context));
            NeverRenderList value = new NeverRenderList();
            context.setGlobalVariable("VALUE", value, false);
            assertNotNull(new SimpleVariable(new SourceRange(1, 1, 1, 1), "VALUE", false).execute(context));
            VariableStatement.getInstance(new SourceRange(1, 1, 1, 1), "VALUE",
                    LongProperty.getInstance(1), true, true).execute(context);
        }
    }

    @Test public void nonmatchingVariableAvoidsValueRendering() throws Exception {
        NoSnapshotContext context = new NoSnapshotContext("object", "selected");
        Field field = BuildContext.class.getDeclaredField("globalVariableTrace");
        field.setAccessible(true);
        assertNotNull(field.get(context));
        context.setGlobalVariable("VALUE", new NeverRenderList(), false);
        assertNotNull(new SimpleVariable(new SourceRange(1, 1, 1, 1), "VALUE", false).execute(context));
        VariableStatement.getInstance(new SourceRange(1, 1, 1, 1), "VALUE",
                LongProperty.getInstance(1), true, true).execute(context);
    }

    @Test public void globMatchesEntireLogicalName() {
        Pattern pattern = GlobalVariableTrace.compileGlob("node/*/x?");
        assertTrue(pattern.matcher("node/a/b/xz").matches());
        assertFalse(pattern.matcher("prefix/node/a/xz").matches());
        assertFalse(pattern.matcher("node/a/x").matches());
        assertTrue(GlobalVariableTrace.compileGlob("**").matcher("a\nb/c").matches());
        assertFalse(GlobalVariableTrace.compileGlob("A*").matcher("abc").matches());
        assertTrue(GlobalVariableTrace.compileGlob("?").matcher("\ud83d\ude00").matches());
        assertFalse(GlobalVariableTrace.compileGlob("?").matcher("ab").matches());
    }

    @Test public void globTreatsRegexSyntaxLiterally() {
        Pattern pattern = GlobalVariableTrace.compileGlob("a.[x]{y}\\+");
        assertTrue(pattern.matcher("a.[x]{y}\\+").matches());
        assertFalse(pattern.matcher("abxxy\\").matches());
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyGlobIsRejected() { GlobalVariableTrace.compileGlob(""); }

    @Test public void emitsTypedSingleLineWithSourcesAndSequence() throws Exception {
        Template object = new Template(new File("/tmp/trace/object.pan"), null,
                Template.TemplateType.OBJECT, "object", null);
        Template caller = new Template(new File("/tmp/trace/included.pan"), null,
                Template.TemplateType.ORDINARY, "included", null);
        GlobalVariableTrace trace = new GlobalVariableTrace(object, "var*");
        assertTrue(trace.matchesVariable("variable"));
        assertFalse(trace.matchesVariable("other"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        try {
            System.setErr(new PrintStream(bytes, true, "UTF-8"));
            trace.read(caller, new SourceRange(1, 2, 3, 4), "variable",
                    new Term[] { StringProperty.getInstance("a\n\\b"), LongProperty.getInstance(2) },
                    TraceValueWriter.snapshot(StringProperty.getInstance("line\nvalue")));
            trace.write(caller, null, "variable", "overwrite", false, true,
                    TraceValueWriter.snapshot(null), TraceValueWriter.snapshot(LongProperty.getInstance(5)));
        } finally { System.setErr(previous); }
        String[] lines = bytes.toString("UTF-8").split("\n");
        assertEquals(2, lines.length);
        assertTrue(lines[0].startsWith("PAN_GLOBAL_TRACE "));
        JsonObject read = new JsonParser().parse(lines[0].substring(17)).getAsJsonObject();
        assertEquals("pan-global-trace", read.get("format").getAsString());
        assertEquals(1, read.get("version").getAsInt());
        assertEquals(1, read.get("sequence").getAsInt());
        assertEquals("object", read.get("object").getAsString());
        assertEquals("/tmp/trace/object.pan", read.get("object_file").getAsString());
        assertEquals("included", read.get("template").getAsString());
        assertEquals("/tmp/trace/included.pan", read.get("template_file").getAsString());
        assertEquals(2, read.getAsJsonObject("location").get("begin_column").getAsInt());
        assertEquals("a\n\\b", read.getAsJsonArray("access_path").get(0).getAsJsonObject().get("value").getAsString());
        assertEquals(2, read.getAsJsonArray("access_path").get(1).getAsJsonObject().get("value").getAsInt());
        assertEquals("line\nvalue", read.getAsJsonObject("value").get("value").getAsString());
        JsonObject write = new JsonParser().parse(lines[1].substring(17)).getAsJsonObject();
        assertEquals(2, write.get("sequence").getAsInt());
        assertTrue(write.get("location").isJsonNull());
        assertEquals("absent", write.getAsJsonObject("old").get("type").getAsString());
        assertTrue(write.get("final").getAsBoolean());
    }

    @Test public void stderrFailureDoesNotEscapeAndSkipUsesExisting() throws Exception {
        Template object = new Template("object");
        GlobalVariableTrace trace = new GlobalVariableTrace(object, "*");
        PrintStream previous = System.err;
        try {
            System.setErr(new PrintStream(new ByteArrayOutputStream()) {
                @Override public void println(String text) { throw new IllegalStateException("failed"); }
            });
            trace.write(object, null, "x", "skip_default", true, false,
                    TraceValueWriter.snapshot(LongProperty.getInstance(1)), null);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            System.setErr(new PrintStream(bytes, true, "UTF-8"));
            trace.write(object, null, "x", "skip_default", true, false,
                    TraceValueWriter.snapshot(LongProperty.getInstance(1)), null);
            JsonObject event = new JsonParser().parse(bytes.toString("UTF-8").substring(17)).getAsJsonObject();
            assertEquals(2, event.get("sequence").getAsInt());
            assertEquals(1, event.getAsJsonObject("existing").get("value").getAsInt());
            assertFalse(event.has("old"));
            assertTrue(event.get("object_file").isJsonNull());
        } finally { System.setErr(previous); }
    }
}
