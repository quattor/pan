package org.quattor.maven;

import java.io.File;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.CompilerResults;

/**
 * Goal which runs a pan syntax check on source files.
 * 
 * @goal check-syntax
 * 
 * @phase process-sources
 */
public class PanCheckSyntaxMojo extends AbstractPanMojo {

    public void execute() throws MojoExecutionException {

        CompilerOptions options = CompilerOptions
                .createCheckSyntaxOptions(warnings);

        Set<File> sources = PluginUtils.collectPanSources(sourceDirectory);

        CompilerResults results = Compiler.run(options, null, sources);

        boolean hadError = results.print(verbose);

        if (hadError) {
            throw new MojoExecutionException("pan language syntax check failed");
        }

    }
}
