package org.quattor.pan.repository;

import static org.quattor.pan.utils.MessageUtils.MSG_NON_ABSOLUTE_PATH_IN_INCLUDE_DIRS;
import static org.quattor.pan.utils.MessageUtils.MSG_NON_DIRECTORY_IN_INCLUDE_DIRS;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

import org.quattor.pan.exceptions.EvaluationException;

abstract public class FileSystemSourceRepository implements SourceRepository {

    // When there are no real relative paths in the load path, use a list of
    // relative paths with only the empty string as an element. This avoids
    // having to constantly check whether the relative path is null.
    protected final static List<String> emptyRelativePaths;
    static {
        ArrayList<String> list = new ArrayList<String>(1);
        list.add(""); //$NON-NLS-1$
        list.trimToSize();
        emptyRelativePaths = list;
    }

    // Collect all of the possible source file types (*.tpl, *.pan, etc.).
    protected static final List<String> sourceFileExtensions;
    static {

        ArrayList<String> extensions = new ArrayList<String>();

        for (SourceType type : SourceType.values()) {
            if (type.isSource()) {
                extensions.add(type.getExtension());
            }
        }

        extensions.trimToSize();

        sourceFileExtensions = Collections.unmodifiableList(extensions);
    };

    // ConcurrentHashMap is ok for retrieveCache with loadpath
    //  2 threads will get the same result,
    //  so it's not an issue if they add the file
    // No static Map, due to eg include directories
    protected Map<String, SourceFile> retrieveCacheLoadpath = new ConcurrentHashMap<String, SourceFile>(1000);

    protected FileSystemSourceRepository() {
    }

    public static SourceRepository getInstance(ParameterList parameters) {

        if (parameters == null) {
            parameters = new ParameterList();
        }

        List<File> includeDirectories = new ArrayList<File>();
        for (Parameter parameter : parameters) {
            String name = parameter.getKey();
            if ("includeDirectory".equals(name)) {
                includeDirectories.add(new File(parameter.getValue()));
            }
        }

        return new FileSystemSourceRepositoryImpl(
                includeDirectories);
    }

    abstract public File lookupSource(String name);

    abstract public File lookupSource(String name, List<String> loadpath);

    abstract public File lookupText(String name);

    abstract public File lookupText(String name, List<String> loadpath);

    public SourceFile retrievePanSource(String name) {
        File file = lookupSource(name);
        return createPanSourceFile(name, file);
    }

    // Optimised due to lots of calls and slow lookupSource
    public SourceFile retrievePanSource(String name, List<String> loadpath) {

        String cacheKey = name + ((String) " ") + String.join(":", loadpath);

        // containsKey uses get
        SourceFile cachedResult = retrieveCacheLoadpath.get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        } else {
            File file = lookupSource(name, loadpath);
            SourceFile result = createPanSourceFile(name, file);
            retrieveCacheLoadpath.put(cacheKey, result);
            return result;
        }
    }

    public SourceFile retrieveTxtSource(String name) {
        File file = lookupText(name);
        return createTxtSourceFile(name, file);
    }

    public SourceFile retrieveTxtSource(String name, List<String> loadpath) {
        File file = lookupText(name, loadpath);
        return createTxtSourceFile(name, file);
    }

    private SourceFile createPanSourceFile(String name, File file) {
        return new SourceFile(name, true, file);
    }

    private SourceFile createTxtSourceFile(String name, File file) {
        return new SourceFile(name, false, file);
    }

    protected List<File> validateAndCopyIncludeDirectories(
            List<File> includeDirectories) {

        ArrayList<File> dirs = new ArrayList<File>();

        // Create a copy to avoid any external modifications. Use the current
        // working directory if paths is null or empty.
        if (includeDirectories != null && includeDirectories.size() != 0) {

            for (File d : includeDirectories) {
                if (!d.isAbsolute()) {
                    throw EvaluationException
                            .create(MSG_NON_ABSOLUTE_PATH_IN_INCLUDE_DIRS,
                                    d.toString());
                }
                if (!d.isDirectory()) {
                    throw EvaluationException.create(
                            MSG_NON_DIRECTORY_IN_INCLUDE_DIRS, d.toString());
                }
            }

            dirs.addAll(includeDirectories);

        } else {

            // No values were given. Use current working directory.
            dirs.add(new File(System.getProperty("user.dir"))); //$NON-NLS-1$

        }

        dirs.trimToSize();

        return Collections.unmodifiableList(dirs);
    }

}
