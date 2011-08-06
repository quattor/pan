package org.quattor.pan.repository;

import java.io.File;
import java.util.List;

import org.quattor.pan.utils.FileUtils;

public class FileSystemSourceRepositoryWithoutSessionDir extends
        FileSystemSourceRepository {

    private final List<File> includeDirectories;

    public FileSystemSourceRepositoryWithoutSessionDir(
            List<File> includeDirectories) {
        this.includeDirectories = validateAndCopyIncludeDirectories(includeDirectories);
    }

    @Override
    public File lookupText(String name) {
        return lookupText(name, emptyRelativePaths);
    }

    @Override
    public File lookupText(String name, List<String> loadpath) {

        assert (name != null);
        assert (loadpath != null);
        assert (loadpath.size() > 0);

        String localName = FileUtils.localizeFilename(name);

        for (File d : includeDirectories) {
            for (String rpath : loadpath) {

                File dir = new File(d, rpath);

                File sourceFile = new File(dir, localName);
                if (sourceFile.exists()) {
                    return sourceFile;
                }

            }

        }

        return null;
    }

    @Override
    public File lookupSource(String name) {
        return lookupSource(name, emptyRelativePaths);
    }

    @Override
    public File lookupSource(String name, List<String> loadpath) {

        assert (name != null);
        assert (loadpath != null);
        assert (loadpath.size() > 0);

        String localName = FileUtils.localizeFilename(name);

        for (File d : includeDirectories) {
            for (String rpath : loadpath) {

                File dir = new File(d, rpath);

                for (String suffix : sourceFileExtensions) {

                    File sourceFile = new File(dir, localName + suffix);
                    if (sourceFile.exists()) {
                        return sourceFile;
                    }
                }

            }

        }

        return null;
    }

}
