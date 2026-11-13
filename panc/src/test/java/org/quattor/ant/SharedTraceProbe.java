package org.quattor.ant;

import java.io.File;
import java.util.Collections;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerResults;
import org.quattor.pan.ttemplate.GlobalVariableTraceSink;

/** Test-only receiver for a sink inherited by an independently defined Ant task. */
public class SharedTraceProbe extends Task {
    private File templates;
    private File output;
    private String object;
    private boolean fail;

    public void setTemplates(File templates) { this.templates = templates; }
    public void setOutput(File output) { this.output = output; }
    public void setObject(String object) { this.object = object; }
    public void setFail(boolean fail) { this.fail = fail; }

    @Override
    public void execute() {
        Object reference = getProject().getReference("trace.session");
        if (reference == null || reference.getClass() != GlobalVariableTraceSink.class
                || getClass().getClassLoader() != GlobalVariableTraceSink.class.getClassLoader()
                || getClass().getClassLoader() != PanCompilerTask.class.getClassLoader()
                || getClass().getClassLoader() != getProject().getReference("trace.loader")) {
            throw new BuildException("inherited sink and receiving task use different class identities");
        }
        GlobalVariableTraceSink sink = (GlobalVariableTraceSink) reference;
        PanCompilerTask task = new PanCompilerTask();
        task.setProject(getProject());
        task.setOutputDir(output);
        task.setFormats("pan,dep");
        task.setTraceObjectGlob("*");
        task.setTraceFile(true);
        task.addConfiguredPath(new Path(getProject(), templates.getAbsolutePath()));
        CompilerResults results = Compiler.run(task.createCompilerOptions(), null,
                Collections.singletonList(new File(templates, object + ".pan")), sink);
        sink.throwIfFailed();
        if (results.print(false)) throw new BuildException("probe compilation failed");
        if (fail) throw new BuildException("intentional nested failure");
    }
}
