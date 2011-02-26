package org.quattor.ant;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.parser.ASTTemplate;
import org.quattor.pan.repository.FileSystemSourceRepository;
import org.quattor.pan.repository.SourceType;
import org.quattor.pan.tasks.CompileTask;
import org.quattor.pan.utils.FileStatCache;

public class DependencyChecker {

    private final List<File> includeDirectories;

    private final Pattern ignoreDependencyPattern;

    private final FileStatCache statCache = new FileStatCache();

    private final static Pattern NONE = Pattern.compile("^$");

    public DependencyChecker(List<File> includeDirectories,
            Pattern ignoredDependencyPattern) {

        ArrayList<File> dirs = new ArrayList<File>();

        if (includeDirectories != null) {
            dirs.addAll(includeDirectories);
        } else {
            String userDir = System.getProperty("user.dir");
            File file = new File(userDir).getAbsoluteFile();
            dirs.add(file);
        }
        dirs.trimToSize();
        this.includeDirectories = Collections.unmodifiableList(dirs);

        if (ignoredDependencyPattern != null) {
            this.ignoreDependencyPattern = ignoredDependencyPattern;
        } else {
            this.ignoreDependencyPattern = NONE;
        }
    }

    public List<File> extractOutdatedFiles(List<File> objectFiles,
            File outputDirectory, boolean gzipOutput) {

        LinkedList<File> outdated = new LinkedList<File>();

        String xmlSuffix = (gzipOutput) ? ".xml.gz" : ".xml";

        // Simple options to be used for extracting the template name. No
        // warnings and debugging is off.
        CompilerOptions options = CompilerOptions.createCheckSyntaxOptions(
                Integer.MAX_VALUE, false);

        for (File objectFile : objectFiles) {

            String name = extractLocalizedTemplateName(objectFile, options);

            // Null indicates that some exception was thrown while trying to
            // extract the template name. Assume that the associated file is out
            // of date. The real exception will be thrown when all files are
            // processed.
            if (name == null) {
                outdated.add(objectFile);
                continue;
            }

            File t = new File(outputDirectory, name + xmlSuffix);
            File d = new File(outputDirectory, name + ".xml.dep");

            // Both profile and dependency files must exist.
            if (!(statCache.exists(t) && statCache.exists(d))) {
                outdated.add(objectFile);
                continue;
            }

            // The modification time of the target xml file.
            long targetTime = statCache.getModificationTime(t);

            // Check dependency file was generated after target file.
            if (statCache.isMissingOrModifiedBefore(d, targetTime)) {
                outdated.add(objectFile);
                continue;
            }

            // Do detailed checking of the full dependency file.
            if (isDependencyListOutdated(d, targetTime)) {
                outdated.add(objectFile);
            }
        }

        return outdated;
    }

    public static String extractLocalizedTemplateName(File sourceFile,
            CompilerOptions options) {

        try {

            ASTTemplate ast = CompileTask.CallImpl.compile(sourceFile, options);
            String name = ast.getIdentifier();
            return name.replaceAll("/", File.separator);

        } catch (Exception e) {
            return null;
        }

    }

    public boolean isDependencyListOutdated(File dependencyFile, Long targetTime) {

        boolean outdated = false;

        Scanner scanner = null;

        try {
            scanner = new Scanner(dependencyFile);

            while (scanner.hasNextLine() && !outdated) {
                if (isDependencyOutdated(scanner.nextLine(), targetTime)) {
                    outdated = true;
                    break;
                }
            }

        } catch (IllegalArgumentException e) {

            // This is usually the result of reading a dependency file from an
            // old version of the compiler. Assume that the profile needs to be
            // compiler.
            System.err.println("Warning: Outdated dependency file ("
                    + dependencyFile.toString() + "); compiling profile");
            outdated = true;

        } catch (IOException e) {

            // If there's a problem finding or reading the file, then assume
            // that the dependency is outdated.
            outdated = true;

        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return outdated;
    }

    public boolean isDependencyOutdated(String line, Long targetTime) {

        DependencyInfo info = new DependencyInfo(line);

        if (ignoreDependencyPattern.matcher(info.name).matches()) {
            return false;
        }

        switch (info.type) {

        case TPL:
            return isSourceDependencyOutdated(info, targetTime);
        case PAN:
            return isSourceDependencyOutdated(info, targetTime);
        case PANX:
            return isSourceDependencyOutdated(info, targetTime);
        case TEXT:
            return isTextDependencyOutdated(info, targetTime);
        case ABSENT_SOURCE:
            return (lookupSourceFile(info.name) != null);
        case ABSENT_TEXT:
            return (lookupTextFile(info.name) != null);
        default:
            throw new BuildException("unknown file type: " + info.type);
        }

    }

    public boolean isSourceDependencyOutdated(DependencyInfo info,
            long targetTime) {

        if (isSingleDependencyOutdated(info.file, targetTime)) {
            return true;
        }

        // Check that the location hasn't changed in the path. If it has
        // changed, then profile isn't current.
        File foundFile = lookupSourceFile(info.name);
        return isSingleDependencyDifferent(info.file, foundFile);

    }

    public boolean isTextDependencyOutdated(DependencyInfo info, long targetTime) {

        if (isSingleDependencyOutdated(info.file, targetTime)) {
            return true;
        }

        // Check that the location hasn't changed in the path. If it has
        // changed, then profile isn't current.
        File foundFile = lookupTextFile(info.name);
        return isSingleDependencyDifferent(info.file, foundFile);

    }

    public boolean isSingleDependencyOutdated(File dep, long targetTime) {

        if (dep != null) {
            return statCache.isMissingOrModifiedAfter(dep, targetTime);
        } else {
            return true;
        }

    }

    public File lookupSourceFile(String tplName) {

        String localTplName = FileSystemSourceRepository.localizeName(tplName);

        List<String> sourceFiles = new ArrayList<String>();
        for (String extension : SourceType.getExtensions()) {
            sourceFiles.add(localTplName + extension);
        }

        for (File pathdir : includeDirectories) {
            for (String sourceFile : sourceFiles) {

                File check = new File(pathdir, sourceFile);
                if (statCache.exists(check)) {
                    return check;
                }
            }
        }

        return null;
    }

    public File lookupTextFile(String tplName) {

        String localTplName = FileSystemSourceRepository.localizeName(tplName);

        for (File pathdir : includeDirectories) {

            File check = new File(pathdir, localTplName);
            if (statCache.exists(check)) {
                return check;
            }
        }

        return null;
    }

    public static boolean isSingleDependencyDifferent(File dep, File foundFile) {
        if (foundFile != null) {
            return (!dep.equals(foundFile));
        } else {

            // SPECIAL CASE:
            //
            // If the file hasn't been found at all, then assume the file is
            // up to date. The file may not have been found on the load path
            // because the internal loadpath variable may be used to find
            // the file. In this case, rely on the explicit
            // list of dependencies to pick up changes. NOTE: this check
            // isn't 100% correct. It is possible to move templates around
            // in the "internal" load path; these changes will not be picked
            // up correctly.

            return false;
        }
    }

    public static String stripPanExtensions(String name) {

        for (SourceType type : SourceType.values()) {
            String extension = type.getExtension();
            if (!"".equals(extension)) {
                if (name.endsWith(extension)) {
                    int index = name.lastIndexOf(extension);
                    return name.substring(0, index);
                }
            }
        }

        return name;
    }

    public static File reconstructSingleDependency(String templatePath,
            String tplName, SourceType type) throws URISyntaxException {

        URI path = new URI(templatePath);
        URI fullname = new URI(tplName + type.getExtension());
        URI fullpath = path.resolve(fullname);

        return new File(fullpath).getAbsoluteFile();

    }

    public static class DependencyInfo {

        public final String name;

        public final SourceType type;

        public final File file;

        public DependencyInfo(String dependencyLine) {

            // Format is a whitespace-separated line. The items are 1)
            // template name (or full file name), 2) file type, and 3) full
            // URI for parent directory. The third element is only there if
            // the file wasn't absent.
            String[] fields = dependencyLine.split("\\s+");

            if (fields.length != 2 && fields.length != 3) {
                throw new BuildException("malformed dependency line");
            }

            name = fields[0];
            type = SourceType.valueOf(fields[1]);

            if (fields.length == 3) {

                try {
                    file = reconstructSingleDependency(fields[2], name, type);
                } catch (URISyntaxException e) {
                    throw new BuildException(e.getMessage());
                }

            } else {
                file = null;
            }

            validate();

        }

        private void validate() {

            if (file == null && !type.isAbsent()) {
                throw new BuildException(
                        "missing path information for dependency");
            }

            if (file != null && type.isAbsent()) {
                throw new BuildException("path information for absent file");
            }

        }

    }

}
