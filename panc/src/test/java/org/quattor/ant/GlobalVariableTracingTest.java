package org.quattor.ant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.tools.ant.BuildException;
import org.junit.Test;
import org.quattor.pan.CompilerOptions;

public class GlobalVariableTracingTest {
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
