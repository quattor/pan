package org.quattor.pan;

import static org.junit.Assert.*;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;
import org.junit.Test;
import org.quattor.pan.output.Formatter;
import org.quattor.pan.tasks.FinalResult;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.statement.StaticIncludeStatement;
import org.quattor.pan.ttemplate.BuildContext;
import org.quattor.pan.ttemplate.IncludeTrace;
import org.quattor.pan.ttemplate.Template;
import org.quattor.pan.ttemplate.SourceRange;

public class IncludeTraceTest {
    private Compiler compiler(Formatter formatter) throws Exception {
        File directory = new File(System.getProperty("panc.testdir"), "depx");
        CompilerOptions options = new CompilerOptions(null, null, 100, 50,
                Collections.singleton(formatter), org.quattor.pan.utils.TestUtils.getTmpdir(),
                Collections.singletonList(directory), CompilerOptions.DeprecationWarnings.OFF,
                null, null, null, 0);
        return new Compiler(options, Collections.<String>emptyList(), Collections.<File>emptyList());
    }

    private static class TraceFormatter implements Formatter {
        final List<FinalResult> results = Collections.synchronizedList(new ArrayList<FinalResult>());
        public URI getResultURI(String name) { return URI.create(name + ".trace"); }
        public String getFormatKey() { return "test-trace"; }
        public boolean requiresIncludeTrace() { return true; }
        public void write(FinalResult result, URI uri) { results.add(result); }
    }

    @Test
    public void functionExecuteAttributesDirectIncludeToDefiningTemplate() throws Exception {
        final BuildContext context = new BuildContext(compiler(new TraceFormatter()), new Template("root"));
        Template root = new Template("root");
        final Template defining = new Template("foreign");
        final SourceRange range = new SourceRange(1, 1, 1, 10);
        final StaticIncludeStatement include = new StaticIncludeStatement(range, "trace_leaf");
        context.setCurrentTemplate(root);
        org.quattor.pan.dml.Operation body = new org.quattor.pan.dml.AbstractOperation(range) {
            public org.quattor.pan.dml.data.Element execute(org.quattor.pan.ttemplate.Context active) {
                assertSame(defining, active.getCurrentTemplate());
                include.execute(active);
                return null;
            }
        };
        context.setFunction("foreign_include", body, defining, range);
        new org.quattor.pan.dml.functions.Function(range, "foreign_include").execute(context);
        IncludeTrace trace = context.getIncludeTrace();
        assertEquals(1, trace.size());
        assertSame(defining, trace.getMetadata().get(0).caller);
        assertEquals("trace_leaf", trace.getMetadata().get(0).target.name);
        assertSame(root, context.getCurrentTemplate());
    }

    @Test
    public void functionCreateAndValidationKeepActualCallersAndExternalProfiles() throws Exception {
        TraceFormatter formatter = new TraceFormatter();
        Compiler compiler = compiler(formatter);
        CompilerResults compiled = Compiler.run(compiler.options,
                Collections.singletonList("trace_context"), Collections.<File>emptyList());
        assertTrue(compiled.getErrors().toString(), compiled.getErrors().isEmpty());
        FinalResult result = null;
        for (FinalResult candidate : formatter.results) {
            if (candidate.objectName.equals("trace_context")) { result = candidate; }
        }
        assertNotNull(result);
        IncludeTrace trace = result.getIncludeTraces().get("trace_context");
        IncludeTrace external = result.getIncludeTraces().get("trace_external");
        assertNotNull(trace);
        assertNotNull(external);
        assertNotSame(trace, external);
        assertEquals(2, result.getIncludeTraces().size());
        assertEquals(2, external.size());
        int creates = 0;
        int structureIncludes = 0;
        int computed = 0;
        for (int i = 0; i < trace.size(); i++) {
            IncludeTrace.Metadata site = trace.getMetadata().get(trace.getMetadataIndex(i));
            assertNotNull(site.sourceRange);
            if (trace.getStatus(i) == IncludeTrace.Status.CREATE) {
                assertEquals("trace_functions", site.caller.name);
                assertEquals("trace_structure", site.target.name);
                creates++;
            } else if (site.target.name.equals("trace_structure_leaf")) {
                assertEquals("trace_structure", site.caller.name);
                assertEquals(IncludeTrace.Status.CREATE, trace.getStatus(trace.getParentIndex(i)));
                structureIncludes++;
            } else if (site.target.name.equals("trace_declaration")) {
                assertEquals("trace_functions", site.caller.name);
            } else if (site.target.name.equals("trace_leaf")) {
                assertEquals("trace_context", site.caller.name);
                computed++;
            }
            assertFalse(site.target.name.equals("not_encountered"));
        }
        assertEquals(2, creates);
        assertEquals(2, structureIncludes);
        assertEquals(1, computed);
        try { result.getIncludeTraces().clear(); fail(); }
        catch (UnsupportedOperationException expected) { }
    }

    @Test
    public void failedResolutionAndInvalidIncludeTypeDoNotCreateOccurrences() throws Exception {
        BuildContext context = new BuildContext(compiler(new TraceFormatter()), new Template("test_root"));
        context.setCurrentTemplate(new Template("test_root"));
        for (String name : new String[] { "missing_template", "trace_external" }) {
            try {
                new StaticIncludeStatement(new SourceRange(1, 1, 1, 10), name).execute(context);
                fail("include should fail: " + name);
            } catch (EvaluationException expected) {
                assertEquals(0, context.getIncludeTrace().size());
            }
        }
    }

    @Test
    public void occurrenceStorageIsPrimitiveAndDisabledBuilderIsAbsent() throws Exception {
        assertEquals(int[].class, IncludeTrace.class.getDeclaredField("sites").getType());
        assertEquals(int[].class, IncludeTrace.class.getDeclaredField("parents").getType());
        assertEquals(byte[].class, IncludeTrace.class.getDeclaredField("statuses").getType());
        Field builder = BuildContext.class.getDeclaredField("includeTrace");
        builder.setAccessible(true);
        assertNull(builder.get(new BuildContext()));
    }

    @Test
    public void repeatedIncludesRetainChildrenAndSuppressedReferencesAreLeaves() throws Exception {
        final List<FinalResult> results = new ArrayList<FinalResult>();
        Formatter formatter = new Formatter() {
            public URI getResultURI(String name) { return URI.create(name + ".trace"); }
            public String getFormatKey() { return "test-trace"; }
            public boolean requiresIncludeTrace() { return true; }
            public void write(FinalResult result, URI uri) { results.add(result); }
        };
        File directory = new File(System.getProperty("panc.testdir"), "depx");
        CompilerOptions options = new CompilerOptions(null, null, 100, 50,
                Collections.singleton(formatter), org.quattor.pan.utils.TestUtils.getTmpdir(),
                Collections.singletonList(directory), CompilerOptions.DeprecationWarnings.OFF,
                null, null, null, 0);
        CompilerResults compiled = Compiler.run(options, Collections.singletonList("trace_root"),
                Collections.<File>emptyList());
        assertTrue(compiled.getErrors().toString(), compiled.getErrors().isEmpty());
        assertEquals(1, results.size());
        IncludeTrace trace = results.get(0).getIncludeTraces().get("trace_root");
        assertNotNull(trace);
        assertEquals(9, trace.size());
        assertEquals(-1, trace.getParentIndex(0));
        assertEquals(0, trace.getParentIndex(1));
        assertEquals(2, trace.getParentIndex(3));
        assertEquals(trace.getMetadataIndex(1), trace.getMetadataIndex(3));
        assertEquals(IncludeTrace.Status.SKIPPED_UNIQUE, trace.getStatus(6));
        assertEquals(IncludeTrace.Status.SKIPPED_DECLARATION, trace.getStatus(8));
        assertEquals(IncludeTrace.Status.EXECUTED, trace.getStatus(4));
        assertEquals(IncludeTrace.Status.EXECUTED, trace.getStatus(7));
        for (int i = 0; i < trace.size(); i++) {
            assertNotEquals(6, trace.getParentIndex(i));
            assertNotEquals(8, trace.getParentIndex(i));
        }
    }

    @Test
    public void compactSnapshotsShareMetadataAndRemainImmutable() throws Exception {
        IncludeTrace.Builder builder = new IncludeTrace.Builder();
        Template caller = new Template("caller");
        Template target = new Template("target");
        int first = builder.enter(caller, target, null, IncludeTrace.Status.EXECUTED);
        builder.leave(-1);
        builder.enter(caller, target, null, IncludeTrace.Status.SKIPPED_UNIQUE);
        IncludeTrace snapshot = builder.snapshot();
        assertEquals(2, snapshot.size());
        assertEquals(1, snapshot.getMetadata().size());
        assertEquals(snapshot.getMetadataIndex(first), snapshot.getMetadataIndex(1));
        assertSame(caller, snapshot.getMetadata().get(0).caller);
        assertSame(target, snapshot.getMetadata().get(0).target);
        builder.enter(null, null, null, IncludeTrace.Status.EXECUTED);
        assertEquals(2, snapshot.size());
        try { snapshot.getMetadata().clear(); fail(); }
        catch (UnsupportedOperationException expected) { }
    }

    @Test
    public void collectionIsDisabledWithoutCapability() {
        assertNull(new BuildContext().getIncludeTrace());
    }

    @Test
    public void existingFormatterLeavesCompilerCollectionDisabled() throws Exception {
        Compiler compiler = compiler(org.quattor.pan.output.PanFormatter.getInstance());
        assertFalse(compiler.options.requiresIncludeTrace());
        BuildContext context = new BuildContext(compiler, new Template("test_root"));
        Field builder = BuildContext.class.getDeclaredField("includeTrace");
        builder.setAccessible(true);
        assertNull(builder.get(context));
        assertNull(context.getIncludeTrace());
    }

    @Test
    public void formatterCapabilityEnablesCollection() throws Exception {
        Formatter formatter = new Formatter() {
            public URI getResultURI(String name) { return URI.create(name + ".trace"); }
            public String getFormatKey() { return "test-trace"; }
            public boolean requiresIncludeTrace() { return true; }
            public void write(FinalResult result, URI uri) { }
        };
        CompilerOptions options = new CompilerOptions(null, null, 100, 50,
                Collections.singleton(formatter), new File("target").getAbsoluteFile(),
                Collections.<File>emptyList(), CompilerOptions.DeprecationWarnings.OFF,
                null, null, null, 0);
        assertTrue(options.requiresIncludeTrace());
    }
}
