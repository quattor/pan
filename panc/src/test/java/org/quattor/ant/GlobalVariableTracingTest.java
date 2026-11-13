package org.quattor.ant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.ttemplate.GlobalVariableTraceSink;
import org.quattor.pan.utils.TestUtils;

public class GlobalVariableTracingTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder(TestUtils.getTmpdir());

    private PanCompilerTask task(File output, String files) {
        Project project = new Project();
        project.init();
        PanCompilerTask task = new PanCompilerTask();
        task.setProject(project);
        task.setOutputDir(output);
        task.setFormats("pan,dep");
        task.setBatchSize(1);
        task.setCheckDependencies(false);
        FileSet fileSet = new FileSet();
        File templates = new File(TestUtils.getTestdir(), "global_variable_trace");
        fileSet.setDir(templates);
        fileSet.setIncludes(files);
        task.addConfiguredFileSet(fileSet);
        task.addConfiguredPath(new Path(project, templates.getAbsolutePath()));
        return task;
    }

    private String log(File output) throws Exception {
        return new String(Files.readAllBytes(new File(output, "trace.log").toPath()), StandardCharsets.UTF_8);
    }

    @Test
    public void testBatchesShareSinkAndNextExecutionReplacesLog() throws Exception {
        File output = temporary.newFolder();
        PanCompilerTask task = task(output, "selected_a.pan,selected_b.pan");
        task.setTraceObjectGlob("*");
        task.setTraceFile(true);
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        try {
            System.setErr(new PrintStream(stderr, true, "UTF-8"));
            task.execute();
        } finally {
            System.setErr(previous);
        }
        assertFalse(stderr.toString("UTF-8").contains("PAN_GLOBAL_TRACE "));
        String first = log(output);
        assertTrue(first.contains("PAN_GLOBAL_TRACE "));
        assertTrue(first.contains("selected_a"));
        assertTrue(first.contains("selected_b"));
        Files.write(new File(output, "trace.log").toPath(), "old record\n".getBytes(StandardCharsets.UTF_8));
        task.execute();
        String second = log(output);
        assertFalse(second.contains("old record"));
        assertTrue(second.contains("selected_a"));
        assertTrue(second.contains("selected_b"));
    }

    @Test
    public void testDisabledAndFileFlagWithoutObjectSelectionDoNotTouchLog() throws Exception {
        File output = temporary.newFolder();
        File trace = new File(output, "trace.log");
        PanCompilerTask task = task(output, "selected_a.pan");
        task.execute();
        assertFalse(trace.exists());
        task.setTraceFile(true);
        task.execute();
        assertFalse(trace.exists());
        Files.write(trace.toPath(), "previous".getBytes(StandardCharsets.UTF_8));
        assertEquals("previous", log(output));
        task.execute();
        assertEquals("previous", log(output));
    }

    @Test
    public void testNoOutdatedObjectsStillStartsFreshTrace() throws Exception {
        File output = temporary.newFolder();
        PanCompilerTask task = task(output, "selected_a.pan");
        task.execute();
        Files.write(new File(output, "trace.log").toPath(), "previous".getBytes(StandardCharsets.UTF_8));
        task.setCheckDependencies(true);
        task.setTraceObjectGlob("*");
        task.setTraceFile(true);
        task.execute();
        assertEquals("", log(output));
    }

    @Test
    public void testFailedCompilationReleasesSink() throws Exception {
        File output = temporary.newFolder();
        PanCompilerTask task = task(output, "rejected.pan");
        task.setTraceObjectGlob("*");
        task.setTraceFile(true);
        try {
            task.execute();
            fail("Expected compilation failure");
        } catch (BuildException expected) {
            assertTrue(expected.getMessage().contains("Compilation failed"));
        }
        assertTrue(log(output).contains("rejected"));
        try (GlobalVariableTraceSink ignored = GlobalVariableTraceSink.open(output)) { }
    }

    @Test
    public void testTraceOpenFailureHasCauseAndDoesNotTruncateLockedLog() throws Exception {
        File output = temporary.newFolder();
        PanCompilerTask task = task(output, "selected_a.pan");
        task.setTraceObjectGlob("*");
        task.setTraceFile(true);
        try (GlobalVariableTraceSink sink = GlobalVariableTraceSink.open(output)) {
            sink.emit("previous");
            try {
                task.execute();
                fail("Expected locked trace failure");
            } catch (BuildException expected) {
                assertNotNull(expected.getCause());
                assertTrue(expected.getMessage().contains("trace"));
            }
            assertEquals("previous\n", log(output));
        }
    }

    @Test
    public void testFileFlagRequiresOutputDirectory() {
        PanCompilerTask task = new PanCompilerTask();
        task.setFormats("none");
        task.setTraceFile(true);
        try {
            task.execute();
            fail("Expected missing output directory failure");
        } catch (BuildException expected) {
            assertTrue(expected.getMessage().contains("outputDirectory"));
            assertNotNull(expected.getCause());
        }
    }
    @Test
    public void testAntGlobSelectionUsesCompilerOptions() {
        PanCompilerTask task = new PanCompilerTask();
        task.setFormats("none");
        task.setTraceObjectGlob("site/[x].?*");
        task.setTraceVariableGlob("a.[x]?*");
        CompilerOptions options = task.createCompilerOptions();
        assertTrue(options.isGlobalVariableTraceEnabled("site/[x].a/host"));
        assertFalse(options.isGlobalVariableTraceEnabled("site/x.a/host"));
        assertTrue(options.getGlobalVariableTracePattern().matcher("a.[x]/b").matches());
        assertFalse(options.getGlobalVariableTracePattern().matcher("a.x/b").matches());
    }

    @Test
    public void testAntRejectsEmptyGlobsWithAttributeName() {
        for (String name : new String[] {"traceObjectGlob", "traceVariableGlob"}) {
            PanCompilerTask task = new PanCompilerTask();
            task.setFormats("none");
            if (name.equals("traceObjectGlob")) task.setTraceObjectGlob("");
            else task.setTraceVariableGlob("");
            try {
                task.execute();
                fail("Expected invalid attribute");
            } catch (BuildException e) {
                assertTrue(e.getMessage().contains(name));
            }
        }
    }
}
