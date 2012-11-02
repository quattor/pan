package org.quattor.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.quattor.pan.CompilerOptions;

public abstract class AbstractPanMojo extends AbstractMojo {

    /**
     * @description root directory of pan sources
     * @parameter expression="${panc.sourceDirectory}"
     *            default-value="${basedir}/src/main/pan"
     * @required
     */
    protected File sourceDirectory;

    /**
     * @description print compilation summary
     * @parameter expression="${panc.verbose}" default-value=false
     * @required
     */
    protected boolean verbose = false;

    /**
     * @description warnings flag ("on", "off", or "fatal")
     * @parameter expression="${panc.warnings}" default-value="on"
     * @required
     */
    protected CompilerOptions.DeprecationWarnings warnings = CompilerOptions.DeprecationWarnings.ON;

    abstract public void execute() throws MojoExecutionException;
}
