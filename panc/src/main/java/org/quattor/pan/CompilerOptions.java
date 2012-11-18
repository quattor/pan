/*
 Copyright (c) 2006 Charles A. Loomis, Jr, Cedric Duprilot, and
 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/CompilerOptions.java $
 $Id: CompilerOptions.java 3937 2008-11-22 10:31:49Z loomis $
 */

package org.quattor.pan;

import static org.quattor.pan.utils.MessageUtils.MSG_CANNOT_LOCATE_OBJECT_TEMPLATE;
import static org.quattor.pan.utils.MessageUtils.MSG_FILE_BUG_REPORT;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_SYNTAX_ROOT_ELEMENT;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_TYPE_FOR_ROOT_ELEMENT;

import java.io.File;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.ConfigurationException;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.output.DepFormatter;
import org.quattor.pan.output.DotFormatter;
import org.quattor.pan.output.Formatter;
import org.quattor.pan.output.JsonFormatter;
import org.quattor.pan.output.JsonGzipFormatter;
import org.quattor.pan.output.PanFormatter;
import org.quattor.pan.output.PanGzipFormatter;
import org.quattor.pan.output.TxtFormatter;
import org.quattor.pan.output.XmlFormatter;
import org.quattor.pan.output.XmlGzipFormatter;
import org.quattor.pan.parser.ASTOperation;
import org.quattor.pan.parser.PanParser;
import org.quattor.pan.parser.PanParserAstUtils;
import org.quattor.pan.repository.ParameterList;
import org.quattor.pan.repository.SourceFile;
import org.quattor.pan.repository.SourceRepository;
import org.quattor.pan.repository.SourceRepositoryFactory;
import org.quattor.pan.template.CompileTimeContext;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Encapsulates the global options for the pan compiler. An instance of this
 * class must be passed to the <code>Compiler</code> itself. Instances of this
 * class are immutable and thread-safe.
 * 
 * @author loomis
 * 
 */
public class CompilerOptions {

    public enum DeprecationWarnings {
        ON, OFF, FATAL;

        public static DeprecationWarnings fromString(String s) {
            return DeprecationWarnings.valueOf(s.toUpperCase());
        }
    };

    /**
     * The iteration limit during the compilation to avoid infinite loops.
     */
    public final int maxIteration;

    /**
     * The call depth limit which is used to prevent infinite recursion in the
     * compiler.
     */
    public final int maxRecursion;

    /**
     * The <code>Formatter</code> that will be used to format the machine
     * profiles.
     */
    public final List<Formatter> formatters;

    /**
     * The directory that will contain the produced machine profiles and
     * dependency files (if any).
     */
    public final File outputDirectory;

    /**
     * Define the deprecation level for compilation: ON, OFF, or FATAL.
     */
    public final DeprecationWarnings deprecationWarnings;

    public final SourceRepository sourceRepository;

    /**
     * Patterns that are matched against a template name to determine if debug()
     * calls are turned on or off. Template names matching one of the patterns
     * in the include set will have the debugging turned on UNLESS it matches a
     * pattern in the exclude set.
     */
    private final Pattern debugNsInclude;
    private final Pattern debugNsExclude;

    /**
     * Directory that will contain the annotation output files.
     */
    public final File annotationDirectory;

    /**
     * Base directory of source files for generation of annotation output files.
     * These files will be generated at locations relative to the base directory
     * in the specified output directory.
     */
    public final File annotationBaseDirectory;

    public final HashResource rootElement;

    /**
     * Construct a CompilerOptions instance to drive a Compiler run. Instances
     * of this class are immutable.
     * 
     * @param debugNsInclude
     *            patterns to use to turn on debugging for matching templates
     * @param debugNsExclude
     *            patterns to use to turn off debugging for matching templates
     * @param maxIteration
     *            maximum number of iterations (<=0 unlimited)
     * @param maxRecursion
     *            maximum call depth (<=0 unlimited)
     * @param formatters
     *            formats for machine configuration files
     * @param outputDirectory
     *            output directory for machine configuration and dependency
     *            files (cannot be null if either writeXmlEnable or
     *            writeDepEnabled is true)
     * @param includeDirectories
     *            list of directories to check for template files; directories
     *            must exist and be absolute
     * @param deprecationWarnings
     *            level for deprecation warnings (ON, OFF, or FATAL)
     * @param annotationDirectory
     *            directory that will contain annotation output files
     * @param annotationBaseDirectory
     *            base directory of source files for annotation output
     * @param rootElement
     *            string containing description of root element to use; if null
     *            or empty string, this defaults to an empty nlist
     * @param failOnWarn
     *            if set to true, all warnings will cause compilation to fail
     * @throws SyntaxException
     *             if the expression for the rootElement is invalid
     */
    public CompilerOptions(Pattern debugNsInclude, Pattern debugNsExclude,
            int maxIteration, int maxRecursion, Set<Formatter> formatters,
            File outputDirectory, List<File> includeDirectories,
            DeprecationWarnings deprecationWarnings, File annotationDirectory,
            File annotationBaseDirectory, String rootElement)
            throws SyntaxException {

        // Check that the iteration and call depth limits are sensible. If
        // negative or zero set these effectively to infinity.
        if (maxIteration <= 0) {
            maxIteration = Integer.MAX_VALUE;
        }
        if (maxRecursion <= 0) {
            maxRecursion = Integer.MAX_VALUE;
        }

        // Check that the output directory is sensible if not null.
        if (outputDirectory != null) {
            checkDirectory(outputDirectory, "output");
        }

        // Check all of the include directories.
        for (File d : includeDirectories) {
            if (d != null) {
                checkDirectory(d, "include");
            } else {
                throw new IllegalArgumentException(
                        "include directory may not be null");
            }
        }

        // If we want the XML or dependency files to be written, the output
        // directory must be specified.
        if ((outputDirectory == null) && (formatters.size() > 0)) {
            throw new IllegalArgumentException(
                    "outputDirectory must be specified if output formats are specified");
        }

        // Everything's OK. Copy the values into this instance.
        this.maxIteration = maxIteration;
        this.maxRecursion = maxRecursion;
        this.outputDirectory = outputDirectory;

        this.deprecationWarnings = deprecationWarnings;

        // Deal with the formatter(s).
        List<Formatter> fmts = new LinkedList<Formatter>();
        if (formatters != null) {
            fmts.addAll(formatters);
        }
        this.formatters = Collections.unmodifiableList(fmts);

        // Setup the debug patterns, ensuring that the debug pattern lists are
        // not null.
        this.debugNsInclude = debugNsInclude;
        this.debugNsExclude = debugNsExclude;

        ParameterList parameters = new ParameterList();
        for (File f : includeDirectories) {
            parameters.append("includeDirectory", f.toString());
        }

        SourceRepository value = null;
        try {
            value = SourceRepositoryFactory.create(parameters);
        } catch (ConfigurationException ce) {
            throw new RuntimeException(ce.getMessage());
            // TODO: Add correct behavior.
        }
        sourceRepository = value;

        this.annotationDirectory = annotationDirectory;
        if (annotationDirectory != null) {
            checkDirectory(annotationDirectory, "annotation");
        }

        this.annotationBaseDirectory = annotationBaseDirectory;
        if (annotationBaseDirectory != null) {
            checkDirectory(annotationBaseDirectory, "annotation base");
        }

        this.rootElement = createRootElement(rootElement);
    }

    // Utility method to turn old options into new deprecation flag.
    public static DeprecationWarnings getDeprecationWarnings(
            int deprecationLevel, boolean failOnWarn) {

        if (deprecationLevel < 0) {
            return DeprecationWarnings.OFF;
        } else {
            return failOnWarn ? DeprecationWarnings.FATAL
                    : DeprecationWarnings.ON;
        }
    }

    /**
     * Create a CompilerOptions object that is appropriate for just doing a
     * syntax check.
     * 
     * @param deprecationLevel
     *            set the deprecation level, the higher the level the fewer
     *            deprecation warnings are produced; 0 produces all warnings
     * @param failOnWarn
     *            if set to true, all warnings will cause compilation to fail
     * @return
     */
    public static CompilerOptions createCheckSyntaxOptions(
            DeprecationWarnings deprecationWarnings) {

        Pattern debugNsInclude = null;
        Pattern debugNsExclude = null;
        int maxIteration = 5000;
        int maxRecursion = 50;
        Set<Formatter> formatters = new HashSet<Formatter>();
        File outputDirectory = null;
        File annotationDirectory = null;
        File annotationBaseDirectory = null;
        LinkedList<File> includeDirectories = new LinkedList<File>();

        try {
            return new CompilerOptions(debugNsInclude, debugNsExclude,
                    maxIteration, maxRecursion, formatters, outputDirectory,
                    includeDirectories, deprecationWarnings,
                    annotationDirectory, annotationBaseDirectory, null);

        } catch (SyntaxException consumed) {
            throw CompilerError.create(MSG_FILE_BUG_REPORT);
        }
    }

    /**
     * Create a CompilerOptions object that is appropriate for just doing a
     * syntax check.
     * 
     * @param deprecationLevel
     *            set the deprecation level, the higher the level the fewer
     *            deprecation warnings are produced; 0 produces all warnings
     * @param failOnWarn
     *            if set to true, all warnings will cause compilation to fail
     * @return
     */
    public static CompilerOptions createAnnotationOptions(
            File annotationDirectory, File annotationBaseDirectory) {

        Pattern debugNsInclude = null;
        Pattern debugNsExclude = null;
        int maxIteration = 5000;
        int maxRecursion = 50;
        Set<Formatter> formatters = new HashSet<Formatter>();
        File outputDirectory = null;
        LinkedList<File> includeDirectories = new LinkedList<File>();

        try {
            return new CompilerOptions(debugNsInclude, debugNsExclude,
                    maxIteration, maxRecursion, formatters, outputDirectory,
                    includeDirectories, DeprecationWarnings.OFF,
                    annotationDirectory, annotationBaseDirectory, null);

        } catch (SyntaxException consumed) {
            throw CompilerError.create(MSG_FILE_BUG_REPORT);
        }
    }

    public static HashResource createRootElement(String rootElement)
            throws SyntaxException {

        if (rootElement == null || "".equals(rootElement.trim())) {

            return new HashResource();

        } else {

            try {

                PanParser parser = new PanParser(new StringReader(rootElement));
                ASTOperation ast = parser.dml();
                Operation dml = PanParserAstUtils.astToDml(ast);
                return (HashResource) dml.execute(new CompileTimeContext());

            } catch (SyntaxException e) {
                throw SyntaxException.create(null,
                        MSG_INVALID_SYNTAX_ROOT_ELEMENT, e.getMessage());
            } catch (ClassCastException e) {
                throw SyntaxException.create(null,
                        MSG_INVALID_TYPE_FOR_ROOT_ELEMENT);
            }

        }
    }

    /**
     * A private utility function to verify that the directory is really a
     * directory, exists, and absolute.
     * 
     * @param dirs
     *            directory to check
     * @param dtype
     *            name to use in case of errors
     */
    private void checkDirectory(File d, String dtype) {

        if (!d.isAbsolute()) {
            throw new IllegalArgumentException(dtype
                    + " directory must be an absolute path");
        }
        if (!d.exists()) {
            throw new IllegalArgumentException(dtype
                    + " directory does not exist");
        }
        if (!d.isDirectory()) {
            throw new IllegalArgumentException(dtype
                    + " directory value is not a directory");
        }

    }

    /**
     * Resolve a list of object template names and template Files to a set of
     * files based on this instance's include directories.
     * 
     * @param objectNames
     *            object template names to lookup
     * @param tplFiles
     *            template Files to process
     * 
     * @return unmodifiable set of the resolved file names
     */
    public Set<File> resolveFileList(List<String> objectNames,
            Collection<File> tplFiles) {

        // First just copy the named templates.
        Set<File> filesToProcess = new TreeSet<File>();
        if (tplFiles != null) {
            filesToProcess.addAll(tplFiles);
        }

        // Now loop over all of the object template names, lookup the files, and
        // add them to the set of files to process.
        if (objectNames != null) {
            for (String oname : objectNames) {
                SourceFile source = sourceRepository.retrievePanSource(oname);
                if (!source.isAbsent()) {
                    filesToProcess.add(source.getPath());
                } else {
                    throw EvaluationException.create((SourceRange) null,
                            (Context) null, MSG_CANNOT_LOCATE_OBJECT_TEMPLATE,
                            oname);
                }
            }
        }

        return Collections.unmodifiableSet(filesToProcess);
    }

    /**
     * A utility function that checks a given template name against the list of
     * debug include and exclude patterns.
     * 
     * @param tplName
     *            name of the template to check
     * 
     * @return flag indicating whether debugging should be activated or not
     */
    public boolean checkDebugEnabled(String tplName) {

        // Check first the exclude patterns. Any matching pattern in the exclude
        // list means that the debugging is disabled for the given template.
        if (debugNsExclude != null && debugNsExclude.matcher(tplName).matches()) {
            return false;
        }

        // Now check the include patterns. Any matching pattern here means that
        // the debugging for this template is enabled.
        if (debugNsInclude != null && debugNsInclude.matcher(tplName).matches()) {
            return true;
        }

        // If we get here, then the template didn't match anything. By default,
        // the debugging is turned off.
        return false;
    }

    // FIXME: This code duplicates code that is also in clojure. The clojure
    // code should eventually be used for generating the list of formatters to
    // be used for a compilation.
    public static Set<Formatter> getFormatters(String s) {

        HashSet<Formatter> formatters = new HashSet<Formatter>();

        String[] names = s.trim().split("\\s*,\\s*");
        for (String fname : names) {
            if ("text".equals(fname)) {
                formatters.add(TxtFormatter.getInstance());
            } else if ("json".equals(fname)) {
                formatters.add(JsonFormatter.getInstance());
            } else if ("json.gz".equals(fname)) {
                formatters.add(JsonGzipFormatter.getInstance());
            } else if ("dot".equals(fname)) {
                formatters.add(DotFormatter.getInstance());
            } else if ("pan".equals(fname)) {
                formatters.add(PanFormatter.getInstance());
            } else if ("pan.gz".equals(fname)) {
                formatters.add(PanGzipFormatter.getInstance());
            } else if ("xml".equals(fname)) {
                formatters.add(XmlFormatter.getInstance());
            } else if ("xml.gz".equals(fname)) {
                formatters.add(XmlGzipFormatter.getInstance());
            } else if ("dep".equals(fname)) {
                formatters.add(DepFormatter.getInstance());
            } else if ("none".equals(fname)) {
                // No-op
            }
        }

        return formatters;
    }

    /**
     * A verbose representation of all of the options in this instance.
     * 
     * @return String representation of options
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("debug include pattern: ");
        sb.append(debugNsInclude.toString());
        sb.append("\n");
        sb.append("\n");

        sb.append("debug exclude pattern: ");
        sb.append(debugNsExclude.toString());
        sb.append("\n");
        sb.append("\n");

        sb.append("max. iteration: ");
        sb.append(maxIteration);
        sb.append("\n");

        sb.append("max. recursion: ");
        sb.append(maxRecursion);
        sb.append("\n");

        sb.append("output directory: ");
        sb.append(outputDirectory);
        sb.append("\n");

        if (formatters != null) {
            sb.append("formatter: ");
            sb.append(formatters.getClass().toString());
            sb.append("\n");
        } else {
            sb.append("formatter: null\n");
        }

        sb.append("source repository: ");
        sb.append(sourceRepository.toString());
        sb.append("\n");

        return sb.toString();
    }

}
