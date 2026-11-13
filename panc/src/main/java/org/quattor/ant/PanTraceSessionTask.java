package org.quattor.ant;

import java.io.File;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.quattor.pan.ttemplate.GlobalVariableTraceSink;

/** Owns one trace file for all nested compiler tasks. */
public class PanTraceSessionTask extends Task implements TaskContainer {
    private File outputDir;
    private String referenceId;
    private final List<Task> tasks = new ArrayList<Task>();

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    @Override public void addTask(Task task) {
        tasks.add(task);
    }

    @Override public void execute() {
        if (outputDir == null) throw new BuildException("outputDir is required");
        if (referenceId == null || referenceId.trim().isEmpty()) {
            throw new BuildException("referenceId is required");
        }
        if (getProject().getReferences().containsKey(referenceId)) {
            throw new BuildException("referenceId already exists: " + referenceId);
        }
        try (GlobalVariableTraceSink sink = GlobalVariableTraceSink.open(outputDir)) {
            getProject().addReference(referenceId, sink);
            try {
                for (Task task : tasks) task.perform();
                sink.throwIfFailed();
            } catch (RuntimeException | Error failure) {
                try {
                    sink.throwIfFailed();
                } catch (UncheckedIOException traceFailure) {
                    if (failure != traceFailure) failure.addSuppressed(traceFailure);
                }
                throw failure;
            } finally {
                if (getProject().getReference(referenceId) == sink) {
                    getProject().getReferences().remove(referenceId);
                }
            }
        } catch (UncheckedIOException failure) {
            throw new BuildException(failure.getMessage(), failure);
        }
    }
}
