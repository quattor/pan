package org.quattor.pan.output;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.repository.SourceFile;
import org.quattor.pan.tasks.FinalResult;
import org.quattor.pan.tasks.Valid2Result;
import org.quattor.pan.ttemplate.IncludeTrace;
import org.quattor.pan.ttemplate.GlobalVariableTraceCapture;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.ttemplate.Template;
import static org.junit.Assert.*;

public class DepxFormatterTest {
    private FinalResult result() throws Exception {
        IncludeTrace.Builder builder = new IncludeTrace.Builder();
        Template root = new Template("root");
        builder.enter(root, null, new SourceRange(2, 3, 4, 5), IncludeTrace.Status.EXECUTED);
        builder.enter(null, root, null, IncludeTrace.Status.SKIPPED_UNIQUE);
        builder.leave(0);
        builder.enter(null, root, null, IncludeTrace.Status.SKIPPED_DECLARATION);
        builder.leave(-1);
        builder.enter(root, null, null, IncludeTrace.Status.CREATE);
        return new FinalResult(null, new Valid2Result("é\"\n\\\t", null,
                Collections.<String>emptySet(), null, builder.snapshot()));
    }

    private String render(FinalResult result) throws Exception {
        StringWriter text = new StringWriter();
        DepxFormatter.getInstance().write(result, new PrintWriter(text));
        return text.toString();
    }

    @Test
    public void emitsFlatVersionedSchemaWithNullsAndAllStatuses() throws Exception {
        String text = render(result());
        JsonObject json = new JsonParser().parse(text).getAsJsonObject();
        assertEquals("depx", json.get("format").getAsString());
        assertEquals(1, json.get("version").getAsInt());
        assertEquals(0, json.getAsJsonArray("roots").size());
        JsonObject profile = json.getAsJsonArray("profiles").get(0).getAsJsonObject();
        assertFalse(profile.has("global_trace"));
        assertEquals("é\"\n\\\t", profile.get("name").getAsString());
        JsonArray sites = profile.getAsJsonArray("sites");
        assertEquals(3, sites.size());
        assertTrue(sites.get(0).getAsJsonObject().get("target").isJsonNull());
        assertTrue(sites.get(0).getAsJsonObject().getAsJsonObject("caller").get("source").isJsonNull());
        JsonObject location = sites.get(0).getAsJsonObject().getAsJsonObject("location");
        assertEquals(2, location.get("begin_line").getAsInt());
        assertEquals(3, location.get("begin_column").getAsInt());
        assertEquals(4, location.get("end_line").getAsInt());
        assertEquals(5, location.get("end_column").getAsInt());
        JsonArray occurrences = profile.getAsJsonArray("occurrences");
        String[] statuses = {"executed", "skipped_unique", "skipped_declaration", "create"};
        for (int i = 0; i < statuses.length; i++) {
            JsonObject occurrence = occurrences.get(i).getAsJsonObject();
            assertEquals(statuses[i], occurrence.get("status").getAsString());
            if (i == 1 || i == 2) { assertEquals(0, occurrence.get("parent").getAsInt()); }
            else { assertTrue(occurrence.get("parent").isJsonNull()); }
        }
        assertEquals(0, json.getAsJsonArray("dependencies").size());
        assertEquals(text, render(result()));
    }

    @Test
    public void writesOrderedCaptureAndIncompletePrefixMetadata() throws Exception {
        GlobalVariableTraceCapture.Builder capture = new GlobalVariableTraceCapture.Builder("*", null, true, 2, 1000);
        capture.add("{\"sequence\":1,\"variable\":\"é\"}");
        capture.add("{\"sequence\":2,\"variable\":\"X\"}");
        capture.drop();
        IncludeTrace trace = new IncludeTrace.Builder().snapshot();
        FinalResult result = new FinalResult(null, new Valid2Result("root", null,
                Collections.<String>emptySet(), null, trace, capture.snapshot()));
        JsonObject profile = new JsonParser().parse(render(result)).getAsJsonObject()
                .getAsJsonArray("profiles").get(0).getAsJsonObject();
        JsonObject value = profile.getAsJsonObject("global_trace");
        assertEquals(1, value.get("version").getAsInt());
        assertEquals("*", value.get("object_glob").getAsString());
        assertEquals("*", value.get("variable_glob").getAsString());
        assertTrue(value.get("selected").getAsBoolean());
        assertFalse(value.get("complete").getAsBoolean());
        assertEquals(1, value.get("dropped_events").getAsInt());
        assertEquals(2, value.getAsJsonObject("limits").get("max_events").getAsInt());
        assertEquals(1000, value.getAsJsonObject("limits").get("max_bytes").getAsInt());
        assertEquals("é", value.getAsJsonArray("events").get(0).getAsJsonObject().get("variable").getAsString());
        assertEquals(2, value.getAsJsonArray("events").get(1).getAsJsonObject().get("sequence").getAsInt());
        try { result.getGlobalVariableTraceCaptures().clear(); fail(); }
        catch (UnsupportedOperationException expected) { }
    }

    @Test
    public void writesFilteredEmptyCapture() throws Exception {
        GlobalVariableTraceCapture capture = new GlobalVariableTraceCapture.Builder("other", "X", false, 5, 100).snapshot();
        FinalResult result = new FinalResult(null, new Valid2Result("root", null,
                Collections.<String>emptySet(), null, new IncludeTrace.Builder().snapshot(), capture));
        JsonObject value = new JsonParser().parse(render(result)).getAsJsonObject().getAsJsonArray("profiles")
                .get(0).getAsJsonObject().getAsJsonObject("global_trace");
        assertFalse(value.get("selected").getAsBoolean());
        assertTrue(value.get("complete").getAsBoolean());
        assertEquals(0, value.get("dropped_events").getAsInt());
        assertEquals(0, value.getAsJsonArray("events").size());
    }

    @Test
    public void sourceLocationsPreserveLiteralPathsAndFirstRootBoundaries() throws Exception {
        File base = new File("/tmp/opencode/é %?# roots");
        File nested = new File(base, "nested %?#");
        SourceFile source = new SourceFile("child", true, new File(nested, "child.pan"));
        JsonObject value = source(source, Arrays.asList(base, nested));
        assertEquals("child", value.get("name").getAsString());
        assertEquals("PAN", value.get("type").getAsString());
        assertEquals(0, value.getAsJsonObject("location").get("root").getAsInt());
        assertEquals("nested %?#", value.getAsJsonObject("location").get("path").getAsString());
        assertEquals("", source(source, Arrays.asList(nested, base)).getAsJsonObject("location").get("path").getAsString());
        assertEquals(source.getLocation().toURI().toASCIIString(), source(source,
                Collections.singletonList(new File("/tmp/opencode/é %?# root"))).getAsJsonObject("location").get("uri").getAsString());
        assertTrue(source(new SourceFile("absent", true, null), Collections.singletonList(base)).get("location").isJsonNull());
    }

    private JsonObject source(SourceFile source, java.util.List<File> roots) throws Exception {
        StringWriter text = new StringWriter();
        com.google.gson.stream.JsonWriter writer = new com.google.gson.stream.JsonWriter(text);
        DepxFormatter.writeSource(writer, source, roots);
        writer.flush();
        return new JsonParser().parse(text.toString()).getAsJsonObject();
    }

    @Test
    public void cliSelectionAndActualCompilerKeepSeparateSortedProfilesAndDependencies() throws Exception {
        for (String source : new String[] {"cmd_option_utils.clj", "settings.clj", "cmd_option.clj"}) {
            clojure.lang.RT.var("clojure.core", "load-file").invoke(
                    new File("src/main/clojure/org/quattor/pan", source).getAbsolutePath());
        }
        Object selected = clojure.lang.RT.var("org.quattor.pan.cmd-option", "str->formatters")
                .invoke("depx,depx.gz");
        Object formatters = ((clojure.lang.IPersistentMap) selected).valAt(clojure.lang.Keyword.intern(null, "formatter"));
        assertTrue(((java.util.Set<?>) formatters).contains(DepxFormatter.getInstance()));
        assertTrue(((java.util.Set<?>) formatters).contains(DepxGzipFormatter.getInstance()));
        for (String removed : new String[] {"depx.json", "depx.json.gz"}) {
            try {
                clojure.lang.RT.var("org.quattor.pan.cmd-option", "str->formatters").invoke(removed);
                fail("Expected unknown formatter: " + removed);
            } catch (clojure.lang.ExceptionInfo expected) {
                assertTrue(expected.getMessage().contains("unknown formatter"));
            }
        }
        final java.util.List<FinalResult> results = Collections.synchronizedList(new java.util.ArrayList<FinalResult>());
        Formatter capture = new Formatter() {
            public java.net.URI getResultURI(String name) { return java.net.URI.create(name + ".capture"); }
            public String getFormatKey() { return "capture"; }
            public boolean requiresIncludeTrace() { return true; }
            public void write(FinalResult result, java.net.URI uri) { results.add(result); }
        };
        File root = new File(System.getProperty("panc.testdir"), "depx");
        CompilerOptions options = new CompilerOptions(null, null, 100, 50, Collections.singleton(capture),
                org.quattor.pan.utils.TestUtils.getTmpdir(), Collections.singletonList(root),
                CompilerOptions.DeprecationWarnings.OFF, null, null, null, 0);
        assertTrue(options.requiresIncludeTrace());
        org.quattor.pan.CompilerResults compiled = org.quattor.pan.Compiler.run(options,
                Collections.singletonList("trace_context"), Collections.<File>emptyList());
        assertTrue(compiled.getErrors().toString(), compiled.getErrors().isEmpty());
        FinalResult result = null;
        for (FinalResult candidate : results) {
            if ("trace_context".equals(candidate.objectName)) { result = candidate; }
        }
        assertNotNull(result);
        JsonObject json = new JsonParser().parse(render(result)).getAsJsonObject();
        assertEquals(root.toPath().toAbsolutePath().normalize().toString(), json.getAsJsonArray("roots").get(0).getAsString());
        JsonArray profiles = json.getAsJsonArray("profiles");
        assertEquals("trace_context", profiles.get(0).getAsJsonObject().get("name").getAsString());
        assertEquals("trace_external", profiles.get(1).getAsJsonObject().get("name").getAsString());
        boolean textDependency = false;
        for (com.google.gson.JsonElement dependency : json.getAsJsonArray("dependencies")) {
            JsonObject source = dependency.getAsJsonObject();
            if ("trace_payload.txt".equals(source.get("name").getAsString())) {
                textDependency = true;
                assertEquals("TEXT", source.get("type").getAsString());
                assertEquals("", source.getAsJsonObject("location").get("path").getAsString());
            }
        }
        assertTrue(textDependency);
        assertEquals(render(result), render(result));
    }

    @Test
    public void registersFormatsAndGzipDecompressesToIdenticalUtf8() throws Exception {
        assertSame(DepxFormatter.getInstance(), FormatterUtils.getFormatterInstance("depx"));
        assertSame(DepxGzipFormatter.getInstance(), FormatterUtils.getFormatterInstance("depx.gz"));
        for (String removed : new String[] {"depx.json", "depx.json.gz"}) {
            assertNull(FormatterUtils.getFormatterInstance(removed));
        }
        assertTrue(CompilerOptions.getFormatters("depx,depx.gz").contains(DepxFormatter.getInstance()));
        assertTrue(CompilerOptions.getFormatters("depx,depx.gz").contains(DepxGzipFormatter.getInstance()));
        assertTrue(DepxFormatter.getInstance().requiresIncludeTrace());
        assertFalse(JsonFormatter.getInstance().requiresIncludeTrace());
        assertEquals("root.depx", DepxFormatter.getInstance().getResultURI("root").toString());
        assertEquals("root.depx.gz", DepxGzipFormatter.getInstance().getResultURI("root").toString());
        File file = File.createTempFile("depx-json", ".gz", org.quattor.pan.utils.TestUtils.getTmpdir());
        try {
            DepxGzipFormatter.getInstance().write(result(), file.toURI());
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            try (GZIPInputStream input = new GZIPInputStream(Files.newInputStream(file.toPath()))) {
                byte[] buffer = new byte[1024];
                int count;
                while ((count = input.read(buffer)) != -1) { bytes.write(buffer, 0, count); }
            }
            assertArrayEquals(render(result()).getBytes(java.nio.charset.StandardCharsets.UTF_8), bytes.toByteArray());
        } finally { Files.deleteIfExists(file.toPath()); }
    }

    @Test
    public void compilerOptionsRejectRemovedFormatsEvenWhenMixedWithValidFormats() {
        for (String removed : new String[] {"depx.json", "depx.json.gz"}) {
            for (String formats : new String[] {removed, "json," + removed, removed + ",depx"}) {
                try {
                    CompilerOptions.getFormatters(formats);
                    fail("Expected rejected formatter: " + formats);
                } catch (IllegalArgumentException expected) {
                    assertTrue(expected.getMessage().contains(removed));
                }
            }
        }
        assertTrue(CompilerOptions.getFormatters("none").isEmpty());
        assertEquals(Collections.singleton(JsonFormatter.getInstance()),
                CompilerOptions.getFormatters("none,json"));
        assertTrue(CompilerOptions.getFormatters("unknown").isEmpty());
    }
}
