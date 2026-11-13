package org.quattor.ant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.quattor.pan.ttemplate.GlobalVariableTraceSink;
import org.quattor.pan.utils.TestUtils;

/** Integration tests of the published Ant owner and borrower tasks. */
public class PanTraceSessionTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder(TestUtils.getTmpdir());

    private Project project(File output) throws Exception {
        return project(output, false);
    }

    private Project project(File output, boolean isolated) throws Exception {
        File templates = new File(TestUtils.getTestdir(), "global_variable_trace");
        File fixture = new File(templates, "production-session.xml");
        Project project = new Project();
        project.init();
        project.setUserProperty("output", output.getAbsolutePath());
        project.setUserProperty("templates", templates.getAbsolutePath());
        project.setUserProperty("fixture", fixture.getAbsolutePath());
        File compilerClasses = new File(PanCompilerTask.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
        project.setUserProperty("compiler.classes", compilerClasses.getAbsolutePath());
        if (isolated) {
            Path path = new Path(project);
            path.createPathElement().setLocation(compilerClasses);
            project.addReference("trace.loader", new AntClassLoader(project, path, false));
        }
        ProjectHelper.configureProject(project, fixture);
        project.addTaskDefinition("shared-trace-failure-probe", FailureProbe.class);
        return project;
    }

    private String log(File output) throws Exception {
        return new String(Files.readAllBytes(new File(output, "trace.log").toPath()), StandardCharsets.UTF_8);
    }

    private void expectFailure(Project project, String target, String message) {
        try {
            project.executeTarget(target);
            fail("Expected failure: " + target);
        } catch (BuildException e) {
            assertTrue(e.toString(), e.toString().contains(message));
        }
    }

    @Test public void nestedPancTasksShareOwnerAndLaterSessionStartsFresh() throws Exception {
        File output = temporary.newFolder();
        Project project = project(output);
        project.executeTarget("root");
        String first = log(output);
        assertTrue(first.contains("selected_a"));
        assertTrue(first.contains("selected_b"));
        int selectedARecords = 0;
        for (String line : first.split("\n")) {
            if (line.contains("selected_a") && line.contains("\"action\":\"read\"")) selectedARecords++;
        }
        assertEquals(2, selectedARecords);
        assertNull(project.getReference("trace.session"));
        project.executeTarget("fresh");
        assertFalse(log(output).contains("selected_a"));
        assertTrue(log(output).contains("selected_b"));
        assertNull(project.getReference("trace.session"));
    }

    @Test public void nestedCompilationFailureRetainsRecordsAndReleasesOwner() throws Exception {
        File output = temporary.newFolder();
        Project project = project(output);
        expectFailure(project, "root-fail", "Compilation failed");
        assertTrue(log(output).contains("rejected"));
        assertNull(project.getReference("trace.session"));
        try (GlobalVariableTraceSink ignored = GlobalVariableTraceSink.open(output)) { }
    }

    @Test public void actualNestedTasksShareIsolatedAntLoaderAndSink() throws Exception {
        File output = temporary.newFolder();
        Project project = project(output, true);
        project.executeTarget("root");
        assertSame(project.getReference("trace.loader"),
                project.getTaskDefinitions().get("panc").getClassLoader());
        assertTrue(log(output).contains("selected_a"));
        assertTrue(log(output).contains("selected_b"));
        assertNull(project.getReference("trace.session"));
        try (GlobalVariableTraceSink ignored = GlobalVariableTraceSink.open(output)) { }
    }

    @Test public void disabledTracingSkipsMissingReferenceAndLog() throws Exception {
        File output = temporary.newFolder();
        project(output).executeTarget("disabled");
        assertFalse(new File(output, "trace.log").exists());
    }

    @Test public void missingAndIncompatibleReferencesFailClearly() throws Exception {
        File output = temporary.newFolder();
        Project project = project(output);
        expectFailure(project, "invalid", "invalid");
        project.addReference("invalid", "wrong type");
        expectFailure(project, "invalid", "classloader");
        assertFalse(new File(output, "trace.log").exists());
    }

    @Test public void ownerValidatesAttributesAndCollisionWithoutTouchingExistingState() throws Exception {
        File output = temporary.newFolder();
        Project project = project(output);
        expectFailure(project, "missing-output", "outputDir");
        expectFailure(project, "missing-reference", "referenceId");
        Object existing = new Object();
        project.addReference("trace.session", existing);
        Files.write(new File(output, "trace.log").toPath(), "preserve".getBytes(StandardCharsets.UTF_8));
        expectFailure(project, "root", "trace.session");
        assertSame(existing, project.getReference("trace.session"));
        assertEquals("preserve", log(output));
    }

    /** Simulates a handled write failure and a simultaneous nested/close failure. */
    public static class FailureProbe extends Task {
        private String mode;

        public void setMode(String mode) { this.mode = mode; }

        @Override public void execute() {
            GlobalVariableTraceSink sink = (GlobalVariableTraceSink) getProject().getReference("trace.session");
            sink.close();
            if (mode.equals("nested")) throw new BuildException("nested failure");
            try {
                sink.emit("record");
                fail("Expected write failure");
            } catch (java.io.UncheckedIOException expected) { }
        }
    }

    @Test public void ownerReportsSuppressedWritesAndPreservesNestedFailureWithCloseFailure() throws Exception {
        File output = temporary.newFolder();
        Project project = project(output);
        expectFailure(project, "suppressed-write", "cannot write trace file");
        assertNull(project.getReference("trace.session"));
        try {
            project.executeTarget("nested-and-close-failure");
            fail("Expected nested failure");
        } catch (BuildException e) {
            assertTrue(e.toString().contains("nested failure"));
            assertEquals(1, e.getSuppressed().length);
            assertTrue(e.getSuppressed()[0].getMessage().contains("cannot close trace file"));
        }
        assertNull(project.getReference("trace.session"));
    }

    @Test public void ownerReportsOpenFailureWithoutPublishingReference() throws Exception {
        File output = temporary.newFolder();
        Project project = project(output);
        try (GlobalVariableTraceSink sink = GlobalVariableTraceSink.open(output)) {
            sink.emit("preserve");
            expectFailure(project, "root", "cannot open trace file");
            assertEquals("preserve\n", log(output));
        }
        assertNull(project.getReference("trace.session"));
    }
}
