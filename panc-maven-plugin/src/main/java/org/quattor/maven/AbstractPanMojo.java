package org.quattor.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.quattor.pan.CompilerOptions;

public abstract class AbstractPanMojo extends AbstractMojo {

    /**
     * root directory of pan sources
     * 
     * @parameter property="panc.sourceDirectory"
     *            default-value="${basedir}/src/main/pan"
     * @required
     */
    protected File sourceDirectory;

    /**
     * print compilation summary
     * 
     * @parameter property="panc.verbose" default-value=false
     * @required
     */
    protected boolean verbose = false;

    /**
     * warnings flag ("ON", "OFF", or "FATAL")
     * 
     * @parameter property="panc.warnings" default-value="on"
     * @required
     */
    protected String warnings = "on";

    abstract public void execute() throws MojoExecutionException;

    protected CompilerOptions.DeprecationWarnings warningsFromString(String s)
            throws MojoExecutionException {
        try {
            return CompilerOptions.DeprecationWarnings.fromString(s);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("invalid value for warnings: " + s);
        }
    }
}
