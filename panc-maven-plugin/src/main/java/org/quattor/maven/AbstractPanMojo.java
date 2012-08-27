package org.quattor.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

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
     * @description deprecation level (<0 is none, 0 is next release, >0 is
     *              future releases)
     * @parameter expression="${panc.deprecationLevel}" default-value=0
     * @required
     */
    protected int deprecationLevel = 0;

    /**
     * @description treat warnings as errors
     * @parameter expression="${panc.failOnWarn}" default-value=false
     * @required
     */
    protected boolean failOnWarn = false;

    /**
     * @description warnings flag ("on", "off", or "fatal")
     * @parameter expression="${panc.warnings}" default-value="on"
     * @required
     */
    protected String warnings = "on";

    abstract public void execute() throws MojoExecutionException;
}
