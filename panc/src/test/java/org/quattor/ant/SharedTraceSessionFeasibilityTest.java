package org.quattor.ant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
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
import org.apache.tools.ant.types.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.quattor.pan.ttemplate.GlobalVariableTraceSink;
import org.quattor.pan.utils.TestUtils;

/** Exercises reference and task-loader propagation through two actual subant levels. */
public class SharedTraceSessionFeasibilityTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder(TestUtils.getTmpdir());

    private void runNestedBuild(boolean failSecond, boolean isolated) throws Exception {
        File output = temporary.newFolder();
        File templates = new File(TestUtils.getTestdir(), "global_variable_trace");
        File fixture = new File(templates, "shared-session.xml");
        File log = new File(output, "trace.log");
        Files.write(log.toPath(), "old record\n".getBytes(StandardCharsets.UTF_8));

        Project project = new Project();
        project.init();
        project.setUserProperty("fixture", fixture.getAbsolutePath());
        project.setUserProperty("templates", templates.getAbsolutePath());
        project.setUserProperty("output", output.getAbsolutePath());
        project.setUserProperty("fail.second", Boolean.toString(failSecond));
        String probeClasses = new File(SharedTraceProbe.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getAbsolutePath();
        String compilerClasses = new File(PanCompilerTask.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getAbsolutePath();
        project.setUserProperty("probe.classes", probeClasses);
        project.setUserProperty("compiler.classes", compilerClasses);
        if (isolated) {
            // Surefire already has both classes on its parent loader; emulate an Ant task jar.
            Path taskPath = new Path(project);
            taskPath.createPathElement().setLocation(new File(probeClasses));
            taskPath.createPathElement().setLocation(new File(compilerClasses));
            project.addReference("trace.loader", new AntClassLoader(project, taskPath, false));
        } else {
            assertNull(project.getReference("trace.loader"));
        }
        ProjectHelper.configureProject(project, fixture);
        project.executeTarget("define");

        Class<?> rootTask = project.getTaskDefinitions().get("shared-trace-probe");
        ClassLoader taskLoader = rootTask.getClassLoader();
        Object loaderReference = project.getReference("trace.loader");
        assertTrue(loaderReference instanceof AntClassLoader);
        assertSame(rootTask, ((ClassLoader) loaderReference).loadClass(rootTask.getName()));
        if (isolated) {
            assertNotSame(SharedTraceProbe.class.getClassLoader(), taskLoader);
            assertSame(loaderReference, taskLoader);
        } else {
            // Ant's default parent-first loader delegates to Surefire's existing classes.
            assertSame(SharedTraceProbe.class.getClassLoader(), taskLoader);
            assertNotSame(loaderReference, taskLoader);
        }
        project.addReference("trace.expected.loader", loaderReference);
        // Open with the task loader: the isolated sink class has a different identity.
        Class<?> sinkClass = taskLoader.loadClass(GlobalVariableTraceSink.class.getName());
        Object sink = sinkClass.getMethod("open", File.class).invoke(null, output);
        assertSame(taskLoader, sink.getClass().getClassLoader());
        assertEquals(sinkClass, sink.getClass());
        project.addReference("trace.session", sink);
        try {
            try {
                project.executeTarget("root");
            } finally {
                ((AutoCloseable) sink).close();
            }
            if (failSecond) fail("Expected nested task failure");
        } catch (BuildException expected) {
            if (!failSecond) throw expected;
            assertTrue(expected.toString().contains("intentional nested failure"));
        }

        String contents = new String(Files.readAllBytes(log.toPath()), StandardCharsets.UTF_8);
        assertTrue(contents.contains("selected_a"));
        assertTrue(contents.contains("selected_b"));
        assertFalse(contents.contains("old record"));
        // Opening after inspecting the log proves the outer finally released the lock.
        try (GlobalVariableTraceSink reopened = GlobalVariableTraceSink.open(output)) { }
    }

    @Test public void repeatedChildTaskDefinitionsBorrowTheSameSink() throws Exception {
        runNestedBuild(false, true);
    }

    @Test public void nestedFailureRetainsPartialRecordsAndReleasesLock() throws Exception {
        runNestedBuild(true, true);
    }

    @Test public void rootTaskdefCreatesLoaderReferenceForNestedTaskdefs() throws Exception {
        runNestedBuild(false, false);
    }
}
