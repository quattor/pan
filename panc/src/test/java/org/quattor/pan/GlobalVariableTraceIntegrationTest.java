package org.quattor.pan;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.quattor.pan.output.Formatter;
import org.quattor.pan.output.DepxFormatter;
import org.quattor.pan.output.DepxGzipFormatter;
import org.quattor.pan.tasks.FinalResult;
import org.quattor.pan.ttemplate.GlobalVariableTraceSink;
import org.quattor.pan.utils.TestUtils;

public class GlobalVariableTraceIntegrationTest {
    private JsonObject profile(JsonObject document, String name) {
        for (JsonElement value : document.getAsJsonArray("profiles")) {
            JsonObject profile = value.getAsJsonObject();
            if (name.equals(profile.get("name").getAsString())) return profile;
        }
        throw new AssertionError("Missing profile " + name);
    }

    private CompilerResults compileDepx(File output, String root, String objectGlob,
            String variableGlob, boolean file, String object) throws Exception {
        CompilerOptions options = new CompilerOptions(null, null, 1000, 50,
                new java.util.HashSet<Formatter>(Arrays.<Formatter>asList(
                        DepxFormatter.getInstance(), DepxGzipFormatter.getInstance())), output,
                Collections.singletonList(new File(TestUtils.getTestdir(), root)),
                CompilerOptions.DeprecationWarnings.OFF, null, null, null, 1,
                objectGlob, variableGlob, file);
        return Compiler.run(options, Collections.singletonList(object), Collections.<File>emptyList());
    }

    private JsonObject readDepx(File output, String name) throws Exception {
        return new JsonParser().parse(new String(Files.readAllBytes(new File(output, name + ".depx").toPath()),
                StandardCharsets.UTF_8)).getAsJsonObject();
    }

    @Test public void depxArtifactsPreserveIndependentProfilesOccurrencesAndExplicitStream() throws Exception {
        File output = new File(TestUtils.getTmpdir(), "depx-global-trace-artifacts");
        assertTrue(output.isDirectory() || output.mkdirs());
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        try {
            System.setErr(new PrintStream(stderr, true, "UTF-8"));
            assertNull(compileDepx(output, "depx", "trace_*", null, true, "trace_context").formatErrors());
        } finally { System.setErr(previous); }
        assertFalse(stderr.toString("UTF-8").contains("PAN_GLOBAL_TRACE "));
        File plain = new File(output, "trace_context.depx");
        byte[] bytes = Files.readAllBytes(plain.toPath());
        ByteArrayOutputStream uncompressed = new ByteArrayOutputStream();
        try (GZIPInputStream input = new GZIPInputStream(Files.newInputStream(
                new File(output, "trace_context.depx.gz").toPath()))) {
            byte[] buffer = new byte[4096];
            for (int count; (count = input.read(buffer)) != -1;) uncompressed.write(buffer, 0, count);
        }
        assertArrayEquals(bytes, uncompressed.toByteArray());
        JsonObject document = new JsonParser().parse(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        Map<String, JsonObject> captured = new HashMap<String, JsonObject>();
        for (String name : new String[] {"trace_context", "trace_external"}) {
            JsonObject profile = profile(document, name);
            JsonObject trace = profile.getAsJsonObject("global_trace");
            assertEquals("*", trace.get("variable_glob").getAsString());
            assertTrue(trace.get("selected").getAsBoolean());
            assertTrue(trace.get("complete").getAsBoolean());
            JsonArray events = trace.getAsJsonArray("events");
            assertTrue(events.size() > 0);
            int sequence = 0;
            for (JsonElement value : events) {
                JsonObject event = value.getAsJsonObject();
                assertEquals(++sequence, event.get("sequence").getAsInt());
                assertEquals(name, event.get("object").getAsString());
                if (!event.get("occurrence").isJsonNull()) {
                    assertTrue(event.get("occurrence").getAsInt() < profile.getAsJsonArray("occurrences").size());
                }
                captured.put(name + ":" + sequence, event);
            }
            assertTrue(events.get(0).getAsJsonObject().get("occurrence").isJsonNull());
        }
        JsonArray rootEvents = profile(document, "trace_context").getAsJsonObject("global_trace").getAsJsonArray("events");
        List<Integer> repeated = new ArrayList<Integer>();
        List<Integer> createParents = new ArrayList<Integer>();
        List<Integer> createChildren = new ArrayList<Integer>();
        boolean function = false, includeExpression = false;
        for (JsonElement value : rootEvents) {
            JsonObject event = value.getAsJsonObject();
            if ("read".equals(event.get("action").getAsString())
                    && "ROOT_GLOBAL".equals(event.get("variable").getAsString())) {
                if ("trace_structure".equals(event.get("template").getAsString())) {
                    createParents.add(event.get("occurrence").getAsInt());
                } else if ("trace_structure_leaf".equals(event.get("template").getAsString())) {
                    createChildren.add(event.get("occurrence").getAsInt());
                }
            }
            if ("trace_global_repeat".equals(event.get("template").getAsString())
                    && "ROOT_GLOBAL".equals(event.get("variable").getAsString())) {
                repeated.add(event.get("occurrence").getAsInt());
            }
            if ("trace_functions".equals(event.get("template").getAsString())
                    && "ROOT_GLOBAL".equals(event.get("variable").getAsString())) {
                assertTrue(event.get("occurrence").isJsonNull());
                function = true;
            }
            if ("TARGET".equals(event.get("variable").getAsString())
                    && "read".equals(event.get("action").getAsString())) {
                assertTrue(event.get("occurrence").isJsonNull());
                includeExpression = true;
            }
        }
        assertEquals(2, repeated.size());
        assertNotEquals(repeated.get(0), repeated.get(1));
        assertEquals(4, createParents.size());
        assertEquals(2, createChildren.size());
        for (int i = 0; i < createChildren.size(); i++) {
            assertEquals(createParents.get(i * 2), createParents.get(i * 2 + 1));
            assertNotEquals(createParents.get(i * 2), createChildren.get(i));
            JsonObject child = profile(document, "trace_context").getAsJsonArray("occurrences")
                    .get(createChildren.get(i)).getAsJsonObject();
            assertEquals(createParents.get(i * 2).intValue(), child.get("parent").getAsInt());
        }
        assertTrue(function);
        assertTrue(includeExpression);
        int streamed = 0;
        for (String line : log(output).split("\n")) {
            assertTrue(line.startsWith("PAN_GLOBAL_TRACE "));
            JsonObject event = new JsonParser().parse(line.substring(17)).getAsJsonObject();
            assertEquals(event, captured.get(event.get("object").getAsString() + ":" + event.get("sequence").getAsInt()));
            streamed++;
        }
        assertEquals(captured.size(), streamed);
    }

    @Test public void depxUnselectedProfileAndFailedBuildHaveExpectedCapturePolicy() throws Exception {
        File output = outputDirectory();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        try {
            System.setErr(new PrintStream(stderr, true, "UTF-8"));
            assertNull(compileDepx(output, "depx", "trace_context", "NOT_PRESENT", false, "trace_context").formatErrors());
        } finally { System.setErr(previous); }
        assertFalse(stderr.toString("UTF-8").contains("PAN_GLOBAL_TRACE "));
        assertFalse(new File(output, "trace.log").exists());
        JsonObject selected = profile(readDepx(output, "trace_context"), "trace_context").getAsJsonObject("global_trace");
        assertTrue(selected.get("selected").getAsBoolean());
        assertTrue(selected.get("complete").getAsBoolean());
        assertEquals(0, selected.getAsJsonArray("events").size());
        JsonObject dependency = profile(readDepx(output, "trace_context"), "trace_external").getAsJsonObject("global_trace");
        assertFalse(dependency.get("selected").getAsBoolean());
        assertTrue(dependency.get("complete").getAsBoolean());
        assertEquals(0, dependency.getAsJsonArray("events").size());
        File failed = outputDirectory();
        assertNotNull(compileDepx(failed, "global_variable_trace", "rejected", "X", true, "rejected").formatErrors());
        assertTrue(log(failed).contains("PAN_GLOBAL_TRACE "));
        assertFalse(new File(failed, "rejected.depx").exists());
        assertFalse(new File(failed, "rejected.depx.gz").exists());
    }

    @Test public void depxPreservesIndexedReadsAndExcludesLocalShadows() throws Exception {
        File output = new File(TestUtils.getTmpdir(), "depx-global-trace-artifacts");
        assertTrue(output.isDirectory() || output.mkdirs());
        assertNull(compileDepx(output, "global_variable_trace", "main", "*", false, "main").formatErrors());
        JsonArray events = profile(readDepx(output, "main"), "main").getAsJsonObject("global_trace").getAsJsonArray("events");
        boolean indexed = false;
        for (JsonElement value : events) {
            JsonObject event = value.getAsJsonObject();
            if ("DATA".equals(event.get("variable").getAsString()) && event.has("access_path")) {
                assertEquals(3, event.getAsJsonArray("access_path").size());
                assertEquals(20, event.getAsJsonObject("value").get("value").getAsInt());
                indexed = true;
            }
            if ("main".equals(event.get("template").getAsString())
                    && event.getAsJsonObject("location").get("begin_line").getAsInt() == 23) {
                assertNotEquals("read", event.get("action").getAsString());
            }
        }
        assertTrue(indexed);
    }
    private File outputDirectory() throws Exception {
        return Files.createTempDirectory(TestUtils.getTmpdir().toPath(), "trace-integration-").toFile();
    }

    private CompilerOptions fileOptions(File output, String objectGlob, boolean file) throws Exception {
        return new CompilerOptions(null, null, 1000, 50, Collections.<Formatter>singleton(new ResultsFormatter()),
                output, Collections.singletonList(new File(TestUtils.getTestdir(), "global_variable_trace")),
                CompilerOptions.DeprecationWarnings.OFF, null, null, null, 4, objectGlob, "ITEM", file);
    }

    private String log(File output) throws Exception {
        return new String(Files.readAllBytes(new File(output, "trace.log").toPath()), StandardCharsets.UTF_8);
    }

    @Test public void processOwnsFreshFileAndDoesNotDuplicateTraceOnStderr() throws Exception {
        File output = outputDirectory();
        Files.write(new File(output, "trace.log").toPath(), "old".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        try {
            System.setErr(new PrintStream(errors, true, "UTF-8"));
            Compiler compiler = new Compiler(fileOptions(output, "selected_*", true),
                    Arrays.asList("selected_a", "selected_b"), Collections.<File>emptyList());
            assertNull(compiler.process().formatErrors());
        } finally { System.setErr(previous); }
        assertFalse(errors.toString("UTF-8").contains("PAN_GLOBAL_TRACE "));
        String[] lines = log(output).split("\n");
        assertEquals(4, lines.length);
        for (String line : lines) {
            assertTrue(line.startsWith("PAN_GLOBAL_TRACE "));
            JsonObject event = new JsonParser().parse(line.substring(17)).getAsJsonObject();
            assertEquals("ITEM", event.get("variable").getAsString());
            assertEquals("string", event.getAsJsonObject(event.has("new") ? "new" : "value").get("type").getAsString());
        }
        try (GlobalVariableTraceSink ignored = GlobalVariableTraceSink.open(output)) { }
    }

    @Test public void disabledAndEmptyInvocationsLeaveOrReplaceLogAsAppropriate() throws Exception {
        File output = outputDirectory();
        File trace = new File(output, "trace.log");
        assertNull(new Compiler(fileOptions(output, null, true), Collections.<String>emptyList(), null).process().formatErrors());
        assertFalse(trace.exists());
        Files.write(trace.toPath(), "old".getBytes(StandardCharsets.UTF_8));
        assertNull(new Compiler(fileOptions(output, null, true), Collections.<String>emptyList(), null).process().formatErrors());
        assertEquals("old", log(output));
        assertNull(new Compiler(fileOptions(output, "*", true), Collections.<String>emptyList(), null).process().formatErrors());
        assertEquals("", log(output));
    }

    @Test public void constructionFailureDoesNotTouchExistingLog() throws Exception {
        File output = outputDirectory();
        Files.write(new File(output, "trace.log").toPath(), "old".getBytes(StandardCharsets.UTF_8));
        try {
            new Compiler(fileOptions(output, "*", true), Collections.singletonList("does-not-exist"), null);
            fail("missing object should fail resolution");
        } catch (RuntimeException expected) { }
        assertEquals("old", log(output));
    }

    @Test public void failedCompilationRetainsPartialTraceAndReleasesLock() throws Exception {
        File output = outputDirectory();
        CompilerOptions options = new CompilerOptions(null, null, 1000, 50,
                Collections.<Formatter>singleton(new ResultsFormatter()), output,
                Collections.singletonList(new File(TestUtils.getTestdir(), "global_variable_trace")),
                CompilerOptions.DeprecationWarnings.OFF, null, null, null, 1, "rejected", "X", true);
        assertNotNull(new Compiler(options, Collections.singletonList("rejected"), null).process().formatErrors());
        assertTrue(log(output).contains("PAN_GLOBAL_TRACE "));
        try (GlobalVariableTraceSink ignored = GlobalVariableTraceSink.open(output)) { }
    }

    @Test public void borrowedSinkStaysOpenAndWriteFailureFailsCompilation() throws Exception {
        File output = outputDirectory();
        GlobalVariableTraceSink sink = GlobalVariableTraceSink.open(output);
        try {
            assertNull(Compiler.run(fileOptions(output, "selected_a", true),
                    Collections.singletonList("selected_a"), null, sink).formatErrors());
            assertNull(Compiler.run(fileOptions(output, "selected_b", true),
                    Collections.singletonList("selected_b"), null, sink).formatErrors());
            assertTrue(log(output).contains("selected_a"));
            assertTrue(log(output).contains("selected_b"));
            sink.emit("still open");
        } finally { sink.close(); }
        CompilerResults result = Compiler.run(fileOptions(output, "selected_a", true),
                Collections.singletonList("selected_a"), null, sink);
        assertNotNull(result.formatErrors());
        assertTrue(result.formatErrors().contains("trace"));
        assertTrue(result.formatErrors().contains("cannot write trace file"));
    }

    @Test public void processReportsOpenAndLockFailuresWithoutCorruptingExistingLog() throws Exception {
        File output = outputDirectory();
        try (GlobalVariableTraceSink sink = GlobalVariableTraceSink.open(output)) {
            sink.emit("first record");
            CompilerResults locked = new Compiler(fileOptions(output, "selected_a", true),
                    Collections.singletonList("selected_a"), null).process();
            assertNotNull(locked.formatErrors());
            assertTrue(locked.formatErrors().contains("trace"));
            assertEquals("first record\n", log(output));
        }
        File blocked = outputDirectory();
        assertTrue(new File(blocked, "trace.log").mkdir());
        CompilerResults failed = new Compiler(fileOptions(blocked, "selected_a", true),
                Collections.singletonList("selected_a"), null).process();
        assertNotNull(failed.formatErrors());
        assertTrue(failed.formatErrors().contains("trace"));
    }
    private static class ResultsFormatter implements Formatter {
        final Map<String, String> profiles = Collections.synchronizedMap(new HashMap<String, String>());
        public URI getResultURI(String name) { return URI.create(name + ".trace-test"); }
        public String getFormatKey() { return "trace-test"; }
        public void write(FinalResult result, URI uri) { profiles.put(result.objectName, result.getRoot().toString()); }
    }

    private static class Run {
        final Map<String, String> profiles;
        final String errors;
        final List<JsonObject> events;
        Run(Map<String, String> profiles, String errors, List<JsonObject> events) {
            this.profiles = profiles;
            this.errors = errors;
            this.events = events;
        }
    }

    private Run compile(String objectGlob, String variableGlob, PrintStream stderr, String... names) throws Exception {
        ResultsFormatter formatter = new ResultsFormatter();
        File directory = new File(TestUtils.getTestdir(), "global_variable_trace");
        CompilerOptions options = new CompilerOptions(null, null, 1000, 50,
                Collections.<Formatter>singleton(formatter), TestUtils.getTmpdir(),
                Collections.singletonList(directory), CompilerOptions.DeprecationWarnings.OFF,
                null, null, null, 4, objectGlob, variableGlob);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        CompilerResults result;
        try {
            System.setErr(stderr == null ? new PrintStream(bytes, true, "UTF-8") : stderr);
            result = Compiler.run(options, Arrays.asList(names), Collections.<File>emptyList());
        } finally {
            System.setErr(previous);
        }
        List<JsonObject> events = new ArrayList<JsonObject>();
        for (String line : bytes.toString("UTF-8").split("\n")) {
            if (line.startsWith("PAN_GLOBAL_TRACE ")) {
                events.add(new JsonParser().parse(line.substring(17)).getAsJsonObject());
            }
        }
        return new Run(new HashMap<String, String>(formatter.profiles), result.formatErrors(), events);
    }

    private static List<JsonObject> events(Run run, String variable, String action) {
        List<JsonObject> matches = new ArrayList<JsonObject>();
        for (JsonObject event : run.events) {
            if (variable.equals(event.get("variable").getAsString())
                    && action.equals(event.get("action").getAsString())) matches.add(event);
        }
        return matches;
    }

    private static JsonObject eventAt(Run run, String variable, String action, String template, int line) {
        JsonObject match = null;
        for (JsonObject event : events(run, variable, action)) {
            if (template.equals(event.get("template").getAsString())
                    && event.getAsJsonObject("location").get("begin_line").getAsInt() == line) {
                assertNull("duplicate event at " + template + ":" + line, match);
                match = event;
            }
        }
        assertNotNull("missing " + variable + " " + action + " at " + template + ":" + line, match);
        return match;
    }

    private static int firstDataValue(JsonObject snapshot) {
        return snapshot.getAsJsonArray("value").get(0).getAsJsonObject()
                .getAsJsonArray("value").get(0).getAsJsonObject().getAsJsonObject("value")
                .getAsJsonArray("value").get(0).getAsJsonObject().get("value").getAsInt();
    }

    @Test public void realTemplatesPreserveProfilesAndTraceAssignmentsReadsAndSources() throws Exception {
        Run baseline = compile(null, null, null, "main");
        Run traced = compile("main", "*", null, "main");
        assertNull(baseline.errors);
        assertEquals(baseline.errors, traced.errors);
        assertEquals(baseline.profiles, traced.profiles);
        assertEquals(1, traced.profiles.size());
        assertTrue(baseline.events.isEmpty());
        List<JsonObject> assigns = events(traced, "X", "assign");
        assertEquals(1, assigns.size());
        assertEquals("absent", assigns.get(0).getAsJsonObject("old").get("type").getAsString());
        assertEquals(2, events(traced, "X", "overwrite").get(0).getAsJsonObject("new").get("value").getAsInt());
        assertEquals(1, events(traced, "X", "skip_default").size());
        assertEquals("undef", events(traced, "U", "assign").get(1).getAsJsonObject("old").get("type").getAsString());
        assertEquals(4, events(traced, "U", "assign").get(1).getAsJsonObject("new").get("value").getAsInt());
        assertEquals("null", events(traced, "N", "assign").get(0).getAsJsonObject("new").get("type").getAsString());
        assertEquals(1, events(traced, "N", "skip_default").size());
        assertEquals("null", events(traced, "TEXT", "assign").get(0).getAsJsonObject("new").get("value").getAsString());
        assertEquals("", events(traced, "EMPTY", "assign").get(0).getAsJsonObject("new").get("value").getAsString());
        JsonObject included = events(traced, "INCLUDED", "assign").get(0);
        assertEquals("leaf", included.get("template").getAsString());
        assertTrue(included.get("template_file").getAsString().endsWith("/leaf.pan"));
        assertEquals("main", included.get("object").getAsString());
        assertTrue(included.get("object_file").getAsString().endsWith("/main.pan"));
        List<JsonObject> indexed = events(traced, "DATA", "read");
        int indexedCount = 0;
        for (JsonObject event : indexed) {
            if (event.has("access_path")) {
                indexedCount++;
                assertEquals(3, event.getAsJsonArray("access_path").size());
                assertEquals(20, event.getAsJsonObject("value").get("value").getAsInt());
            }
        }
        assertEquals(1, indexedCount);
        assertEquals(1, events(traced, "INDEX", "read").size());
        assertEquals(1, events(traced, "TARGET", "read").size());
        assertEquals(1, events(traced, "FLAG", "read").size());
        assertEquals(1, events(traced, "OFF", "read").size());
        eventAt(traced, "TARGET", "read", "main", 18);
        eventAt(traced, "FLAG", "read", "main", 21);
        eventAt(traced, "OFF", "read", "main", 22);
        eventAt(traced, "SOURCE", "read", "main", 19);
        eventAt(traced, "SOURCE", "read", "main", 21);
        eventAt(traced, "SOURCE", "read", "main", 22);
        eventAt(traced, "SOURCE", "read", "leaf", 2);
        eventAt(traced, "PATH", "read", "main", 29);
        JsonObject functionRead = eventAt(traced, "SOURCE", "read", "functions", 2);
        assertTrue(functionRead.get("template_file").getAsString().endsWith("/functions.pan"));
        for (JsonObject event : traced.events) {
            if (!"main".equals(event.get("template").getAsString())) continue;
            int line = event.getAsJsonObject("location").get("begin_line").getAsInt();
            assertFalse("skipped default or local shadow emitted a read: " + event,
                    "read".equals(event.get("action").getAsString()) && (line == 5 || line == 23));
            assertFalse("skipped branch emitted a read: " + event,
                    "read".equals(event.get("action").getAsString())
                    && ((line == 21 && "OFF".equals(event.get("variable").getAsString()))
                    || (line == 22 && "FLAG".equals(event.get("variable").getAsString()))));
        }
        int caller = 0, body = 0;
        for (JsonObject event : events(traced, "SOURCE", "read")) {
            if ("functions".equals(event.get("template").getAsString())) body++;
            if ("main".equals(event.get("template").getAsString())) caller++;
        }
        assertEquals(1, body);
        assertEquals(3, caller);
        JsonObject firstData = events(traced, "DATA", "assign").get(0).getAsJsonObject("new");
        JsonObject before = eventAt(traced, "DATA", "read", "main", 24).getAsJsonObject("value");
        JsonObject overwrite = events(traced, "DATA", "overwrite").get(0);
        JsonObject after = eventAt(traced, "DATA", "read", "main", 26).getAsJsonObject("value");
        assertEquals(10, firstDataValue(firstData));
        assertEquals(10, firstDataValue(before));
        assertEquals(10, firstDataValue(overwrite.getAsJsonObject("old")));
        assertEquals(42, firstDataValue(overwrite.getAsJsonObject("new")));
        assertEquals(42, firstDataValue(after));
    }

    @Test public void filteringSuppressesConditionalAndIncludeReads() throws Exception {
        Run baseline = compile(null, null, null, "main");
        Run filtered = compile("main", "SOURCE", null, "main");
        assertNull(filtered.errors);
        assertEquals(baseline.errors, filtered.errors);
        assertEquals(baseline.profiles, filtered.profiles);
        assertFalse(filtered.events.isEmpty());
        for (JsonObject event : filtered.events) assertEquals("SOURCE", event.get("variable").getAsString());
        assertTrue(events(filtered, "TARGET", "read").isEmpty());
        assertTrue(events(filtered, "FLAG", "read").isEmpty());
        assertTrue(events(filtered, "OFF", "read").isEmpty());
    }

    @Test public void defaultVariableGlobSelectsAllAndLogicalObjectNameIncludesNamespace() throws Exception {
        Run baseline = compile(null, null, null, "ns/qualified");
        Run traced = compile("ns/*", null, null, "ns/qualified");
        assertNull(traced.errors);
        assertEquals(baseline.errors, traced.errors);
        assertEquals(baseline.profiles, traced.profiles);
        assertEquals(1, events(traced, "FIRST", "assign").size());
        assertEquals(1, events(traced, "SECOND", "assign").size());
        for (JsonObject event : traced.events) {
            assertEquals("ns/qualified", event.get("object").getAsString());
            assertEquals("ns/qualified", event.get("template").getAsString());
            assertTrue(event.get("object_file").getAsString().endsWith("/ns/qualified.pan"));
        }
        assertTrue(compile("qualified", null, null, "ns/qualified").events.isEmpty());
    }

    @Test public void acceptedAndRejectedReplacementsMatchDisabledCompilation() throws Exception {
        for (String name : new String[] { "replacement", "rejected" }) {
            Run baseline = compile(null, null, null, name);
            Run traced = compile(name, "*", null, name);
            assertEquals(baseline.errors, traced.errors);
            assertEquals(baseline.profiles, traced.profiles);
            if ("rejected".equals(name)) {
                assertNotNull(traced.errors);
                assertTrue(events(traced, "X", "overwrite").isEmpty());
            } else {
                assertNull(traced.errors);
                assertEquals("undef", events(traced, "X", "assign").get(1).getAsJsonObject("old").get("type").getAsString());
            }
        }
    }

    @Test public void parallelObjectsEmitCompleteIsolatedSequences() throws Exception {
        String[] names = { "selected_a", "selected_b", "unselected" };
        Run baseline = compile(null, null, null, names);
        Run traced = compile("selected_*", "ITEM", null, names);
        assertNull(traced.errors);
        assertEquals(baseline.errors, traced.errors);
        assertEquals(baseline.profiles, traced.profiles);
        Map<String, Integer> sequences = new HashMap<String, Integer>();
        for (JsonObject event : traced.events) {
            String object = event.get("object").getAsString();
            assertTrue(object.startsWith("selected_"));
            assertEquals("ITEM", event.get("variable").getAsString());
            int sequence = sequences.containsKey(object) ? sequences.get(object) + 1 : 1;
            assertEquals(sequence, event.get("sequence").getAsInt());
            assertEquals(object, event.get("template").getAsString());
            assertTrue(event.get("object_file").getAsString().endsWith("/" + object + ".pan"));
            assertEquals(sequence == 1 ? "assign" : "read", event.get("action").getAsString());
            JsonObject value = event.getAsJsonObject(sequence == 1 ? "new" : "value");
            assertEquals(object.equals("selected_a") ? "a" : "b", value.get("value").getAsString());
            sequences.put(object, sequence);
        }
        assertEquals(Integer.valueOf(2), sequences.get("selected_a"));
        assertEquals(Integer.valueOf(2), sequences.get("selected_b"));
        assertEquals(2, sequences.size());
    }

    @Test public void stderrFailureDoesNotAlterSuccessfulCompilation() throws Exception {
        Run baseline = compile(null, null, null, "selected_a");
        PrintStream broken = new PrintStream(new ByteArrayOutputStream()) {
            @Override public void println(String text) { throw new IllegalStateException("failed"); }
        };
        Run traced = compile("selected_a", "*", broken, "selected_a");
        assertNull(traced.errors);
        assertEquals(baseline.errors, traced.errors);
        assertEquals(baseline.profiles, traced.profiles);
    }
}
